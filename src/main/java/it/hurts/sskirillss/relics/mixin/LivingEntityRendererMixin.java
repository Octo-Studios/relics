package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
    @Inject(method = "isShaking(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    public void isShaking(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity.hasEffect(RelicsMobEffects.TREMOR))
            cir.setReturnValue(true);
    }
}