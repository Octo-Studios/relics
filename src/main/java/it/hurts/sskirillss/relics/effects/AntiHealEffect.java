package it.hurts.sskirillss.relics.effects;

import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.BlockPos;
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

    @EventBusSubscriber(modid = Reference.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var entity = event.getEntity();
            var level = entity.level();

            var box = entity.getBoundingBox();

            var min = BlockPos.containing(Math.floor(box.minX) - 1, Math.floor(box.minY), Math.floor(box.minZ) - 1);
            var max = BlockPos.containing(Math.floor(box.maxX) + 1, Math.floor(entity.getEyeY()) + 1, Math.floor(box.maxZ) + 1);

            // WHY!? (cuz of thread locks lol)
            if (!level.hasChunksAt(min, max))
                return;

            if (entity.hasEffect(EffectRegistry.ANTI_HEAL))
                event.setCanceled(true);
        }
    }
}