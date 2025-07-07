package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    public void onRotationUpdate(float yaw, float pitch, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;

        if (player != null && player.hasEffect(RelicsMobEffects.STUN))
            ci.cancel();
    }
}