package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

public class VanishingEffect extends MobEffect {
    public VanishingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0X6836AA);
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
            if (event.getEntity().hasEffect(RelicsMobEffects.VANISHING))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onHandRender(RenderHandEvent event) {
            if (Minecraft.getInstance().player.hasEffect(RelicsMobEffects.VANISHING) && event.getItemStack().isEmpty())
                event.setCanceled(true);
        }
    }
}