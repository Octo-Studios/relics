package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.entities.ShadowGlaiveModel;
import it.hurts.sskirillss.relics.entities.ShadowGlaiveEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShadowGlaiveRenderer extends EntityRenderer<ShadowGlaiveEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/entities/shadow_glaive.png");

    public ShadowGlaiveRenderer(Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(ShadowGlaiveEntity entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        float time = entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        matrixStackIn.pushPose();

        matrixStackIn.scale(1.35F, 1.35F, 1.35F);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(90F));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(time * 40F));
        matrixStackIn.translate(0F, -1F, -0.075F);

        new ShadowGlaiveModel<>().renderToBuffer(matrixStackIn, bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entity))), packedLightIn, OverlayTexture.NO_OVERLAY);

        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowGlaiveEntity entity) {
        return TEXTURE;
    }
}