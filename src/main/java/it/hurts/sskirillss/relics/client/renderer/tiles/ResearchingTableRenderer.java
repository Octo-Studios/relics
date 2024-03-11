package it.hurts.sskirillss.relics.client.renderer.tiles;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.tiles.ResearchingTableTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ResearchingTableRenderer implements BlockEntityRenderer<ResearchingTableTile> {
    public ResearchingTableRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ResearchingTableTile tileEntity, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        ItemStack stack = tileEntity.getStack();

        if (stack == null || stack.isEmpty())
            return;

        // TODO: REMOVE AFTER FINISHING
        List<Item> items = Lists.newArrayList(
                ItemRegistry.AMPHIBIAN_BOOT.get()
        );

        matrixStack.pushPose();

        if (items.contains(stack.getItem())) {
            matrixStack.translate(0.5F, 1.15F, 0.5F);
        } else {
            matrixStack.translate(0.5F, 0.96F, 0.5F);
            matrixStack.scale(1.25F, 1.25F, 1.25F);
            matrixStack.mulPose(Vector3f.XN.rotationDegrees(90));
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        itemRenderer.render(stack, ItemTransforms.TransformType.GROUND, true, matrixStack, buffer, combinedLight, combinedOverlay,
                itemRenderer.getModel(stack, tileEntity.getLevel(), null, 0));

        matrixStack.popPose();
    }
}