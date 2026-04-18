package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.hurts.sskirillss.relics.entities.ChainedElectricityEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ChainedElectricityRenderer extends EntityRenderer<ChainedElectricityEntity> {
    public ChainedElectricityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ChainedElectricityEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var previous = entity.getPreviousEntity();

        if (previous == null || !previous.isAlive())
            return;

        var currentPos = new Vec3(
                Mth.lerp(partialTicks, entity.xo, entity.getX()),
                Mth.lerp(partialTicks, entity.yo, entity.getY()),
                Mth.lerp(partialTicks, entity.zo, entity.getZ())
        );
        var previousPos = new Vec3(
                Mth.lerp(partialTicks, previous.xo, previous.getX()),
                Mth.lerp(partialTicks, previous.yo, previous.getY()),
                Mth.lerp(partialTicks, previous.zo, previous.getZ())
        );

        var start = new Vec3(0D, entity.getBbHeight() * 0.5D, 0D);
        var end = previousPos.subtract(currentPos).add(0D, previous.getBbHeight() * 0.5D, 0D);
        var delta = end.subtract(start);
        var length = delta.length();

        if (length < 0.01D || length > 10D)
            return;

        var direction = delta.normalize();
        var tangent = direction.cross(new Vec3(0D, 1D, 0D));

        if (tangent.lengthSqr() < 1.0E-6D)
            tangent = direction.cross(new Vec3(1D, 0D, 0D));

        tangent = tangent.normalize();
        var bitangent = direction.cross(tangent).normalize();

        var pulse = 0.7F + 0.3F * Mth.sin((entity.tickCount + partialTicks) * 2.4F);
        var coreColor = entity.isFlawless() ? new int[]{255, 215, 120} : new int[]{150, 220, 255};
        var auraColor = entity.isFlawless() ? new int[]{255, 170, 40} : new int[]{70, 130, 255};
        var now = entity.level().getGameTime();
        var seed = (entity.getId() * 73428767L) ^ (previous.getId() * 912931L) ^ (now / 2L);
        var random = new Random(seed);

        poseStack.pushPose();

        var lightningConsumer = buffer.getBuffer(RenderType.lightning());

        var points = this.buildLightningPath(start, end, tangent, bitangent, random, Math.max(8, (int) (length * 5D)));
        var frames = this.buildSmoothFrames(points);
        var coreBaseHalfWidth = (0.022F + 0.012F * pulse) * 1.25F;
        var coreHalfWidths = this.buildHalfWidths(points.length, coreBaseHalfWidth);
        var outlineHalfWidths = this.buildHalfWidths(points.length, coreBaseHalfWidth * 1.9F);
        var impulsePos = (float) ((entity.tickCount + partialTicks) * 0.17D % 1D);
        var impulseSigma = 0.11F;

        for (var i = 0; i < points.length - 1; i++) {
            var a = points[i];
            var b = points[i + 1];
            var segmentDelta = b.subtract(a);

            if (segmentDelta.lengthSqr() < 1.0E-6D)
                continue;

            var tMid = (i + 0.5F) / Math.max(1F, points.length - 1F);
            var centerGradient = 0.7F + 0.3F * (float) Math.sin(Math.PI * tMid);
            var impulseDist = Math.abs(tMid - impulsePos);
            var wrappedDist = Math.min(impulseDist, 1F - impulseDist);
            var impulse = (float) Math.exp(-(wrappedDist * wrappedDist) / (2F * impulseSigma * impulseSigma));
            var intensity = Math.clamp(centerGradient + impulse * 0.95F, 0.4F, 1.85F);
            var segmentCoreColor = this.scaleColor(coreColor, intensity);
            var segmentAuraColor = this.scaleColor(auraColor, 0.8F + impulse * 0.8F);
            var coreAlpha = (int) Math.clamp((185F * pulse) * (0.85F + intensity * 0.45F), 20F, 255F);
            var outlineAlpha = (int) Math.clamp((70F * pulse) * (0.7F + intensity * 0.35F), 8F, 210F);

            this.tubeSection(lightningConsumer, poseStack, a, b, frames[i].tangent(), frames[i].bitangent(), frames[i + 1].tangent(), frames[i + 1].bitangent(), coreHalfWidths[i], coreHalfWidths[i + 1], segmentCoreColor[0], segmentCoreColor[1], segmentCoreColor[2], coreAlpha);
            this.tubeSection(lightningConsumer, poseStack, a, b, frames[i].tangent(), frames[i].bitangent(), frames[i + 1].tangent(), frames[i + 1].bitangent(), outlineHalfWidths[i], outlineHalfWidths[i + 1], segmentAuraColor[0], segmentAuraColor[1], segmentAuraColor[2], outlineAlpha);
        }

        poseStack.popPose();
    }

    private Vec3[] buildLightningPath(Vec3 start, Vec3 end, Vec3 tangent, Vec3 bitangent, Random random, int segments) {
        var points = new Vec3[segments + 1];

        for (var i = 0; i <= segments; i++) {
            var t = i / (double) segments;
            var base = start.lerp(end, t);

            if (i == 0 || i == segments) {
                points[i] = base;
                continue;
            }

            var envelope = Math.sin(Math.PI * t);
            var jitter = 0.22D * envelope;
            var jt = (random.nextDouble() * 2D - 1D) * jitter;
            var jb = (random.nextDouble() * 2D - 1D) * jitter;

            points[i] = base.add(tangent.scale(jt)).add(bitangent.scale(jb));
        }

        return points;
    }

    private float[] buildHalfWidths(int pointCount, float baseWidth) {
        var widths = new float[pointCount];

        for (var i = 0; i < pointCount; i++) {
            var t = i / (float) Math.max(1, pointCount - 1);
            var taper = 1F - 0.65F * Math.abs(t - 0.5F);

            widths[i] = baseWidth * taper;
        }

        return widths;
    }

    private Frame[] buildSmoothFrames(Vec3[] points) {
        var frames = new Frame[points.length];
        var directions = new Vec3[points.length];

        for (var i = 0; i < points.length; i++) {
            Vec3 direction;

            if (i == 0)
                direction = points[1].subtract(points[0]);
            else if (i == points.length - 1)
                direction = points[points.length - 1].subtract(points[points.length - 2]);
            else
                direction = points[i + 1].subtract(points[i - 1]);

            if (direction.lengthSqr() < 1.0E-6D)
                direction = i > 0 ? directions[i - 1] : new Vec3(0D, 1D, 0D);

            directions[i] = direction.normalize();
        }

        var firstFrame = this.buildFrame(directions[0]);
        var tangent = firstFrame[0];
        var bitangent = firstFrame[1];

        frames[0] = new Frame(directions[0], tangent, bitangent);

        for (var i = 1; i < points.length; i++) {
            var direction = directions[i];
            var projectedTangent = tangent.subtract(direction.scale(tangent.dot(direction)));

            if (projectedTangent.lengthSqr() < 1.0E-6D)
                projectedTangent = bitangent.subtract(direction.scale(bitangent.dot(direction)));

            if (projectedTangent.lengthSqr() < 1.0E-6D)
                projectedTangent = this.buildFrame(direction)[0];

            tangent = projectedTangent.normalize();
            bitangent = direction.cross(tangent).normalize();

            frames[i] = new Frame(direction, tangent, bitangent);
        }

        return frames;
    }

    private Vec3[] buildFrame(Vec3 direction) {
        var tangent = direction.cross(new Vec3(0D, 1D, 0D));

        if (tangent.lengthSqr() < 1.0E-6D)
            tangent = direction.cross(new Vec3(1D, 0D, 0D));

        tangent = tangent.normalize();

        return new Vec3[]{tangent, direction.cross(tangent).normalize()};
    }

    private void tubeSection(VertexConsumer consumer, PoseStack poseStack, Vec3 start, Vec3 end, Vec3 startTangent, Vec3 startBitangent, Vec3 endTangent, Vec3 endBitangent, float startHalfWidth, float endHalfWidth, int red, int green, int blue, int alpha) {
        var startRing = this.buildRing(start, startTangent, startBitangent, startHalfWidth);
        var endRing = this.buildRing(end, endTangent, endBitangent, endHalfWidth);

        for (var side = 0; side < 4; side++) {
            var next = (side + 1) & 3;

            this.quad(consumer, poseStack, startRing[side], endRing[side], endRing[next], startRing[next], red, green, blue, alpha);
        }
    }

    private Vec3[] buildRing(Vec3 center, Vec3 tangent, Vec3 bitangent, float halfWidth) {
        var ta = tangent.scale(halfWidth);
        var ba = bitangent.scale(halfWidth);

        return new Vec3[]{
                center.add(ta).add(ba),
                center.subtract(ta).add(ba),
                center.subtract(ta).subtract(ba),
                center.add(ta).subtract(ba)
        };
    }

    private void quad(VertexConsumer consumer, PoseStack poseStack, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, int red, int green, int blue, int alpha) {
        var pose = poseStack.last();

        consumer.addVertex(pose, (float) p0.x, (float) p0.y, (float) p0.z).setColor(red, green, blue, alpha).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, (float) p1.x, (float) p1.y, (float) p1.z).setColor(red, green, blue, alpha).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, (float) p2.x, (float) p2.y, (float) p2.z).setColor(red, green, blue, alpha).setLight(LightTexture.FULL_BRIGHT);
        consumer.addVertex(pose, (float) p3.x, (float) p3.y, (float) p3.z).setColor(red, green, blue, alpha).setLight(LightTexture.FULL_BRIGHT);
    }

    private int[] scaleColor(int[] color, float multiplier) {
        return new int[]{
                (int) Math.clamp(color[0] * multiplier, 0F, 255F),
                (int) Math.clamp(color[1] * multiplier, 0F, 255F),
                (int) Math.clamp(color[2] * multiplier, 0F, 255F)
        };
    }

    @Override
    public boolean shouldRender(ChainedElectricityEntity entity, @Nonnull Frustum clipping, double x, double y, double z) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(ChainedElectricityEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private record Frame(Vec3 direction, Vec3 tangent, Vec3 bitangent) {
    }
}
