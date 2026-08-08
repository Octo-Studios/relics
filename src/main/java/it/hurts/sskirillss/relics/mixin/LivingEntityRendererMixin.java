package it.hurts.sskirillss.relics.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.post_effects.misc.EntityGlitchMask;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow
    protected M model;
    @Final
    @Shadow
    protected List<RenderLayer<T, M>> layers;

    @Inject(method = "isShaking(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    public void isShaking(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.hasEffect(RelicsMobEffects.TREMOR))
            cir.setReturnValue(true);
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.BEFORE)
    )
    private void relics$renderGlitchMask(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        var animation = this.relics$getLayerAnimation(entity, partialTicks);

        EntityGlitchMask.render(entity, this.model, this.layers, ((LivingEntityRenderer<T, M>) (Object) this).getTextureLocation(entity), poseStack, packedLight,
                animation.limbSwing(), animation.limbSwingAmount(), partialTicks, animation.ageInTicks(), animation.netHeadYaw(), animation.headPitch());
    }

    private RelicsLayerAnimation relics$getLayerAnimation(T entity, float partialTicks) {
        var shouldSit = entity.isPassenger() && entity.getVehicle() != null && entity.getVehicle().shouldRiderSit();
        var bodyYaw = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        var headYaw = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        var netHeadYaw = headYaw - bodyYaw;

        if (shouldSit && entity.getVehicle() instanceof LivingEntity vehicle) {
            bodyYaw = Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot);
            netHeadYaw = headYaw - bodyYaw;

            var wrappedYaw = Mth.wrapDegrees(netHeadYaw);

            if (wrappedYaw < -85.0F)
                wrappedYaw = -85.0F;

            if (wrappedYaw >= 85.0F)
                wrappedYaw = 85.0F;

            bodyYaw = headYaw - wrappedYaw;

            if (wrappedYaw * wrappedYaw > 2500.0F)
                bodyYaw += wrappedYaw * 0.2F;

            netHeadYaw = headYaw - bodyYaw;
        }

        var headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
            headPitch *= -1.0F;
            netHeadYaw *= -1.0F;
        }

        netHeadYaw = Mth.wrapDegrees(netHeadYaw);

        var limbSwing = 0.0F;
        var limbSwingAmount = 0.0F;

        if (!shouldSit && entity.isAlive()) {
            limbSwingAmount = entity.walkAnimation.speed(partialTicks);
            limbSwing = entity.walkAnimation.position(partialTicks);

            if (entity.isBaby())
                limbSwing *= 3.0F;

            if (limbSwingAmount > 1.0F)
                limbSwingAmount = 1.0F;
        }

        return new RelicsLayerAnimation(limbSwing, limbSwingAmount, entity.tickCount + partialTicks, netHeadYaw, headPitch);
    }

    private record RelicsLayerAnimation(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
