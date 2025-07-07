package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class BleedingEffect extends MobEffect {
    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0X6836AA);
    }

    @EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onLivingUpdate(EntityTickEvent.Post event) {
            if (event.getEntity() instanceof LivingEntity entity && entity.tickCount % 20 == 0 && entity.hasEffect(RelicsMobEffects.BLEEDING))
                entity.hurt(entity.level().damageSources().magic(), Math.min(10, entity.getHealth() * 0.05F));
        }
    }
}