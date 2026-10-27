package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.items.AstralSatchelItem;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void relics$onDrop(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
        var player = (LocalPlayer) (Object) this;

        if (AstralSatchelItem.isLocked(player, player.getInventory().selected))
            cir.setReturnValue(false);
    }
}
