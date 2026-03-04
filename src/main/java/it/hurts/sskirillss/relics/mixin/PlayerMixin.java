package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.items.relics.RiderFluteItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", shift = At.Shift.BEFORE), cancellable = true)
    private void relics$interceptRiderFluteCapture(Entity entityToInteractOn, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(entityToInteractOn instanceof LivingEntity livingEntity))
            return;

        var player = (Player) (Object) this;
        var stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof RiderFluteItem item))
            return;

        var result = item.interactLivingEntity(stack, player, livingEntity, hand);

        if (result.consumesAction())
            cir.setReturnValue(result);
    }
}