package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class VanishingEffect extends MobEffect {
    public VanishingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0X6836AA);
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
            if (event.getEntity().hasEffect(EffectRegistry.VANISHING.get()))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onHandRender(RenderHandEvent event) {
            if (Minecraft.getInstance().player.hasEffect(EffectRegistry.VANISHING.get()) && event.getItemStack().isEmpty())
                event.setCanceled(true);
        }
    }
}