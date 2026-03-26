package it.hurts.sskirillss.relics.items.relics.head;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class ChefHatItem extends WearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("satiety")
                                .initialMaxLevel(10)
                                .rankModifier(1, "quick_meal")
                                .rankModifier(3, "meatball_healing")
                                .rankModifier(5, "cooked_meatball")
                                .stat(AbilityStatTemplate.builder("chance")
                                        .initialValue(0.1D, 0.15D)
                                        .thresholdValue(0D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1143D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("healing")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1143D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("drop")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("consume")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("healing")
                                                .rankModifierVisibilityState("meatball_healing", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("meatballs_dropped")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("raw_meatballs_eaten")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("cooked_meatballs_eaten")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("health_restored")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("meatball_healing", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 3, 12).star(1, 11, 14).star(2, 18, 9).star(3, 17, 13).star(4, 4, 17).star(5, 12, 7)
                                        .link(2, 3).link(3, 1).link(0, 4).link(4, 1).link(5, 0).link(5, 2)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.NETHER_LIKE, LootEntries.THE_NETHER)
                        .build())
                .build();
    }

    public static boolean canQuicklyEatMeatballs(LivingEntity entity) {
        if (entity == null)
            return false;

        for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.CHEF_HAT.get())) {
            if (!(stack.getItem() instanceof ChefHatItem relic))
                continue;

            var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("satiety");

            if (abilityData.canPlayerUse(entity) && abilityData.isRankModifierUnlocked("quick_meal"))
                return true;
        }

        return false;
    }

    public static void onMeatballConsumed(LivingEntity entity, boolean cooked) {
        if (entity == null)
            return;

        double maxHealing = 0D;
        ChefHatItem healingRelic = null;
        ItemStack healingStack = ItemStack.EMPTY;
        var consumedMetric = cooked ? "cooked_meatballs_eaten" : "raw_meatballs_eaten";

        for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.CHEF_HAT.get())) {
            if (!(stack.getItem() instanceof ChefHatItem relic))
                continue;

            var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("satiety");

            if (!abilityData.canPlayerUse(entity))
                continue;

            relic.getRelicData(entity, stack).getLevelingData().addExperience("satiety", "consume", 1);
            abilityData.getStatisticData().getMetricData(consumedMetric).addValue(1);

            if (!abilityData.isRankModifierUnlocked("meatball_healing"))
                continue;

            var healing = abilityData.getStatData("healing").getValue();

            if (cooked)
                healing *= 2D;

            if (healing <= maxHealing)
                continue;

            maxHealing = healing;
            healingRelic = relic;
            healingStack = stack;
        }

        if (maxHealing <= 0D || healingStack.isEmpty())
            return;

        var abilityData = healingRelic.getRelicData(entity, healingStack).getAbilitiesData().getAbilityData("satiety");
        var before = entity.getHealth();

        entity.heal((float) maxHealing);

        var restored = Math.max(0F, entity.getHealth() - before);

        if (restored <= 0F)
            return;

        healingRelic.getRelicData(entity, healingStack).getLevelingData().addExperience("satiety", "healing", restored);
        abilityData.getStatisticData().getMetricData("health_restored").addValue(restored);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            var target = event.getEntity();

            var source = event.getSource().getEntity() instanceof Player player ? player : null;

            if (source == null && target.getKillCredit() instanceof Player player)
                source = player;

            if (source == null || source.level().isClientSide())
                return;

            var random = source.getRandom();
            var fireAspectHolder = source.level().holderLookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT);
            var cookedByFireAspect = event.getSource().is(DamageTypeTags.IS_PLAYER_ATTACK)
                    && source.getMainHandItem().getEnchantmentLevel(fireAspectHolder) > 0;
            var cookedByFire = target.getRemainingFireTicks() > 0
                    || event.getSource().is(DamageTypeTags.IS_FIRE)
                    || cookedByFireAspect;

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.CHEF_HAT.get())) {
                var relic = (ChefHatItem) stack.getItem();
                var abilityData = relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("satiety");

                if (!abilityData.canPlayerUse(source))
                    continue;

                var chance = abilityData.getStatData("chance").getValue();

                if (chance <= 0D)
                    continue;

                if (random.nextDouble() > chance)
                    continue;

                var cookedDrop = abilityData.isRankModifierUnlocked("cooked_meatball") || cookedByFire;
                var item = cookedDrop
                        ? RelicsItems.COOKED_MEATBALL.get()
                        : RelicsItems.RAW_MEATBALL.get();

                target.spawnAtLocation(new ItemStack(item, 1));

                relic.getRelicData(source, stack).getLevelingData().addExperience("satiety", "drop", 1);
                abilityData.getStatisticData().getMetricData("meatballs_dropped").addValue(1);
            }
        }
    }
}