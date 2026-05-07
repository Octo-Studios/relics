package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.utility.EntityBlockSpeedFactorEvent;
import it.hurts.sskirillss.relics.items.relics.ClotOfTimeItem;
import it.hurts.sskirillss.relics.misc.mixin.FluidWalkGraceAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin implements FluidWalkGraceAccessor {
    @Unique
    private int relics$fluidWalkGraceTicks;

    @Inject(method = "baseTick()V", at = @At("TAIL"))
    private void relics$tickFluidWalkGrace(CallbackInfo ci) {
        if (relics$fluidWalkGraceTicks > 0)
            relics$fluidWalkGraceTicks--;
    }

    @Override
    public boolean relics$hasFluidWalkGrace() {
        return relics$fluidWalkGraceTicks > 0;
    }

    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
    public void getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        Entity entity = (Entity) (Object) this;

        EntityBlockSpeedFactorEvent event = new EntityBlockSpeedFactorEvent(entity, entity.level().getBlockState(entity.getOnPos()), cir.getReturnValue());

        NeoForge.EVENT_BUS.post(event);

        cir.setReturnValue(event.getSpeedFactor());
    }

    @Inject(method = "canUsePortal", at = @At("HEAD"), cancellable = true)
    private void canUsePortal(boolean allowPassengers, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof Player player))
            return;

        if (player.isUsingItem() && player.getUseItem().getItem() instanceof ClotOfTimeItem)
            cir.setReturnValue(false);
    }
}
