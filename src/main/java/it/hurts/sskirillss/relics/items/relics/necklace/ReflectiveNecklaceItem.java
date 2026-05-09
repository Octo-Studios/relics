package it.hurts.sskirillss.relics.items.relics.necklace;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.entities.ReflectiveOrbEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
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
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Comparator;

public class ReflectiveNecklaceItem extends WearableRelicItem {
    public static final int ORB_SEARCH_RADIUS = 16;

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("reflection")
                                .initialMaxLevel(10)
                                .rankModifier(1, "stun")
                                .rankModifier(3, "piercing")
                                .rankModifier(5, "bounce")
                                .stat(AbilityStatTemplate.builder("chance")
                                        .initialValue(0.1D, 0.25D)
                                        .thresholdValue(0D, 1D)
                                        .targetValue(RelicsScalingModels.RADICAL.get(), 1.00016D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 2.49996D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("lifetime")
                                        .initialValue(5D, 10D)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 60.04761D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("piercings")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 19.9995D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2.50025D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bounces")
                                        .initialValue(1D, 2D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 49.999D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("construct")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("impact")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("total_orbs")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("total_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("total_stun")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("stun", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("total_bounces")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("bounce", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 17, 6).star(1, 4, 11).star(2, 16, 17).star(3, 9, 19).star(4, 18, 24).star(5, 4, 27)
                                        .link(1, 3).link(3, 2).link(3, 4).link(0, 3).link(3, 5)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.NETHER_LIKE, LootEntries.THE_NETHER)
                        .build())
                .build();
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            var source = event.getSource().getEntity();
            var damage = event.getOriginalDamage();
            var entity = event.getEntity();

            var level = entity.level();
            var random = level.getRandom();

            if (damage >= 1) {
                for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.REFLECTIVE_NECKLACE.get())) {
                    var relic = (ReflectiveNecklaceItem) stack.getItem();

                    if (level.getRandom().nextDouble() > relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getStatData("chance").getValue())
                        continue;

                    var orbDamage = Math.clamp((float) (damage * relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getStatData("damage").getValue()), Float.MIN_VALUE, Float.MAX_VALUE);

                    var orb = new ReflectiveOrbEntity(RelicsEntities.REFLECTIVE_ORB.get(), level);

                    orb.setPiercings(relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getRankModifierData("piercing").isEnabled() ? (int) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getStatData("piercings").getValue() : 0);
                    orb.setBounces(relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getRankModifierData("bounce").isEnabled() ? (int) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getStatData("bounces").getValue() : 0);
                    orb.setStun(relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getRankModifierData("stun").isEnabled() ? (int) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getStatData("stun").getValue() : 0);
                    orb.setLifetime((int) (relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getStatData("lifetime").getValue() * 20));
                    orb.setFlawless(relic.getRelicData(entity, stack).isVisuallyFlawless());
                    orb.setPos(entity.getEyePosition());
                    orb.setDamage(orbDamage);
                    orb.setOwner(entity);
                    orb.setStack(stack);

                    if (source != null)
                        orb.setDeltaMovement(entity.position().subtract(source.position()).normalize().add(MathUtils.randomFloat(random) * 0.5D, 0, MathUtils.randomFloat(random) * 0.5D));
                    else
                        orb.setDeltaMovement(MathUtils.randomFloat(random) * 0.5D, 0.5D + random.nextFloat() * 0.25D, MathUtils.randomFloat(random) * 0.5D);

                    level.addFreshEntity(orb);

                    relic.getRelicData(entity, stack).getLevelingData().addExperience("reflection", "construct", orbDamage * 0.1D);

                    relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("reflection").getStatisticData().getMetricData("total_orbs").addValue(1);
                }
            }

            if (!(event.getSource().getEntity() instanceof LivingEntity livingSource))
                return;

            var stack = EntityUtils.findEquippedCurio(livingSource, RelicsItems.REFLECTIVE_NECKLACE.get());

            if (!stack.isEmpty()) {
                var step = 0;

                for (var orb : level.getEntitiesOfClass(ReflectiveOrbEntity.class, livingSource.getBoundingBox().inflate(ORB_SEARCH_RADIUS)).stream()
                        .filter(orb -> !orb.isTargeted() && orb.getOwner() instanceof LivingEntity owner && owner.getStringUUID().equals(livingSource.getStringUUID()))
                        .sorted(Comparator.comparingInt(orb -> (int) orb.position().distanceTo(entity.position())))
                        .toList()) {

                    ServerScheduler.schedule(step++ * 2, () -> orb.setTarget(entity.position().add(0D, entity.getBbHeight(), 0D).add(entity.getKnownMovement())));
                }
            }
        }
    }
}
