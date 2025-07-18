package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void onKeyPress(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (key == GLFW.GLFW_KEY_ESCAPE)
            return;

        var player = Minecraft.getInstance().player;

        if (player != null && player.hasEffect(RelicsMobEffects.STUN))
            ci.cancel();
    }
}