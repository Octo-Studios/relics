package it.hurts.sskirillss.relics.client.renderer.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.items.JellyfishNecklaceArcModel;
import it.hurts.sskirillss.relics.client.models.items.JellyfishNecklaceModel;
import it.hurts.sskirillss.relics.client.renderer.items.base.AbstractNecklaceRenderer;
import it.hurts.sskirillss.relics.items.relics.necklace.JellyfishNecklaceItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class JellyfishNecklaceRenderer extends AbstractNecklaceRenderer<LivingEntity, JellyfishNecklaceModel> {
    public JellyfishNecklaceRenderer() {
        super(() -> new JellyfishNecklaceModel(Minecraft.getInstance().getEntityModels().bakeLayer(JellyfishNecklaceModel.LAYER)), ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/jellyfish_necklace.png"));
    }

    @Override
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource bufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        super.render(stack, slotContext, poseStack, parent, bufferSource, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);

        if (!(stack.getItem() instanceof JellyfishNecklaceItem relic))
            return;

        var entity = slotContext.entity();

        var time = entity.tickCount + partialTicks;

        var count = relic.getRings(stack);
        var spinSpeed = 15F;

        for (int i = 0; i < count; i++) {
            var phase = 2D * Math.PI * i / count;

            poseStack.pushPose();

            ICurioRenderer.rotateIfSneaking(poseStack, entity);
            ICurioRenderer.translateIfSneaking(poseStack, entity);

            poseStack.mulPose(Axis.YP.rotationDegrees((float) (time * spinSpeed + phase * 180 / Math.PI)));

            var shakeX = (float) Math.sin(time * 1.5F + phase) * 2.5F;
            var shakeZ = (float) Math.cos(time * 2F + phase) * 2.5F;

            poseStack.mulPose(Axis.XP.rotationDegrees(shakeX));
            poseStack.mulPose(Axis.ZP.rotationDegrees(shakeZ));

            var scale = 1F + (float) Math.sin(time * 0.75F + phase) * 0.075F;

            poseStack.scale(scale, scale, scale);

            new JellyfishNecklaceArcModel<>().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(this.getFlawlessOrDefaultTexture(entity, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/jellyfish_necklace_arc_" + ((int) (time % 4) + 1) + ".png")))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }
    }

    @Override
    protected float getJellyIntensity() {
        return 0.5F;
    }

    @Override
    protected float getMovementSwingIntensity() {
        return 0.4F;
    }
}