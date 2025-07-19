package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.common.EntityBlockSpeedFactorEvent;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class TremorEffect extends MobEffect {
    public TremorEffect() {
        super(MobEffectCategory.HARMFUL, 0X6836AA);
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingAttack(EntityInvulnerabilityCheckEvent event) {
            if (event.getOriginalInvulnerability())
                return;

            if (event.getSource().getDirectEntity() instanceof LivingEntity entity && entity.hasEffect(RelicsMobEffects.TREMOR))
                event.setInvulnerable(true);
        }

        @SubscribeEvent
        public static void onBlockBreakSpeed(PlayerEvent.BreakSpeed event) {
            if (event.getEntity().hasEffect(RelicsMobEffects.TREMOR))
                event.setNewSpeed(0F);
        }

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            if (event.getPlayer().hasEffect(RelicsMobEffects.TREMOR))
                event.setCanceled(true);
        }
    }
}