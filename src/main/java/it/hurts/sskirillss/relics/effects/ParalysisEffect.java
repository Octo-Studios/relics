package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.player.Input;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

public class ParalysisEffect extends MobEffect {
    public ParalysisEffect() {
        super(MobEffectCategory.HARMFUL, 0X6836AA);
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            Player player = event.getEntity();

            if (player.hasEffect(RelicsMobEffects.PARALYSIS)) {
                Input input = event.getInput();

                input.shiftKeyDown = false;
                input.jumping = false;

                input.forwardImpulse = 0;
                input.leftImpulse = 0;
            }
        }
    }
}