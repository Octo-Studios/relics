package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class FlightEffect extends MobEffect {
    private static final String FLIGHT_TAG = "relics:flight_effect";

    public FlightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0X9ADDE7);
    }

    @EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            if (!(event.getEntity() instanceof Player player))
                return;

            var abilities = player.getAbilities();
            var hasEffect = player.hasEffect(RelicsMobEffects.FLIGHT);
            var hadEffect = player.getPersistentData().getBoolean(FLIGHT_TAG);

            if (hasEffect) {
                player.getPersistentData().putBoolean(FLIGHT_TAG, true);
                player.fallDistance = 0F;

                if (!abilities.mayfly || !abilities.flying) {
                    abilities.mayfly = true;
                    abilities.flying = true;
                    player.onUpdateAbilities();
                }

                return;
            }

            if (!hadEffect)
                return;

            player.getPersistentData().remove(FLIGHT_TAG);

            if (player.isCreative() || player.isSpectator())
                return;

            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }
}
