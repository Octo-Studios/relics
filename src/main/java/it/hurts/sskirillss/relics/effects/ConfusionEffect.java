package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.player.Input;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

public class ConfusionEffect extends MobEffect {
    public ConfusionEffect() {
        super(MobEffectCategory.HARMFUL, 0X6836AA);
    }

    @EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onMovementInput(MovementInputUpdateEvent event) {
            Player player = event.getEntity();

            if (player.hasEffect(RelicsMobEffects.CONFUSION)) {
                Input input = event.getInput();

                input.leftImpulse *= -1;
                input.forwardImpulse *= -1;
            }
        }
    }
}