package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.entities.LeafCoreModel;
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

public class LeavesBlockRenderer extends EntityRenderer<LeavesBlockEntity> {
    public LeavesBlockRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(LeavesBlockEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        var state = entity.getBlockState();

        if (state.getRenderShape() != RenderShape.MODEL)
            return;

        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        var model = dispatcher.getBlockModel(state);

        var pos = new BlockPos((int) entity.getX(), (int) entity.getBoundingBox().maxY, (int) entity.getZ());

        var time = entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        poseStack.pushPose();

        poseStack.translate(0F, 0.35F, 0F);

        poseStack.mulPose(Axis.YN.rotationDegrees(time * 20));
        poseStack.mulPose(Axis.XN.rotationDegrees(time * 20));

        poseStack.translate(0F, -1.35F, 0F);

        new LeafCoreModel<>().renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        poseStack.pushPose();

        poseStack.scale(0.65F, 0.65F, 0.65F);

        poseStack.translate(0F, 0.575F, 0F);

        poseStack.mulPose(Axis.YP.rotationDegrees(time * 20));
        poseStack.mulPose(Axis.XP.rotationDegrees(time * 20));

        poseStack.translate(-0.5F, -0.5F, -0.5F);

        for (var renderType : model.getRenderTypes(state, RandomSource.create(), ModelData.EMPTY))
            dispatcher.getModelRenderer().tesselateBlock(entity.level(), model, state, pos, poseStack, buffer.getBuffer(renderType), false, entity.getRandom(), state.getSeed(pos), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(LeavesBlockEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/entities/leaf_core" + (entity.isFlawless() ? "_flawless" : "") + ".png");
    }
}