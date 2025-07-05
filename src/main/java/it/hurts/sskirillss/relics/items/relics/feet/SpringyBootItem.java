package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class SpringyBootItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("bounce")
                                .rankModifier(1, "disappearance")
                                .rankModifier(3, "strike")
                                .rankModifier(4, "shockwave")
                                .stat(StatTemplate.builder("power")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff8a5610)
                                .borderBottom(0xff275504)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.MOUNTAIN)
                        .build())
                .build();
    }

    public boolean isLeaped(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.SPRINGY_BOOT_LEAPED, false);
    }

    public void setLeaped(ItemStack stack, boolean leaped) {
        stack.set(DataComponentRegistry.SPRINGY_BOOT_LEAPED, leaped);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
            var entity = event.getEntity();
            var level = entity.level();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.SPRINGY_BOOT.get())) {
                var relic = (SpringyBootItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "bounce") || relic.isLeaped(stack) || !entity.isShiftKeyDown())
                    continue;

                var angle = entity.getLookAngle();

                if (angle.y() < 0F)
                    angle = new Vec3(angle.x(), 0F, angle.z());

                entity.setDeltaMovement(entity.getDeltaMovement().add(angle.add(0F, 0.25F, 0F).normalize().scale(relic.getStatValue(entity, stack, "bounce", "power"))));

                relic.setLeaped(stack, true);

                var speed = entity.getDeltaMovement().length();

                level.playSound(null, entity.blockPosition(), SoundRegistry.SPRING_BOING.get(), SoundSource.MASTER, (float) Math.min(2F, 0.25F + speed * 0.5F), (float) Math.max(0.1F, 2F - speed * 0.75F));
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();
            var level = entity.level();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.SPRINGY_BOOT.get())) {
                var relic = (SpringyBootItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "bounce") || !relic.isLeaped(stack))
                    continue;


            }
        }
    }
}