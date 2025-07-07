package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.entities.ShockwaveBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.neoforged.neoforge.client.model.data.ModelData;

public class ShockwaveBlockRenderer extends EntityRenderer<ShockwaveBlockEntity> {
    public ShockwaveBlockRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(ShockwaveBlockEntity entity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(entity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);

        var state = entity.getBlockState();

        if (state.getRenderShape() != RenderShape.MODEL)
            return;

        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        var model = dispatcher.getBlockModel(state);

        var pos = new BlockPos((int) entity.getX(), (int) entity.getBoundingBox().maxY, (int) entity.getZ());

        pMatrixStack.pushPose();

        pMatrixStack.translate(-0.5F, 0F, -0.5F);

        for (var renderType : model.getRenderTypes(state, RandomSource.create(), ModelData.EMPTY))
            dispatcher.getModelRenderer().tesselateBlock(entity.level(), model, state, pos, pMatrixStack, pBuffer.getBuffer(renderType), false, RandomSource.create(), state.getSeed(pos), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);

        pMatrixStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ShockwaveBlockEntity pEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}