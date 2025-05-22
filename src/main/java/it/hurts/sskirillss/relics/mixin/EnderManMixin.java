package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public class EnderManMixin {
    @Inject(at = @At(value = "HEAD"), method = "isLookingAtMe", cancellable = true)
    protected void calmEndermans(Player player, CallbackInfoReturnable<Boolean> info) {
        if (!EntityUtils.findEquippedCurio(player, RelicsItems.ENDER_HAND.get()).isEmpty()) info.setReturnValue(false);
    }
}