package it.hurts.sskirillss.relics.client.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.entities.GhostlyFogEntity;
import it.hurts.sskirillss.relics.init.RelicsParticles;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
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

public class GhostlyFogParticle extends TextureSheetParticle {
    public static final ParticleRenderType RENDERER = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager manager) {
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return Relics.MODID + ":ghostly_fog";
        }
    };

    private final float initialSize;
    private final float targetSize;
    private final float rotation;
    private final float spin;

    protected GhostlyFogParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int lifetimeLimit) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.lifetime = Math.min(Math.max(1, lifetimeLimit), GhostlyFogEntity.PARTICLE_MIN_LIFETIME + this.random.nextInt(GhostlyFogEntity.PARTICLE_RANDOM_LIFETIME_BOUND));
        this.initialSize = 0.55F + this.random.nextFloat() * 0.35F;
        this.targetSize = this.initialSize * (1.8F + this.random.nextFloat() * 0.8F);
        this.quadSize = this.initialSize;
        this.rotation = this.random.nextFloat() * Mth.TWO_PI;
        this.spin = (this.random.nextFloat() - 0.5F) * 0.012F;
        this.gravity = -0.002F;
        this.friction = 0.93F;
        this.hasPhysics = true;

        this.alpha = 0F;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
    }

    @Override
    public void tick() {
        super.tick();

        var progress = this.age / (float) this.lifetime;
        
        var fadeIn = Mth.clamp(progress / 0.18F, 0F, 1F);
        var fadeOut = Mth.clamp((1F - progress) / 0.18F, 0F, 1F);

        this.alpha = 0.15F * fadeIn * fadeOut;
        this.quadSize = Mth.lerp(Mth.clamp(progress, 0F, 1F), this.initialSize, this.targetSize);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        var x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - camera.getPosition().x());
        var y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - camera.getPosition().y()) + 0.015F;
        var z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - camera.getPosition().z());
        var angle = this.rotation + (this.age + partialTicks) * this.spin;
        var light = this.getLightColor(partialTicks);
        var size = this.getQuadSize(partialTicks);
        var cos = Mth.cos(angle) * size;
        var sin = Mth.sin(angle) * size;

        var x1 = x - cos - sin;
        var z1 = z - sin + cos;
        var x2 = x - cos + sin;
        var z2 = z - sin - cos;
        var x3 = x + cos + sin;
        var z3 = z + sin - cos;
        var x4 = x + cos - sin;
        var z4 = z + sin + cos;

        this.addVertex(buffer, x1, y, z1, this.getU1(), this.getV1(), light);
        this.addVertex(buffer, x4, y, z4, this.getU0(), this.getV1(), light);
        this.addVertex(buffer, x3, y, z3, this.getU0(), this.getV0(), light);
        this.addVertex(buffer, x2, y, z2, this.getU1(), this.getV0(), light);

        this.addVertex(buffer, x1, y, z1, this.getU1(), this.getV1(), light);
        this.addVertex(buffer, x2, y, z2, this.getU1(), this.getV0(), light);
        this.addVertex(buffer, x3, y, z3, this.getU0(), this.getV0(), light);
        this.addVertex(buffer, x4, y, z4, this.getU0(), this.getV1(), light);
    }

    private void addVertex(VertexConsumer buffer, float x, float y, float z, float u, float v, int light) {
        buffer.addVertex(x, y, z).setUv(u, v).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Nonnull
    @Override
    public ParticleRenderType getRenderType() {
        return RENDERER;
    }

    public record Options(int lifetimeLimit) implements ParticleOptions {
        public Options {
            lifetimeLimit = Math.max(1, lifetimeLimit);
        }

        @Nonnull
        @Override
        public ParticleType<Options> getType() {
            return RelicsParticles.GHOSTLY_FOG.get();
        }

        public static final MapCodec<Options> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Codec.INT.fieldOf("lifetime_limit").forGetter(Options::lifetimeLimit)
                ).apply(instance, Options::new));

        public static final StreamCodec<ByteBuf, Options> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, Options::lifetimeLimit,
                Options::new
        );
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

    public static class Factory implements ParticleProvider<Options> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(Options options, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            var particle = new GhostlyFogParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options.lifetimeLimit());

            particle.pickSprite(this.sprites);

            return particle;
        }
    }
}
