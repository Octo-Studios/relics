package it.hurts.sskirillss.relics.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.ParticleRegistry;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

public class BasicColoredParticle extends TextureSheetParticle {
    private final Constructor constructor;

    private float oldQuadSize;
    private float currentQuadSize;

    private final Vector3f startPos;

    public BasicColoredParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Constructor constructor) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        setColor(constructor.getColor().getRed() / 255F, constructor.getColor().getGreen() / 255F, constructor.getColor().getBlue() / 255F);
        setSize(constructor.getDiameter(), constructor.getDiameter());
        setAlpha(constructor.getColor().getAlpha() / 255F);
        setLifetime(constructor.getLifetime());

        this.constructor = constructor;

        this.quadSize = constructor.getDiameter();
        this.hasPhysics = constructor.isPhysical();

        this.oldQuadSize = quadSize;
        this.currentQuadSize = quadSize;

        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        this.startPos = new Vector3f((float) x, (float) y, (float) z);
    }

    private Vector3f samplePosition(float t) {
        var pathPoints = constructor.getPathPoints();

        int n = pathPoints.size();
        if (n == 0) return new Vector3f((float) x, (float) y, (float) z);
        if (n == 1) return pathPoints.get(0);
        float f = t * (n - 1);
        int i = Math.min((int) Math.floor(f), n - 2);
        float lt = f - i;
        Vector3f p0 = pathPoints.get(Math.max(i - 1, 0));
        Vector3f p1 = pathPoints.get(i);
        Vector3f p2 = pathPoints.get(i + 1);
        Vector3f p3 = pathPoints.get(Math.min(i + 2, n - 1));
        float tt = lt, uu = 1 - tt;
        float tt2 = tt * tt, uu2 = uu * uu, tt3 = tt2 * tt, uu3 = uu2 * uu;
        return new Vector3f(p0).mul(-0.5f * tt3 + tt2 - 0.5f * tt)
                .add(new Vector3f(p1).mul(1.5f * tt3 - 2.5f * tt2 + 1f))
                .add(new Vector3f(p2).mul(-1.5f * tt3 + 2.0f * tt2 + 0.5f * tt))
                .add(new Vector3f(p3).mul(0.5f * tt3 - 0.5f * tt2));
    }

    private Color sampleColor(float t) {
        var colorStops = constructor.getColorStops();

        if (colorStops == null || colorStops.isEmpty()) {
            return constructor.getColor();
        }
        int m = colorStops.size();
        if (m == 1) {
            return colorStops.get(0);
        }
        float f = t * (m - 1);
        int i = Math.min((int) Math.floor(f), m - 2);
        float lt = f - i;
        Color a = colorStops.get(i), b = colorStops.get(i + 1);
        int r = (int) Mth.lerp(lt, a.getRed(), b.getRed());
        int g = (int) Mth.lerp(lt, a.getGreen(), b.getGreen());
        int bl = (int) Mth.lerp(lt, a.getBlue(), b.getBlue());
        int al = (int) Mth.lerp(lt, a.getAlpha(), b.getAlpha());
        return new Color(r, g, bl, al);
    }

    @Override
    public void tick() {
        this.oldQuadSize = quadSize;
        this.currentQuadSize *= constructor.getScaleModifier();

        xo = x;
        yo = y;
        zo = z;

        oRoll = roll;
        roll += constructor.getRoll();

        float t = age / (float) lifetime;
        Color c = sampleColor(t);
        setColor(c.getRed() / 255f,
                c.getGreen() / 255f,
                c.getBlue() / 255f);
        setAlpha(c.getAlpha() / 255f);

        var pathPoints = constructor.getPathPoints();

        if (pathPoints != null && pathPoints.size() >= 2) {
            Vector3f rel = samplePosition(t);
            Vector3f target = new Vector3f(startPos).add(rel);
            Vector3f cur = new Vector3f((float) x, (float) y, (float) z);
            Vector3f d = target.sub(cur, new Vector3f());
            xd = d.x();
            yd = d.y();
            zd = d.z();
        }

        move(xd, yd, zd);

        if (this.age++ >= this.lifetime)
            this.remove();
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Nonnull
    @Override
    public ParticleRenderType getRenderType() {
        return constructor.isVisibleThroughWalls() ? RENDERER_NO_DEPTH : RENDERER_TRANSLUCENT;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        this.quadSize = Mth.lerp(partialTicks, oldQuadSize, currentQuadSize);

        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        super.render(buffer, renderInfo, partialTicks);
    }

    public static final ParticleRenderType RENDERER_NO_DEPTH = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager manager) {
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();

            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return Relics.MODID + ":" + "basic_colored_no_depth";
        }
    };

    public static final ParticleRenderType RENDERER_TRANSLUCENT = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager manager) {
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return Relics.MODID + ":" + "basic_colored_translucent";
        }
    };

    @Data
    @Builder
    public static class Constructor {
        @Builder.Default
        private Color color;

        @Builder.Default
        private float diameter = 1F;

        @Builder.Default
        private float roll = 0F;

        @Builder.Default
        private boolean physical = true;

        @Builder.Default
        private boolean visibleThroughWalls = false;

        @Builder.Default
        private int lifetime = 20;

        @Builder.Default
        private float scaleModifier = 1F;

        @Builder.Default
        private List<Vector3f> pathPoints = List.of();

        @Builder.Default
        private List<Color> colorStops = List.of();

        public static class ConstructorBuilder {
            private Color color = new Color(0xFFFFFFFF, true);

            public ConstructorBuilder color(int color) {
                this.color = new Color(color, true);

                return this;
            }

            public ConstructorBuilder color(float r, float g, float b, float a) {
                return this.color(new Color(r, g, b, a).getRGB());
            }

            public ConstructorBuilder color(float r, float g, float b) {
                return this.color(r, g, b, 1F);
            }

            public ConstructorBuilder color(int r, int g, int b, int a) {
                return this.color(r / 255F, g / 255F, b / 255F, a / 255F);
            }

            public ConstructorBuilder color(int r, int g, int b) {
                return this.color(r, g, b, 0xFF);
            }
        }
    }

    public static class Options implements ParticleOptions {
        @Getter
        private final Constructor data;

        private Options(int color, float diameter, int lifetime, float roll, float scaleModifier, boolean visibleThroughWalls) {
            this.data = Constructor.builder()
                    .color(color)
                    .diameter(diameter)
                    .lifetime(lifetime)
                    .roll(roll)
                    .scaleModifier(scaleModifier)
                    .visibleThroughWalls(visibleThroughWalls)
                    .build();
        }

        public Options(Constructor data) {
            this.data = data;
        }

        @Nonnull
        @Override
        public ParticleType<Options> getType() {
            return ParticleRegistry.BASIC_COLORED.get();
        }

        public static final MapCodec<Options> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Codec.INT.fieldOf("color").forGetter(options -> options.getData().getColor().getRGB()),
                        Codec.FLOAT.fieldOf("diameter").forGetter(options -> options.getData().getDiameter()),
                        Codec.INT.fieldOf("lifetime").forGetter(options -> options.getData().getLifetime()),
                        Codec.FLOAT.fieldOf("roll").forGetter(options -> options.getData().getRoll()),
                        Codec.FLOAT.fieldOf("scaleModifier").forGetter(options -> options.getData().getScaleModifier()),
                        Codec.BOOL.fieldOf("visibleThroughWalls").forGetter(options -> options.getData().isVisibleThroughWalls())
                ).apply(instance, Options::new));

        public static final StreamCodec<ByteBuf, Options> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, options -> options.getData().getColor().getRGB(),
                ByteBufCodecs.FLOAT, options -> options.getData().getDiameter(),
                ByteBufCodecs.INT, options -> options.getData().getLifetime(),
                ByteBufCodecs.FLOAT, options -> options.getData().getRoll(),
                ByteBufCodecs.FLOAT, options -> options.getData().getScaleModifier(),
                ByteBufCodecs.BOOL, options -> options.getData().isVisibleThroughWalls(),
                Options::new
        );
    }

    public static class Factory implements ParticleProvider<Options> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(Options options, ClientLevel world, double xPos, double yPos, double zPos, double xVelocity, double yVelocity, double zVelocity) {
            BasicColoredParticle particle = new BasicColoredParticle(world, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity, options.getData());

            particle.pickSprite(sprites);

            return particle;
        }
    }

    public static class Type extends ParticleType<Options> {
        public Type() {
            super(false);
        }

        @Override
        public MapCodec<Options> codec() {
            return Options.CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, Options> streamCodec() {
            return Options.STREAM_CODEC;
        }
    }
}