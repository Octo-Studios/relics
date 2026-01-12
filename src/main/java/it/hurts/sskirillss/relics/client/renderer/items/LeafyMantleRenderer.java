package it.hurts.sskirillss.relics.client.renderer.items;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.items.LeafyMantleModel;
import it.hurts.sskirillss.relics.client.models.items.MidnightMantleModel;
import it.hurts.sskirillss.relics.client.renderer.items.base.IRelicRenderer;
import it.hurts.sskirillss.relics.items.relics.back.LeafyMantleItem;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
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

public class LeafyMantleRenderer implements IRelicRenderer {
    private final LeafyMantleModel model;

    public LeafyMantleRenderer() {
        this.model = new LeafyMantleModel(Minecraft.getInstance().getEntityModels().bakeLayer(LeafyMantleModel.LAYER));
    }

    @Override
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource bufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = slotContext.entity();

        var relic = (LeafyMantleItem) stack.getItem();

        poseStack.pushPose();

        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, this.model);

        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(FlawlessUtils.getTexture(entity, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/leafy_mantle.png")))), relic.getRelicData(entity, stack).isFlawless() ? LightTexture.FULL_BRIGHT : light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}