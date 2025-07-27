package it.hurts.sskirillss.relics.items.relics.necklace;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.ReflectiveOrbEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Comparator;

public class ReflectiveNecklaceItem extends RelicItem {
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
                                .stat(StatTemplate.builder("chance")
                                        .initialValue(0.1D, 0.2D)
                                        .thresholdValue(0D, 1D)
                                        .upgradeModifier(RelicsScalingModels.RADICAL.get(), 0.135D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.558D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("lifetime")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.053D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("piercings")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.162D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.114D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("bounces")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.471D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff00baff)
                                .borderBottom(0xff0090a9)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.NETHER_LIKE, LootEntries.THE_NETHER)
                        .build())
                .build();
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onEntityHurt(LivingDamageEvent.Pre event) {
            var source = event.getSource().getEntity();
            var damage = event.getOriginalDamage();
            var entity = event.getEntity();

            var level = entity.level();
            var random = level.getRandom();

            if (damage >= 1) {
                for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.REFLECTIVE_NECKLACE.get())) {
                    var relic = (ReflectiveNecklaceItem) stack.getItem();

                    if (level.getRandom().nextDouble() > relic.getStatValue(entity, stack, "reflection", "chance"))
                        continue;

                    var orb = new ReflectiveOrbEntity(RelicsEntities.REFLECTIVE_ORB.get(), level);

                    orb.setPiercings(relic.isAbilityRankModifierUnlocked(entity, stack, "reflection", "piercing") ? (int) relic.getStatValue(entity, stack, "reflection", "piercings") : 0);
                    orb.setBounces(relic.isAbilityRankModifierUnlocked(entity, stack, "reflection", "bounce") ? (int) relic.getStatValue(entity, stack, "reflection", "bounces") : 0);
                    orb.setStun(relic.isAbilityRankModifierUnlocked(entity, stack, "reflection", "stun") ? (int) relic.getStatValue(entity, stack, "reflection", "stun") : 0);
                    orb.setDamage(Math.clamp((float) (damage * relic.getStatValue(entity, stack, "reflection", "damage")), Float.MIN_VALUE, Float.MAX_VALUE));
                    orb.setLifetime((int) (relic.getStatValue(entity, stack, "reflection", "lifetime") * 20));
                    orb.setFlawless(relic.isRelicFlawless(entity, stack));
                    orb.setPos(entity.getEyePosition());
                    orb.setOwner(entity);

                    if (source != null)
                        orb.setDeltaMovement(entity.position().subtract(source.position()).normalize().add(MathUtils.randomFloat(random) * 0.5D, 0, MathUtils.randomFloat(random) * 0.5D));
                    else
                        orb.setDeltaMovement(MathUtils.randomFloat(random) * 0.5D, 0.5D + random.nextFloat() * 0.25D, MathUtils.randomFloat(random) * 0.5D);

                    level.addFreshEntity(orb);
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