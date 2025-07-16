package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.api.events.common.LivingSlippingEvent;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.belt.KineticBeltItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.PacketSyncEntityEffects;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
        var motion = entity.getDeltaMovement();

        for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.KINETIC_BELT.get())) {
            var relic = (KineticBeltItem) stack.getItem();

            if (!relic.canPlayerUseAbility(entity, stack, "gliding") || relic.getAbilityMode(entity, stack, "gliding").equals("disabled") || !relic.isActive(stack))
                continue;

            var scale = relic.getStatValue(entity, stack, "gliding", "efficiency");

            entity.setDeltaMovement(motion.add(motion.scale(scale)));
        }
    }

    @ModifyConstant(method = "travel", constant = @Constant(floatValue = 0.91F, ordinal = 1))
    private float removeAirDrag(float original) {
        var entity = (LivingEntity) (Object) this;

        var maxValue = 0.985F;
        var diff = maxValue - original;

        var scale = (float) EntityUtils.findEquippedCurios(entity, RelicsItems.KINETIC_BELT.get()).stream()
                .filter(stack -> {
                    var relic = ((KineticBeltItem) stack.getItem());

                    return relic.canPlayerUseAbility(entity, stack, "gliding") && relic.getAbilityMode(entity, stack, "gliding").equals("disabled") && !relic.isLanded(stack) && relic.isActive(stack);
                })
                .mapToDouble(stack -> ((KineticBeltItem) stack.getItem()).getStatValue(entity, stack, "gliding", "efficiency"))
                .max()
                .orElse(0D);

        return original + (diff * scale);
    }

    @ModifyVariable(method = "travel", name = "f2", index = 8, ordinal = 0, at = @At("STORE"))
    protected float setBlockFriction(float original) {
        LivingEntity entity = (LivingEntity) (Object) this;

        LivingSlippingEvent event = new LivingSlippingEvent(entity, entity.getCommandSenderWorld().getBlockState(entity.getOnPos()), original);

        NeoForge.EVENT_BUS.post(event);

        return event.getFriction();
    }

    @Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
    protected void onAiStep(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.hasEffect(RelicsMobEffects.STUN))
            cir.setReturnValue(true);

        if (entity.hasEffect(RelicsMobEffects.PARALYSIS))
            cir.setReturnValue(true);
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