package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class ImmortalityEffect extends MobEffect {
    public ImmortalityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0X6836AA);
    }

    @EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
            if (event.getEntity().hasEffect(RelicsMobEffects.IMMORTALITY))
                event.setCanceled(true);
        }
    }
}