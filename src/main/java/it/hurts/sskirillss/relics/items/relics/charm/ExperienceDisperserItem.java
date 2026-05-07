package it.hurts.sskirillss.relics.items.relics.charm;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.relic.leveling.RelicExperienceChangeEvent;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;

public class ExperienceDisperserItem extends WearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("dispersion")
                                .initialMaxLevel(10)
                                .rankModifier(1, "same_item")
                                .rankModifier(3, "player_xp")
                                .rankModifier(5, "max_level")
                                .stat(AbilityStatTemplate.builder("distribution_ratio")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1619D)
                                        .formatValue(value -> MathUtils.round(value * 100D, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("same_item_bonus")
                                        .initialValue(0.15D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0857D)
                                        .formatValue(value -> MathUtils.round(value * 100D, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("player_xp_ratio")
                                        .initialValue(0.025D, 0.05D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.2571D)
                                        .formatValue(value -> MathUtils.round(value * 100D, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("trigger")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("distributions")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("distributed_experience")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("player_xp_conversions")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("converted_player_experience")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 12, 24).star(1, 4, 15).star(2, 4, 8).star(3, 9, 9).star(4, 7, 4).star(5, 14, 4).star(6, 16, 8).star(7, 13, 16)
                                        .link(0, 1).link(1, 2).link(2, 3).link(3, 4).link(4, 5).link(5, 6).link(6, 7)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(150)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.WILDCARD)
                        .build())
                .build();
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onRelicExperienceChange(RelicExperienceChangeEvent event) {
            if (event.getDelta() <= 0D)
                return;

            if ("dispersion".equals(event.getAbility()) && "trigger".equals(event.getExperienceSource()))
                return;

            if (!(event.getBearer() instanceof Player player) || player.level().isClientSide())
                return;

            var relics = getEquippedRelics(player);

            if (relics.isEmpty())
                return;

            var sourceRelicData = event.getStack().getItem() instanceof IRelicItem sourceRelic
                    ? sourceRelic.getRelicData(player, event.getStack())
                    : null;

            var sourceAtMaxLevel = sourceRelicData != null && sourceRelicData.isMaxLevel();

            for (var disperserStack : EntityUtils.findEquippedCurios(player, RelicsItems.EXPERIENCE_DISPERSER.get())) {
                var disperser = (ExperienceDisperserItem) disperserStack.getItem();
                var ability = disperser.getRelicData(player, disperserStack).getAbilitiesData().getAbilityData("dispersion");

                if (!ability.canPlayerUse(player))
                    continue;

                if (sourceAtMaxLevel && !ability.getRankModifierData("max_level").isEnabled())
                    continue;

                var ratio = ability.getStatData("distribution_ratio").getValue();
                var sameItemBonus = ability.getStatData("same_item_bonus").getValue();

                if (ratio <= 0D)
                    continue;

                var targets = new ArrayList<ItemStack>();
                var sourceExcluded = false;

                for (var relicStack : relics) {
                    if (relicStack.getItem() instanceof ExperienceDisperserItem)
                        continue;

                    if (!sourceExcluded && (relicStack == event.getStack() || ItemStack.isSameItemSameComponents(relicStack, event.getStack()))) {
                        sourceExcluded = true;
                        continue;
                    }

                    if (canReceiveExperience(player, relicStack))
                        targets.add(relicStack);
                }

                if (targets.isEmpty())
                    continue;

                var triggered = false;
                var distributed = 0D;

                for (var relicStack : targets) {
                    var transfer = event.getDelta() * ratio;

                    if (ability.getRankModifierData("same_item").isEnabled() && relicStack.getItem() == event.getStack().getItem())
                        transfer *= 1D + sameItemBonus;

                    if (transfer <= 0D || !(relicStack.getItem() instanceof IRelicItem relic))
                        continue;

                    relic.getRelicData(player, relicStack).getLevelingData().addExperience("dispersion", "trigger", transfer);

                    triggered = true;
                    distributed += transfer;
                }

                if (triggered) {
                    var stats = ability.getStatisticData();
                    var distributedMetric = stats.getMetricData("distributed_experience");
                    var before = distributedMetric.getValue();

                    distributedMetric.addValue(distributed);

                    var units = getWholeUnitDelta(before, distributedMetric.getValue());

                    if (units > 0) {
                        disperser.getRelicData(player, disperserStack).getLevelingData().addExperience("dispersion", "trigger", units);

                        stats.getMetricData("distributions").addValue(units);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerXpPickup(PlayerXpEvent.PickupXp event) {
            var player = event.getEntity();
            var orb = event.getOrb();

            if (orb.getValue() <= 0)
                return;

            if (player.level().isClientSide())
                return;

            var relics = getEquippedRelics(player);

            if (relics.isEmpty())
                return;

            for (var disperserStack : EntityUtils.findEquippedCurios(player, RelicsItems.EXPERIENCE_DISPERSER.get())) {
                var disperser = (ExperienceDisperserItem) disperserStack.getItem();
                var ability = disperser.getRelicData(player, disperserStack).getAbilitiesData().getAbilityData("dispersion");

                if (!ability.canPlayerUse(player) || !ability.getRankModifierData("player_xp").isEnabled())
                    continue;

                var ratio = ability.getStatData("player_xp_ratio").getValue();
                var targets = new ArrayList<ItemStack>();

                for (var relicStack : relics) {
                    if (relicStack.getItem() instanceof ExperienceDisperserItem)
                        continue;

                    if (canReceiveExperience(player, relicStack))
                        targets.add(relicStack);
                }

                if (targets.isEmpty())
                    continue;

                var distributed = Math.min(orb.getValue(), (int) Math.floor(orb.getValue() * ratio));

                if (distributed <= 0)
                    continue;

                var split = distributed / (double) targets.size();

                if (split <= 0D)
                    continue;

                var triggered = false;

                for (var relicStack : targets) {
                    if (!(relicStack.getItem() instanceof IRelicItem relic))
                        continue;

                    relic.getRelicData(player, relicStack).getLevelingData().addExperience("dispersion", "trigger", split);

                    triggered = true;
                }

                if (triggered) {
                    orb.value -= distributed;

                    var stats = ability.getStatisticData();
                    var convertedMetric = stats.getMetricData("converted_player_experience");
                    var before = convertedMetric.getValue();

                    convertedMetric.addValue(distributed);

                    var units = getWholeUnitDelta(before, convertedMetric.getValue());

                    if (units > 0)
                        stats.getMetricData("player_xp_conversions").addValue(units);
                }
            }
        }

        private static int getWholeUnitDelta(double before, double after) {
            var epsilon = 1.0E-9D;
            var from = (int) Math.floor(before + epsilon);
            var to = (int) Math.floor(after + epsilon);

            return Math.max(0, to - from);
        }

        private static boolean canReceiveExperience(Player player, ItemStack stack) {
            if (!(stack.getItem() instanceof IRelicItem relic))
                return false;

            var data = relic.getRelicData(player, stack);

            return !data.isMaxLevel();
        }

        private static List<ItemStack> getEquippedRelics(Player player) {
            var relics = new ArrayList<ItemStack>();

            for (var handStack : player.getHandSlots()) {
                if (handStack.getItem() instanceof IRelicItem)
                    relics.add(handStack);
            }

            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                for (int i = 0; i < handler.getEquippedCurios().getSlots(); i++) {
                    var stack = handler.getEquippedCurios().getStackInSlot(i);

                    if (stack.getItem() instanceof IRelicItem)
                        relics.add(stack);
                }
            });

            return relics;
        }
    }
}
