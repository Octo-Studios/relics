package it.hurts.sskirillss.relics.items.relics.necklace;

import it.hurts.sskirillss.relics.api.relics.MetricTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.StatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.ReflectiveOrbEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Comparator;

public class ReflectiveNecklaceItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("reflection")
                                .maxLevel(10)
                                .stat(StatTemplate.builder("chance")
                                        .initialValue(0.1D, 0.2D)
                                        .thresholdValue(0D, 1D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("lifetime")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(7.5D, 15D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("interval")
                                        .initialValue(1D, 0.75D)
                                        .thresholdValue(0, Double.MAX_VALUE)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), -0.09D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(200)
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff00baff)
                                .borderBottom(0xff0090a9)
                                .textured(true)
                                .build())
                        .build())
                .statistic(StatisticTemplate.builder()
                        .metric(MetricTemplate.builder("use_time")
                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.NETHER_LIKE, LootEntries.THE_NETHER)
                        .build())
                .build();
    }

    @EventBusSubscriber(modid = Reference.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onEntityHurt(LivingDamageEvent.Pre event) {
            var damage = event.getOriginalDamage();
            var source = event.getSource().getEntity();
            var entity = event.getEntity();
            var level = entity.level();
            var random = level.getRandom();

            if (damage >= 1) {
                for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.REFLECTIVE_NECKLACE.get())) {
                    var relic = (ReflectiveNecklaceItem) stack.getItem();

                    if (level.getRandom().nextDouble() > relic.getStatValue(entity, stack, "reflection", "chance"))
                        continue;

                    var orb = new ReflectiveOrbEntity(RelicsEntities.REFLECTIVE_ORB.get(), level);

                    orb.setDamage(Math.clamp((float) (damage * relic.getStatValue(entity, stack, "reflection", "damage")), Float.MIN_VALUE, Float.MAX_VALUE));
                    orb.setLifetime((int) (relic.getStatValue(entity, stack, "reflection", "lifetime") * 20));
                    orb.setPos(entity.getEyePosition());
                    orb.setOwner(entity);

                    if (source != null)
                        orb.setDeltaMovement(entity.position().subtract(source.position()).normalize().add(MathUtils.randomFloat(random) * 0.5D, 0, MathUtils.randomFloat(random) * 0.5D));
                    else
                        orb.setDeltaMovement(MathUtils.randomFloat(random) * 0.5D, 0.5D + random.nextFloat() * 0.25D, MathUtils.randomFloat(random) * 0.5D);

                    level.addFreshEntity(orb);
                }
            }

            var stack = EntityUtils.findEquippedCurio(source, RelicsItems.REFLECTIVE_NECKLACE.get());

            var step = 0;

            if (source != null && !stack.isEmpty()) {
                var relic = (ReflectiveNecklaceItem) stack.getItem();

                for (var orb : level.getEntitiesOfClass(ReflectiveOrbEntity.class, source.getBoundingBox().inflate(32D)).stream()
                        .filter(orb -> !orb.isTargeted() && orb.getOwner() instanceof LivingEntity owner && owner.getStringUUID().equals(source.getStringUUID()))
                        .sorted(Comparator.comparingInt(orb -> (int) orb.position().distanceTo(entity.position())))
                        .toList()) {
                    orb.setTarget(entity);
                    orb.setTargeted(true);
                    orb.setDelay((int) (step++ * (relic.getStatValue(entity, stack, "reflection", "interval")) * 20));
                    orb.setMotion(entity.position().add(0D, entity.getBbHeight() / 2D, 0D).subtract(orb.position()).scale(1.25D).normalize());
                }
            }
        }
    }
}