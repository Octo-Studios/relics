package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

public class AntiHealEffect extends MobEffect {
    public AntiHealEffect() {
        super(MobEffectCategory.HARMFUL, 0X6836AA);
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            LivingEntity entity = event.getEntity();

            if (entity.hasEffect(RelicsMobEffects.ANTI_HEAL))
                event.setCanceled(true);
        }
    }
}