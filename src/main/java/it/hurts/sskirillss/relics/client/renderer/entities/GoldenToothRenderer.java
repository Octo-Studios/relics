package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.entities.GoldenToothModel;
import it.hurts.sskirillss.relics.entities.GoldenToothEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class GoldenToothRenderer extends EntityRenderer<GoldenToothEntity> {
    public GoldenToothRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(GoldenToothEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        var time = entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        poseStack.pushPose();

        var bob = (float) Math.sin(time * 0.1D) * 0.05F;

        poseStack.translate(0, bob, 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees(180F));

        poseStack.translate(0, -1.15F, -0.025);

        var rotation = (time * 20F) % 360F;

        poseStack.translate(0,0,0.02);

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        poseStack.translate(0,0,-0.02);

        var scale = 0.65F;

        poseStack.scale(scale, scale, scale);

        new GoldenToothModel<>().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenToothEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/entities/golden_tooth.png");
    }
}