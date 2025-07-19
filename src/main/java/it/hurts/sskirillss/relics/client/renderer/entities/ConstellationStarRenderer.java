package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.entities.ConstellationStarModel;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.ConstellationStarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ConstellationStarRenderer extends EntityRenderer<ConstellationStarEntity> {
    public ConstellationStarRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(ConstellationStarEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        var time = entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        poseStack.pushPose();

        var baseY = -0.85F;

        poseStack.translate(0, baseY, 0);

        var bob = (float) Math.sin(time * 0.1D) * 0.05F;

        poseStack.translate(0, bob, 0);

        var jitterX = (float) Math.sin(time * 0.5D) * 0.02F;
        var jitterZ = (float) Math.cos(time * 0.7D) * 0.02F;

        poseStack.translate(jitterX, 0, jitterZ);

        var rotation = (time * 30F) % 360F;

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

        var scale = 1F + (float) Math.sin(time * 0.3D) * 0.1F;

        poseStack.scale(scale, scale, scale);

        new ConstellationStarModel<>().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ConstellationStarEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/entities/constellation_star" + (entity.isFlawless() ? "_flawless" : "") + ".png");
    }
}