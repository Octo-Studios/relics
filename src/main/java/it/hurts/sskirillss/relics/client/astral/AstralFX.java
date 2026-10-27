package it.hurts.sskirillss.relics.client.astral;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.items.AstralSatchelItem;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class AstralFX {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final List<Particle> MOTES = new ArrayList<>();

    private static final int MOTE_COUNT = 150;
    private static final double MAX_SPEED = 0.03D;

    private static double lastRadius;

    private static class Particle {
        Vec3 position;
        Vec3 previous;
        Vec3 velocity;
        float size;
        float phase;
        int color;
    }

    public static void init() {
        MOTES.clear();

        lastRadius = AstralSpace.getRadius();

        populate();
    }

    private static int getTargetCount() {
        var ratio = AstralSpace.getRadius() / AstralSatchelItem.DEFAULT_RADIUS;

        return Math.max((int) (MOTE_COUNT * ratio * ratio), MOTE_COUNT);
    }

    private static void populate() {
        var target = getTargetCount();

        while (MOTES.size() > target)
            MOTES.removeLast();

        while (MOTES.size() < target) {
            var particle = new Particle();

            particle.position = randomInSphere(AstralSpace.getRadius() - 1D);
            particle.previous = particle.position;
            particle.velocity = randomVelocity(0.01D);
            particle.size = 0.12F + RANDOM.nextFloat() * 0.2F;
            particle.phase = RANDOM.nextFloat() * 6.28F;
            particle.color = (50 + RANDOM.nextInt(100)) << 16 | RANDOM.nextInt(100) << 8 | 255;

            MOTES.add(particle);
        }
    }

    public static void reset() {
        MOTES.clear();
    }

    public static void tick() {
        var radius = AstralSpace.getRadius();

        if (radius != lastRadius) {
            if (lastRadius > 0D) {
                var factor = Math.max(radius - 1D, 1D) / Math.max(lastRadius - 1D, 1D);

                for (var particle : MOTES) {
                    particle.position = particle.position.scale(factor);
                    particle.previous = particle.position;
                }
            }

            lastRadius = radius;

            populate();
        }

        for (var particle : MOTES) {
            var swirl = new Vec3(-particle.position.z, 0D, particle.position.x).scale(0.00008D);

            particle.previous = particle.position;
            particle.velocity = particle.velocity.scale(0.97D).add(randomVelocity(0.0018D)).add(swirl);

            if (particle.velocity.length() > MAX_SPEED)
                particle.velocity = particle.velocity.normalize().scale(MAX_SPEED);

            particle.position = particle.position.add(particle.velocity);

            var limit = radius - 0.5D;

            if (particle.position.length() > limit) {
                var normal = particle.position.normalize();

                particle.position = normal.scale(limit);
                particle.velocity = particle.velocity.subtract(normal.scale(2D * Math.max(0D, particle.velocity.dot(normal)))).scale(0.5D);
            }
        }
    }

    public static void render(PoseStack pose, Vector3f right, Vector3f up, float partialTick, float time) {
        if (MOTES.isEmpty())
            return;

        var matrix = pose.last().pose();
        var sparks = AstralSprites.begin(AstralSprites.SPARK);
        var scale = (float) Math.sqrt(Math.max(1D, AstralSpace.getRadius() / AstralSatchelItem.DEFAULT_RADIUS));

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        for (var particle : MOTES) {
            var position = particle.previous.lerp(particle.position, partialTick);
            var alpha = (int) (200F * (0.45F + 0.55F * Mth.sin(time * 1.6F + particle.phase)));

            AstralSprites.quad(sparks, matrix, position.x, position.y, position.z, right, up, particle.size * scale, 0F, particle.color, alpha);
        }

        AstralSprites.end(sparks);
    }

    private static Vec3 randomVelocity(double scale) {
        return new Vec3(RANDOM.nextGaussian(), RANDOM.nextGaussian(), RANDOM.nextGaussian()).scale(scale);
    }

    private static Vec3 randomInSphere(double radius) {
        var direction = new Vec3(RANDOM.nextGaussian(), RANDOM.nextGaussian(), RANDOM.nextGaussian());

        if (direction.lengthSqr() < 1.0E-6D)
            direction = new Vec3(0D, 1D, 0D);

        return direction.normalize().scale(radius * Math.cbrt(RANDOM.nextDouble()));
    }
}
