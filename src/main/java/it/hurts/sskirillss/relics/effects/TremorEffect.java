package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
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
        public static void onItemUse(LivingEntityUseItemEvent.Start event) {
            if (event.getEntity().hasEffect(RelicsMobEffects.TREMOR))
                event.setCanceled(true);
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

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onMouseInput(InputEvent.InteractionKeyMappingTriggered event) {
            var player = Minecraft.getInstance().player;

            if (player != null && player.hasEffect(RelicsMobEffects.TREMOR)) {
                event.setSwingHand(false);

                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderHand(RenderHandEvent event) {
            var player = Minecraft.getInstance().player;

            if (player != null && player.hasEffect(RelicsMobEffects.TREMOR)) {
                var poseStack = event.getPoseStack();

                float age = player.tickCount + event.getPartialTick();

                float shakeX = (float) Math.sin(age * 20) * 0.01F;
                float shakeY = (float) Math.cos(age * 20) * 0.01F;

                poseStack.translate(shakeX, shakeY, 0);
            }
        }
    }
}