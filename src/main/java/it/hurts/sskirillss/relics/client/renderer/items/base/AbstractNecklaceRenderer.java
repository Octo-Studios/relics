package it.hurts.sskirillss.relics.client.renderer.items.base;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.function.Supplier;

public abstract class AbstractNecklaceRenderer<T extends LivingEntity, M extends EntityModel<?> & INecklaceModel<T>> implements ICurioRenderer {
    private final ResourceLocation texture;
    private final M model;

    protected AbstractNecklaceRenderer(Supplier<M> modelSupplier, ResourceLocation texture) {
        this.model = modelSupplier.get();

        this.texture = texture;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource buf, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        poseStack.pushPose();

        model.prepareMobModel((T) player, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim((T) player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(player, (HumanoidModel<LivingEntity>) model);

        model.getBodyPart().translateAndRotate(poseStack);

        poseStack.translate(0, 1, 0.015F);

        var vc = ItemRenderer.getArmorFoilBuffer(buf, RenderType.entityTranslucentCull(texture), stack.hasFoil());

        var neck = model.getBodyPart().getChild("neck");
        var pendant = model.getBodyPart().getChild("pendant");

        neck.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);

        var dx = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
        var dy = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
        var dz = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());

        var bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        var sinYaw = Math.sin(bodyYaw * (Math.PI / 180D));
        var cosYaw = -Math.cos(bodyYaw * (Math.PI / 180D));

        var forwardMotion = Mth.clamp((float) (dx * sinYaw + dz * cosYaw) * 100F, -80F, 80F);
        var verticalMotion = Mth.clamp((float) dy * 10F, -6F, 32F);
        var sideMotion = Mth.clamp((float) (dx * cosYaw - dz * sinYaw) * 100F, -20F, 20F);
        var pressMotion = forwardMotion > 0 ? forwardMotion * 0.1F : forwardMotion * 0.3F;
        var liftMotion = verticalMotion > 0 ? -verticalMotion * 2F : -verticalMotion * 0.05F;

        var rawDegX = pressMotion + liftMotion;
        var rawDegY = sideMotion * 0.7F;
        var nlDegX = Math.signum(rawDegX) * (float) Math.pow(Math.abs(rawDegX), 1.2F);
        var nlDegY = Math.signum(rawDegY) * (float) Math.pow(Math.abs(rawDegY), 1.2F);
        var radX = nlDegX * ((float) Math.PI / 180F);
        var radY = nlDegY * ((float) Math.PI / 180F);

        pendant.xRot = radX;
        pendant.yRot = radY;
        pendant.zRot = 0F;

        var t = ageInTicks + partialTicks;

        pendant.xRot += (Mth.sin(t * 0.1F) + Mth.sin(t * 0.3F)) * 0.02F;
        pendant.yRot += (Mth.cos(t * 0.1F) + Mth.cos(t * 0.3F)) * 0.02F;

        var swing = player.getAttackAnim(partialTicks);

        if (swing > 0F)
            pendant.yRot -= Mth.sin(swing * (float) Math.PI) * 0.35F;

        pendant.render(poseStack, vc, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}