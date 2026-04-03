package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.entities.ConstellationStarModel;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.FallingStarEntity;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class FallingStarRenderer extends EntityRenderer<FallingStarEntity> {
    public FallingStarRenderer(Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(FallingStarEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();

        poseStack.scale(1.5F, 1.5F, 1.5F);
        poseStack.translate(0, -1, 0);

        new ConstellationStarModel<>().renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        var random = RandomSource.create(432L);
        var vertexConsumer = buffers.getBuffer(RenderType.dragonRays());
        var time = entity.tickCount + partialTicks;

        poseStack.pushPose();
        poseStack.translate(0.0F, 1.1F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 4.0F));

        for (var i = 0; i < 40; ++i) {
            poseStack.pushPose();

            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F + time * 2.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F + time * 4.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + time * 3.0F));

            var length = random.nextFloat() * 0.5F + 0.5F;
            var width = random.nextFloat() * 0.1F + 0.075F;
            var matrix = poseStack.last().pose();

            var coreColor = FlawlessUtils.getColor(entity.isFlawless(), new Color(255, 255, 255, 255));
            var beamColor = FlawlessUtils.getColor(entity.isFlawless(), new Color(0, 150, 255, 0));

            vertexConsumer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(coreColor.getRed(), coreColor.getGreen(), coreColor.getBlue(), coreColor.getAlpha());
            vertexConsumer.addVertex(matrix, -0.866F * width, length, -0.5F * width).setColor(beamColor.getRed(), beamColor.getGreen(), beamColor.getBlue(), beamColor.getAlpha());
            vertexConsumer.addVertex(matrix, 0.866F * width, length, -0.5F * width).setColor(beamColor.getRed(), beamColor.getGreen(), beamColor.getBlue(), beamColor.getAlpha());

            vertexConsumer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(coreColor.getRed(), coreColor.getGreen(), coreColor.getBlue(), coreColor.getAlpha());
            vertexConsumer.addVertex(matrix, 0.866F * width, length, -0.5F * width).setColor(beamColor.getRed(), beamColor.getGreen(), beamColor.getBlue(), beamColor.getAlpha());
            vertexConsumer.addVertex(matrix, 0.0F, length, width).setColor(beamColor.getRed(), beamColor.getGreen(), beamColor.getBlue(), beamColor.getAlpha());

            vertexConsumer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(coreColor.getRed(), coreColor.getGreen(), coreColor.getBlue(), coreColor.getAlpha());
            vertexConsumer.addVertex(matrix, 0.0F, length, width).setColor(beamColor.getRed(), beamColor.getGreen(), beamColor.getBlue(), beamColor.getAlpha());
            vertexConsumer.addVertex(matrix, -0.866F * width, length, -0.5F * width).setColor(beamColor.getRed(), beamColor.getGreen(), beamColor.getBlue(), beamColor.getAlpha());
            poseStack.popPose();
        }

        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FallingStarEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/entities/constellation_star" + (entity.isFlawless() ? "_flawless" : "") + ".png");
    }
}