package it.hurts.sskirillss.relics.client.renderer.items;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.items.KineticBeltModel;
import it.hurts.sskirillss.relics.client.models.items.KineticBeltWingsModel;
import it.hurts.sskirillss.relics.client.renderer.items.base.IRelicRenderer;
import it.hurts.sskirillss.relics.items.relics.belt.KineticBeltItem;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.awt.*;

public class KineticBeltRenderer implements IRelicRenderer {
    private final KineticBeltModel model;

    public KineticBeltRenderer() {
        this.model = new KineticBeltModel(Minecraft.getInstance().getEntityModels().bakeLayer(KineticBeltModel.LAYER));
    }

    @Override
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource bufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = slotContext.entity();
        var relic = (KineticBeltItem) stack.getItem();

        poseStack.pushPose();

        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, this.model);

        if (relic.getRelicData(entity, stack).isFlawless())
            this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/kinetic_belt_flawless.png"))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        else {
            this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/kinetic_belt.png"))), light, OverlayTexture.NO_OVERLAY);
            this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/kinetic_belt_glow.png"))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }

        if (relic.isActive(stack)) {
            var time = entity.tickCount + partialTicks;

            var shakeX = Mth.sin(time * 3F) * 0.01F;
            var shakeY = Mth.sin(time * 2F) * 0.025F;
            var shakeZ = Mth.cos(time * 3F) * 0.01F;

            poseStack.translate(shakeX, shakeY, shakeZ);

            new KineticBeltWingsModel<>().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityTranslucentCull(FlawlessUtils.getTexture(entity, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/kinetic_belt_wings.png")))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, new Color(1F, 1F, 1F, 0.9F + Mth.sin(time * 2F) * 0.1F).getRGB());
        }

        poseStack.popPose();
    }
}