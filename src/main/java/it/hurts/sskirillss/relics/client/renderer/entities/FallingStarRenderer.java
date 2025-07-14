package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.entities.FallingStarModel;
import it.hurts.sskirillss.relics.client.models.entities.LeafCoreModel;
import it.hurts.sskirillss.relics.entities.FallingStarEntity;
import it.hurts.sskirillss.relics.entities.LeavesBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.neoforged.neoforge.client.model.data.ModelData;

public class FallingStarRenderer extends EntityRenderer<FallingStarEntity> {
    public FallingStarRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(FallingStarEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        var time = entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

//        poseStack.pushPose();
//
//        poseStack.translate(0,-0.725,0);
//
//        new FallingStarModel<>().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
//
//        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(FallingStarEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/entities/falling_star" + (entity.isFlawless() ? "_flawless" : "") + ".png");
    }
}