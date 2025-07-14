package it.hurts.sskirillss.relics.client.renderer.items;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.items.MidnightMantleModel;
import it.hurts.sskirillss.relics.client.renderer.items.base.IRelicRenderer;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
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

public class MidnightMantleRenderer implements ICurioRenderer, IRelicRenderer {
    private final MidnightMantleModel model;

    public MidnightMantleRenderer() {
        this.model = new MidnightMantleModel(Minecraft.getInstance().getEntityModels().bakeLayer(MidnightMantleModel.LAYER));
    }

    @Override
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource bufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = slotContext.entity();
        var relic = (MidnightMantleItem) stack.getItem();

        var time = entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        poseStack.pushPose();

        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, this.model);

        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(this.getFlawlessOrDefaultTexture(entity, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/midnight_mantle_" + relic.getAbilityMode(entity, stack, "phase") + ".png")))), relic.isRelicFlawless(entity, stack) ? LightTexture.FULL_BRIGHT : light, OverlayTexture.NO_OVERLAY);

        float flicker = 0.75F + 0.25F * Mth.sin(time * 0.25F);

        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(this.getFlawlessOrDefaultTexture(entity, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/midnight_mantle_stars_" + relic.getAbilityMode(entity, stack, "phase") + ".png")))), LightTexture.pack((int) (15 * flicker), (int) (15 * flicker)), OverlayTexture.NO_OVERLAY);

//        if (relic.isActive(stack)) {
//            var time = entity.tickCount + partialTicks;
//
//            var shakeX = Mth.sin(time * 3F) * 0.01F;
//            var shakeY = Mth.sin(time * 2F) * 0.025F;
//            var shakeZ = Mth.cos(time * 3F) * 0.01F;
//
//            poseStack.translate(shakeX, shakeY, shakeZ);
//
//            new KineticBeltWingsModel<>().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityTranslucentCull(this.getFlawlessOrDefaultTexture(entity, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/kinetic_belt_wings.png")))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, new Color(1F,1F,1F,0.9F + Mth.sin(time * 2F) * 0.1F).getRGB());
//        }

        poseStack.popPose();
    }
}