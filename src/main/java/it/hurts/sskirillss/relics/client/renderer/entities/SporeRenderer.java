package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.entities.SporeModel;
import it.hurts.sskirillss.relics.entities.SporeEntity;
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
public class SporeRenderer extends EntityRenderer<SporeEntity> {
    public SporeRenderer(Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(SporeEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        if (entityIn.tickCount < 5)
            return;

        float time = entityIn.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        poseStack.pushPose();

        var scale = (float) (Math.clamp(entityIn.tickCount * 0.0175F, 0, 0.5F) + Math.sin(time * 0.1F) * 0.05F);

        poseStack.translate(0, 0.05F, 0);

        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.YP.rotation(time * 0.25F));
        poseStack.mulPose(Axis.ZN.rotation(time * 0.25F));

        new SporeModel<>().renderToBuffer(poseStack, bufferIn.getBuffer(RenderType.entityCutout(getTextureLocation(entityIn))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SporeEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/entities/spore.png");
    }
}