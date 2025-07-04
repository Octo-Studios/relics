package it.hurts.sskirillss.relics.client.renderer.items.base;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
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

public abstract class AbstractNecklaceRenderer<T extends LivingEntity, M extends EntityModel<?> & INecklaceModel<T>> implements ICurioRenderer, IRelicRenderer {
    private final M model;
    private final ResourceLocation texture;

    protected AbstractNecklaceRenderer(Supplier<M> modelSupplier, ResourceLocation texture) {
        this.model = modelSupplier.get();
        this.texture = texture;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource bufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        var relic = (IRelicItem) stack.getItem();

        poseStack.pushPose();

        this.model.prepareMobModel((T) player, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim((T) player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(player, (HumanoidModel<LivingEntity>) this.model);

        this.model.getBodyPart().translateAndRotate(poseStack);

        poseStack.translate(0.0F, 1.0F, 0.015F);

        var vertexConsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.entityTranslucentCull(this.getFlawlessOrDefaultTexture(player, stack, texture)), stack.hasFoil());

        this.model.getBodyPart().getChild("neck").render(poseStack, vertexConsumer, relic.isRelicFlawless(player, stack) ? LightTexture.FULL_BRIGHT : light, OverlayTexture.NO_OVERLAY);

        var deltaX = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
        var deltaY = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
        var deltaZ = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());

        var verticalMotion = Mth.clamp(deltaY * 10F, -6F, 32F);
        var bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);

        var sin = (float) Math.sin(bodyYaw);
        var cos = (float) -Math.cos(bodyYaw);

        var forwardMotion = Mth.clamp((deltaX * sin + deltaZ * cos) * 100F, -80F, 80F);
        var sideMotion = Mth.clamp((deltaX * cos - deltaZ * sin) * 100F, -20F, 20F);

        var pressTilt = forwardMotion > 0 ? forwardMotion * 0.1F : forwardMotion * 0.3F;
        var liftTilt = verticalMotion > 0 ? -verticalMotion * 2F : -verticalMotion * 0.05F;

        var rawX = pressTilt + liftTilt;
        var rawY = sideMotion * 0.7F;

        var nlX = (float) Math.signum(rawX) * (float) Math.pow(Math.abs(rawX), 1.2F);
        var nlY = (float) Math.signum(rawY) * (float) Math.pow(Math.abs(rawY), 1.2F);

        var rotX = nlX * ((float) Math.PI / 180F);
        var rotY = nlY * ((float) Math.PI / 180F);

        var pendant = this.model.getBodyPart().getChild("pendant");

        poseStack.pushPose();

        var px = pendant.x / 16F;
        var py = pendant.y / 16F;
        var pz = pendant.z / 16F;

        poseStack.translate(px, py, pz);

        var rawJ = Mth.clamp(verticalMotion * 0.035F, -0.3F, 0.3F);
        var jelly = (float) (-rawJ * this.getJellyIntensity());

        poseStack.scale(1F - jelly * (jelly < 0 ? 3F : 1), 1F + jelly * 3F, 1F - jelly * (jelly < 0 ? 3F : 1));

        poseStack.translate(-px, -py, -pz);

        var mi = this.getMovementSwingIntensity();

        pendant.xRot = rotX * mi;
        pendant.yRot = rotY * mi;
        pendant.zRot = 0F;

        var t = ageInTicks + partialTicks;
        var idleI = this.getIdleSwingIntensity();

        pendant.xRot += (Mth.sin(t * 0.1F) + Mth.sin(t * 0.3F)) * idleI;
        pendant.yRot += (Mth.cos(t * 0.1F) + Mth.cos(t * 0.3F)) * idleI;

        var atk = player.getAttackAnim(partialTicks);

        if (atk > 0F)
            pendant.yRot -= Mth.sin(atk * (float) Math.PI) * 0.35F;

        pendant.render(poseStack, vertexConsumer, relic.isRelicFlawless(player, stack) ? LightTexture.FULL_BRIGHT : light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();

        poseStack.popPose();
    }

    protected float getJellyIntensity() {
        return 0F;
    }

    protected float getMovementSwingIntensity() {
        return 0.9F;
    }

    protected float getIdleSwingIntensity() {
        return 0.05F;
    }
}