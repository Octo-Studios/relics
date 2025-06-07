package it.hurts.sskirillss.relics.items.relics.necklace;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import top.theillusivec4.curios.api.SlotContext;

public class JellyfishNecklaceItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("jelly")
                                .maxLevel(10)
                                .stat(StatTemplate.builder("max_health")
                                        .initialValue(0.1D, 0.2D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("regeneration")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("shock")
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.5D, 2.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.AQUATIC)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();

        if (this.canPlayerUseAbility(entity, stack, "jelly") && entity.isInWater()) {
            EntityUtils.resetAttribute(entity, stack, Attributes.MAX_HEALTH, (float) (entity.getMaxHealth() * this.getStatValue(entity, stack, "jelly", "max_health")), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.JELLYFISH_NECKLACE.get())) {
                var relic = (JellyfishNecklaceItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "jelly"))
                    continue;

                event.setAmount((float) (event.getAmount() + (event.getAmount() * relic.getStatValue(entity, stack, "jelly", "regeneration"))));
            }
        }
    }
}