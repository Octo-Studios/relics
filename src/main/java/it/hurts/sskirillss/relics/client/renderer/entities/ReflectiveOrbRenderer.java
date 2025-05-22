package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.client.models.entities.ReflectiveOrbModel;
import it.hurts.sskirillss.relics.entities.ReflectiveOrbEntity;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReflectiveOrbRenderer extends EntityRenderer<ReflectiveOrbEntity> {
    public ReflectiveOrbRenderer(Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(ReflectiveOrbEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        var time = entityIn.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        matrixStackIn.pushPose();

        matrixStackIn.translate(0.0, 0.25, 0.0);

        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(time * 25F));

        matrixStackIn.translate(0.0, -1.25, 0.0);

        new ReflectiveOrbModel<>().renderToBuffer(matrixStackIn, bufferIn.getBuffer(RenderType.entityTranslucentCull(getTextureLocation(entityIn))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        matrixStackIn.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ReflectiveOrbEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/entities/reflective_orb.png");
    }
}