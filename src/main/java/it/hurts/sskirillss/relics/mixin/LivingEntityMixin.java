package it.hurts.sskirillss.relics.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.hurts.sskirillss.relics.api.events.utility.LivingSlippingEvent;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.belt.KineticBeltItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.PacketSyncEntityEffects;
import it.hurts.sskirillss.relics.utils.RelicStacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void onJumpFromGround(CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        var scale = RelicStacks.forStat(entity, RelicsItems.KINETIC_BELT.get(), "gliding", "efficiency")
                .filter(entry -> ((KineticBeltItem) entry.relic()).isActive(entry.stack()))
                .max("efficiency");

        if (scale > 0D)
            entity.setDeltaMovement(entity.getDeltaMovement().scale(1D + scale));
    }

    @ModifyConstant(method = "travel", constant = @Constant(floatValue = 0.91F, ordinal = 1))
    private float removeAirDrag(float original) {
        var entity = (LivingEntity) (Object) this;

        if (entity instanceof Player player && (player.getAbilities().flying || player.isSpectator()))
            return original;

        var maxValue = 0.985F;
        var diff = maxValue - original;

        var scale = (float) RelicStacks.forStat(entity, RelicsItems.KINETIC_BELT.get(), "gliding", "efficiency")
                .filter(entry -> {
                    var relic = (KineticBeltItem) entry.relic();

                    return !relic.isLanded(entry.stack()) || relic.isActive(entry.stack());
                }).max("efficiency");

        return original + (diff * scale);
    }

    @ModifyVariable(method = "travel", name = "f2", index = 8, ordinal = 0, at = @At("STORE"))
    protected float setBlockFriction(float original) {
        LivingEntity entity = (LivingEntity) (Object) this;

        LivingSlippingEvent event = new LivingSlippingEvent(entity, entity.getCommandSenderWorld().getBlockState(entity.getOnPos()), original);

        NeoForge.EVENT_BUS.post(event);

        return event.getFriction();
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isImmobile()Z"))
    protected boolean onAiStep(boolean original) {
        var entity = (LivingEntity) (Object) this;

        if (entity.hasEffect(RelicsMobEffects.STUN))
            return true;

        if (entity.hasEffect(RelicsMobEffects.PARALYSIS))
            return true;

        return original;
    }

    @Inject(method = "onEffectAdded", at = @At("TAIL"))
    protected void onEffectAdded(MobEffectInstance effect, Entity target, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        Level level = entity.getCommandSenderWorld();

        if (level.isClientSide())
            return;

        NetworkHandler.sendToClientsTrackingEntity(new PacketSyncEntityEffects((CompoundTag) effect.save(), PacketSyncEntityEffects.Action.ADD, entity.getId()), entity);
    }

    @Inject(method = "onEffectUpdated", at = @At("TAIL"))
    protected void onEffectUpdated(MobEffectInstance effect, boolean forced, Entity target, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        Level level = entity.getCommandSenderWorld();

        if (level.isClientSide())
            return;

        NetworkHandler.sendToClientsTrackingEntity(new PacketSyncEntityEffects((CompoundTag) effect.save(), PacketSyncEntityEffects.Action.UPDATE, entity.getId()), entity);
    }

    @Inject(method = "onEffectRemoved", at = @At("TAIL"))
    protected void onEffectRemoved(MobEffectInstance effect, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        Level level = entity.getCommandSenderWorld();

        if (level.isClientSide())
            return;

        NetworkHandler.sendToClientsTrackingEntity(new PacketSyncEntityEffects((CompoundTag) effect.save(), PacketSyncEntityEffects.Action.REMOVE, entity.getId()), entity);
    }

    @Inject(method = "canBeSeenByAnyone", at = @At("HEAD"), cancellable = true)
    protected void canBeSeenByAnyone(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.hasEffect(RelicsMobEffects.VANISHING))
            cir.setReturnValue(false);
    }
}
