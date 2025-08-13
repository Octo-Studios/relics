package it.hurts.sskirillss.relics.items.relics.necklace;

import com.google.common.collect.Lists;
import it.hurts.sskirillss.relics.api.relics.MetricTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.StatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.ElectricSparkEntity;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JellyfishNecklaceItem extends RelicItem {

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("regeneration")
                                .initialMaxLevel(10)
                                .rankModifier(3, "retention")
                                .stat(StatTemplate.builder("max_health")
                                        .initialValue(0.1D, 0.2D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.3628D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("regeneration")
                                        .initialValue(0.15D, 0.35D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.4604D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("health_regeneration")
                                                .build())
                                        .build())
                                .statistic(StatisticTemplate.builder()
                                        .metric(MetricTemplate.builder("health_regenerated")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 10).star(1, 3, 9).star(2, 19, 9).star(3, 2, 16).star(4, 11, 16).star(5, 20, 16).star(6, 11, 24)
                                        .link(0, 2).link(2, 5).link(5, 6).link(6, 3).link(3, 1).link(1, 0).link(0, 4).link(4, 6).link(3, 4).link(4, 5)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("shock")
                                .initialMaxLevel(10)
                                .rankModifier(1, "conductor")
                                .rankModifier(5, "charge")
                                .modes("enabled", "disabled")
                                .stat(StatTemplate.builder("cooldown")
                                        .initialValue(120D, 60D)
                                        .thresholdValue(0, Double.MAX_VALUE)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), -13.9528D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("rings")
                                        .initialValue(1D, 2D)
                                        .thresholdValue(0, Double.MAX_VALUE)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.2286D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.RADICAL.get(), 0.7606D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("knockback")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.0471D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("paralysis")
                                        .initialValue(1D, 2.5D)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.0404D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("distance")
                                        .initialValue(2.5D, 5D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 4.1858D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("bounces")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.2095D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(2.5D, 5D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 4.1858D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage_modifier")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.0680D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(2.5D, 5D)
                                        .upgradeModifier(RelicsScalingModels.RADICAL.get(), 4.2258D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("rings_accumulating")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("arcs_bouncing")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("hit_paralysis")
                                                .rankModifierCondition("charge")
                                                .build())
                                        .build())
                                .statistic(StatisticTemplate.builder()
                                        .metric(MetricTemplate.builder("rings_accumulated")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(MetricTemplate.builder("rings_paralysis")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(MetricTemplate.builder("arcs_spawned")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(MetricTemplate.builder("arcs_bounces")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(MetricTemplate.builder("arcs_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(MetricTemplate.builder("hit_paralysis")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .abilityRankModifierVisibilityCondition("charge")
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 4).star(1, 4, 11).star(2, 11, 11).star(3, 18, 11).star(4, 11, 18).star(5, 6, 21).star(6, 16, 21).star(7, 5, 24).star(8, 17, 24).star(9, 4, 27).star(10, 18, 27).star(11, 8, 29).star(12, 14, 29)
                                        .link(4, 12).link(12, 11).link(11, 4).link(5, 6).link(7, 8).link(9, 10).link(0, 3).link(3, 4).link(4, 1).link(1, 0).link(0, 2).link(2, 4).link(1, 2).link(2, 3)
                                        .build())
                                .build())
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff00baff)
                                .borderBottom(0xff0090a9)
                                .textured(true)
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.AQUATIC)
                        .build())
                .build();
    }

    public int getCooldown(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.JELLYFISH_NECKLACE_COOLDOWN, 0);
    }

    public void setCooldown(ItemStack stack, int cooldown) {
        stack.set(RelicsDataComponents.JELLYFISH_NECKLACE_COOLDOWN, Math.max(0, cooldown));
    }

    public void addCooldown(ItemStack stack, int cooldown) {
        this.setCooldown(stack, this.getCooldown(stack) + cooldown);
    }

    public List<String> getDamagedEntities(ItemStack stack) {
        return new ArrayList<>(stack.getOrDefault(RelicsDataComponents.JELLYFISH_NECKLACE_DAMAGED_ENTITIES, new ArrayList<>()));
    }

    public void setDamagedEntities(ItemStack stack, List<String> targets) {
        stack.set(RelicsDataComponents.JELLYFISH_NECKLACE_DAMAGED_ENTITIES, targets);
    }

    public void addDamagedEntities(ItemStack stack, String... targets) {
        var list = this.getDamagedEntities(stack);

        list.addAll(Lists.newArrayList(targets));

        this.setDamagedEntities(stack, list);
    }

    public void removeDamagedEntities(ItemStack stack, String... targets) {
        var list = this.getDamagedEntities(stack);

        list.removeAll(Lists.newArrayList(targets));

        this.setDamagedEntities(stack, list);
    }

    public int getDuration(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.JELLYFISH_NECKLACE_DURATION, 0);
    }

    public void setDuration(ItemStack stack, int duration) {
        stack.set(RelicsDataComponents.JELLYFISH_NECKLACE_DURATION, Math.max(0, duration));
    }

    public void addDuration(ItemStack stack, int duration) {
        this.setDuration(stack, this.getDuration(stack) + duration);
    }

    public int getRings(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.JELLYFISH_NECKLACE_RINGS, 0);
    }

    public void setRings(ItemStack stack, int rings) {
        stack.set(RelicsDataComponents.JELLYFISH_NECKLACE_RINGS, Math.max(0, rings));
    }

    public void addRings(ItemStack stack, int rings) {
        this.setRings(stack, this.getRings(stack) + rings);
    }

    public List<String> getAffectedEntities(ItemStack stack) {
        return new ArrayList<>(stack.getOrDefault(RelicsDataComponents.JELLYFISH_NECKLACE_AFFECTED_ENTITIES, new ArrayList<>()));
    }

    public void setAffectedEntities(ItemStack stack, List<String> targets) {
        stack.set(RelicsDataComponents.JELLYFISH_NECKLACE_AFFECTED_ENTITIES, targets);
    }

    public void addAffectedEntities(ItemStack stack, String... targets) {
        var list = this.getAffectedEntities(stack);

        list.addAll(Lists.newArrayList(targets));

        this.setAffectedEntities(stack, list);
    }

    public void removeAffectedEntities(ItemStack stack, String... targets) {
        var list = this.getAffectedEntities(stack);

        list.removeAll(Lists.newArrayList(targets));

        this.setAffectedEntities(stack, list);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();
        var level = entity.level();

        if (this.canPlayerUseAbility(entity, stack, "regeneration")) {
            var multiplier = (float) this.getStatValue(entity, stack, "regeneration", "max_health");

            if (entity.isInLiquid() || entity.isInRain()) {
                var boostedMaxHealth = entity.getMaxHealth() * multiplier;

                EntityUtils.resetAttribute(entity, stack, Attributes.MAX_HEALTH, multiplier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                EntityUtils.removeAttribute(entity, stack, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE);

                entity.setHealth(entity.getHealth() + Math.min(entity.getAbsorptionAmount(), boostedMaxHealth));
            } else {
                EntityUtils.removeAttribute(entity, stack, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

                if (this.isAbilityRankModifierUnlocked(entity, stack, "regeneration", "retention") && !EntityUtils.hasAttribute(entity, stack, Attributes.MAX_HEALTH)
                        && !EntityUtils.hasAttribute(entity, stack, Attributes.MAX_ABSORPTION)) {
                    var baseMaxHealth = entity.getMaxHealth();
                    var currentHealth = entity.getHealth();
                    var extraHealth = Math.max(0F, currentHealth - baseMaxHealth);

                    entity.setHealth(Math.min(currentHealth, baseMaxHealth));

                    EntityUtils.resetAttribute(entity, stack, Attributes.MAX_ABSORPTION, extraHealth, AttributeModifier.Operation.ADD_VALUE);

                    entity.setAbsorptionAmount(extraHealth);
                }
            }
        }

        if (this.canPlayerUseAbility(entity, stack, "shock") && !this.getAbilityMode(entity, stack, "shock").equals("disabled")) {
            var cooldown = this.getCooldown(stack);
            var rings = this.getRings(stack);

            if (rings < this.getStatValue(entity, stack, "shock", "rings")) {
                var maxCooldown = (int) this.getStatValue(entity, stack, "shock", "cooldown");

                if (cooldown > 0)
                    this.addCooldown(stack, -1);
                else {
                    this.addRings(stack, 1);
                    this.setCooldown(stack, maxCooldown * 20);

                    if (!level.isClientSide()) {
                        this.addAbilityMetricValue(entity, stack, "shock", "rings_accumulated", 1);

                        if (this.canAddRelicExperience(entity, stack, "shock", "rings_accumulating"))
                            this.addRelicExperience(entity, stack, "shock", "rings_accumulating", 1);
                    }
                }
            }

            var affectedEntities = this.getAffectedEntities(stack);

            Predicate<LivingEntity> predicate = entry -> {
                var uuid = entry.getStringUUID();

                return !uuid.equals(entity.getStringUUID()) && !affectedEntities.contains(uuid);
            };

            var collidedEntities = level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(0.5F), predicate);

            if (rings > 0 && !collidedEntities.isEmpty()) {
                var radius = this.getStatValue(entity, stack, "shock", "radius");

                for (var target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(0.5F).inflate(radius), predicate)) {
                    var diff = target.position().add(0, target.getBbHeight() / 2D, 0).subtract(entity.position());
                    var knockback = 0.5D * this.getStatValue(entity, stack, "shock", "knockback");
                    var paralysis = this.getStatValue(entity, stack, "shock", "paralysis");

                    target.addEffect(new MobEffectInstance(RelicsMobEffects.PARALYSIS, (int) (paralysis * 20), 0, false, true));

                    if (!level.isClientSide())
                        this.addAbilityMetricValue(entity, stack, "shock", "rings_paralysis", paralysis);

                    target.setDeltaMovement(diff.normalize().multiply(knockback, knockback / 2F, knockback));

                    var spark = new ElectricSparkEntity(RelicsEntities.ELECTRIC_SPARK.get(), level);

                    spark.setDamageModifier(this.isAbilityRankModifierUnlocked(entity, stack, "shock", "conductor") ? (float) this.getStatValue(entity, stack, "shock", "damage_modifier") : 0F);
                    spark.setDistance((float) this.getStatValue(entity, stack, "shock", "distance"));
                    spark.setBounces((int) this.getStatValue(entity, stack, "shock", "bounces"));
                    spark.setDamage((float) this.getStatValue(entity, stack, "shock", "damage"));
                    spark.setPos(entity.position().add(0F, entity.getBbHeight() / 2F, 0D));
                    spark.setFlawless(this.isRelicFlawless(entity, stack));
                    spark.setTarget(target);
                    spark.setOwner(entity);
                    spark.setStack(stack);

                    level.addFreshEntity(spark);

                    this.addAffectedEntities(stack, target.getStringUUID());

                    if (!level.isClientSide())
                        this.addAbilityMetricValue(entity, stack, "shock", "arcs_spawned", 1);
                }

                this.addRings(stack, -1);

                if (this.isAbilityRankModifierUnlocked(entity, stack, "shock", "charge")) {
                    var maxDuration = (int) this.getStatValue(entity, stack, "shock", "duration");

                    this.setDuration(stack, maxDuration * 20);
                }
            }

            if (!affectedEntities.isEmpty())
                this.removeAffectedEntities(stack, affectedEntities.stream()
                        .filter(Predicate.not(collidedEntities.stream()
                                .map(Entity::getStringUUID)
                                .collect(Collectors.toSet())::contains))
                        .toArray(String[]::new));

            if (this.isAbilityRankModifierUnlocked(entity, stack, "shock", "charge")) {
                var duration = this.getDuration(stack);

                if (duration > 0)
                    this.addDuration(stack, -1);
                else
                    this.setDamagedEntities(stack, new ArrayList<>());
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (stack.getItem() == newStack.getItem())
            return;

        var entity = slotContext.entity();

        EntityUtils.removeAttribute(entity, stack, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE);
        EntityUtils.removeAttribute(entity, stack, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var entity = event.getEntity();
            var level = entity.level();

            var pos = entity.blockPosition();

            // WHY!? (cuz of thread locks lol)
            if (!level.hasChunkAt(pos) || !level.isLoaded(pos))
                return;

            if (entity.isInLiquid() || entity.isInRain()) {
                for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.JELLYFISH_NECKLACE.get())) {
                    var relic = (JellyfishNecklaceItem) stack.getItem();

                    if (!relic.canPlayerUseAbility(entity, stack, "regeneration"))
                        continue;

                    var health = Math.min(entity.getMaxHealth(), event.getAmount() * relic.getStatValue(entity, stack, "regeneration", "regeneration"));

                    event.setAmount((float) (event.getAmount() + health));

                    if (!level.isClientSide()) {
                        relic.addAbilityMetricValue(entity, stack, "regeneration", "health_regenerated", health);

                        if (relic.canAddRelicExperience(entity, stack, "regeneration", "health_regeneration"))
                            relic.addRelicExperience(entity, stack, "regeneration", "health_regeneration", health);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getDirectEntity() instanceof LivingEntity entity))
                return;

            var level = entity.level();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.JELLYFISH_NECKLACE.get())) {
                var relic = (JellyfishNecklaceItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "shock") || relic.getAbilityMode(entity, stack, "shock").equals("disabled")
                        || !relic.isAbilityRankModifierUnlocked(entity, stack, "shock", "charge"))
                    continue;

                var duration = relic.getDuration(stack);

                if (duration <= 0)
                    continue;

                var targets = relic.getDamagedEntities(stack);
                var target = event.getEntity();
                var uuid = target.getStringUUID();

                if (!targets.contains(uuid)) {
                    var paralysis = relic.getStatValue(entity, stack, "shock", "paralysis");

                    target.addEffect(new MobEffectInstance(RelicsMobEffects.PARALYSIS, (int) (paralysis * 20), 0, false, false));

                    if (!level.isClientSide()) {
                        relic.addAbilityMetricValue(entity, stack, "shock", "hit_paralysis", paralysis);

                        if (relic.canAddRelicExperience(entity, stack, "shock", "hit_paralysis"))
                            relic.addRelicExperience(entity, stack, "shock", "hit_paralysis", paralysis);
                    }

                    relic.addDamagedEntities(stack, uuid);
                }
            }
        }
    }
}