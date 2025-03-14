package it.hurts.sskirillss.relics.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.init.ParticleRegistry;
import it.hurts.sskirillss.relics.utils.Reference;
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
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;

public class BasicColoredParticle extends TextureSheetParticle {
    private final Constructor constructor;

    private float oldQuadSize;
    private float currentQuadSize;

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
        return constructor.isVisibleThroughWalls() ? RENDERER_NO_DEPTH : ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
            return Reference.MODID + ":" + "basic_colored_no_depth";
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