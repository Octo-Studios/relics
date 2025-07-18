package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    public void onKeyPress(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;

        if (player != null && player.hasEffect(RelicsMobEffects.STUN))
            ci.cancel();
    }
}