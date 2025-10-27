package it.hurts.sskirillss.relics.mixin.shakes;

import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    public void getFieldOfViewModifier(CallbackInfoReturnable<Float> cir) {
        var modifier = 0F;

        for (var effect : ShakeManager.SHAKES.values())
            modifier += effect.getShakeFOV((AbstractClientPlayer) (Object) this, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true));

        if (modifier != 0F)
            cir.setReturnValue(cir.getReturnValue() + modifier);
    }
}