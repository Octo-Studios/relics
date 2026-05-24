package it.hurts.sskirillss.relics.items.relics.necklace;

import com.google.common.collect.Lists;
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
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.SelectorType;
import it.hurts.sskirillss.relics.entities.ElectricSparkEntity;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.RelicStackingUtils;
import it.hurts.sskirillss.relics.utils.TargetingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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

public class JellyfishNecklaceItem extends WearableRelicItem {
    private static final ResourceLocation REGEN_MAX_HEALTH_ATTRIBUTE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "jellyfish_necklace_regeneration_max_health");
    private static final ResourceLocation REGEN_RETENTION_ATTRIBUTE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "jellyfish_necklace_regeneration_retention");

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("regeneration")
                                .initialMaxLevel(10)
                                .rankModifier(3, "retention")
                                .stat(AbilityStatTemplate.builder("max_health")
                                        .initialValue(0.1D, 0.2D)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 1.5001D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("regeneration")
                                        .initialValue(0.15D, 0.35D)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 1.99985D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("health_regeneration")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("health_regenerated")
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
                                .stat(AbilityStatTemplate.builder("cooldown")
                                        .initialValue(120D, 60D)
                                        .thresholdValue(0, Double.MAX_VALUE)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 9.99988D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("rings")
                                        .initialValue(1D, 2D)
                                        .thresholdValue(0, Double.MAX_VALUE)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 10.001D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.RADICAL.get(), 4.99977D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("knockback")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 2.50355D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("paralysis")
                                        .initialValue(1D, 2.5D)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 9.9989D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("distance")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 19.99989D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bounces")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 24.9975D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 19.99989D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage_modifier")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.EXPONENTIAL.get(), 4.99993D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.RADICAL.get(), 30.00017D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("rings_accumulating")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("arcs_bouncing")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("hit_paralysis")
                                                .rankModifierVisibilityState("charge", VisibilityState.OBFUSCATED)
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("rings_accumulated")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("rings_paralysis")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("arcs_spawned")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("arcs_bounces")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("arcs_damage")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("hit_paralysis")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .rankModifierVisibilityState("charge", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 4).star(1, 4, 11).star(2, 11, 11).star(3, 18, 11).star(4, 11, 18).star(5, 6, 21).star(6, 16, 21).star(7, 5, 24).star(8, 17, 24).star(9, 4, 27).star(10, 18, 27).star(11, 8, 29).star(12, 14, 29)
                                        .link(4, 12).link(12, 11).link(11, 4).link(5, 6).link(7, 8).link(9, 10).link(0, 3).link(3, 4).link(4, 1).link(1, 0).link(0, 2).link(2, 4).link(1, 2).link(2, 3)
                                        .build())
                                .targeting(AbilityTargetingTemplate.builder()
                                        .selector(SelectorType.HARMFUL)
                                        .build())
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

        var activeRegenerationStacks = RelicStackingUtils.findActiveStacks(entity, RelicsItems.JELLYFISH_NECKLACE.get(),
                equippedStack -> equippedStack.getItem() == this
                        && this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("regeneration").canPlayerUse(entity));

        if (RelicStackingUtils.isControllerStack(stack, activeRegenerationStacks)) {
            var multiplier = (float) RelicStackingUtils.sumValue(entity, stack, "regeneration", "max_health");
            var retentionUnlocked = activeRegenerationStacks.stream().anyMatch(
                    equippedStack -> this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("regeneration").getRankModifierData("retention").isEnabled());

            if (entity.isInLiquid() || entity.isInRain()) {
                var boostedMaxHealth = entity.getMaxHealth() * multiplier;

                EntityUtils.resetAttribute(entity, Attributes.MAX_HEALTH, multiplier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, REGEN_MAX_HEALTH_ATTRIBUTE);
                EntityUtils.removeAttribute(entity, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, REGEN_RETENTION_ATTRIBUTE);

                entity.setHealth(entity.getHealth() + Math.min(entity.getAbsorptionAmount(), boostedMaxHealth));
            } else {
                EntityUtils.removeAttribute(entity, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, REGEN_MAX_HEALTH_ATTRIBUTE);

                if (retentionUnlocked && !EntityUtils.hasAttribute(entity, Attributes.MAX_HEALTH, REGEN_MAX_HEALTH_ATTRIBUTE)
                        && !EntityUtils.hasAttribute(entity, Attributes.MAX_ABSORPTION, REGEN_RETENTION_ATTRIBUTE)) {
                    var baseMaxHealth = entity.getMaxHealth();
                    var currentHealth = entity.getHealth();
                    var extraHealth = Math.max(0F, currentHealth - baseMaxHealth);

                    entity.setHealth(Math.min(currentHealth, baseMaxHealth));

                    EntityUtils.resetAttribute(entity, Attributes.MAX_ABSORPTION, extraHealth, AttributeModifier.Operation.ADD_VALUE, REGEN_RETENTION_ATTRIBUTE);
                    entity.setAbsorptionAmount(extraHealth);
                }
            }
        }

        var activeShockStacks = RelicStackingUtils.findActiveStacks(entity, RelicsItems.JELLYFISH_NECKLACE.get(),
                equippedStack -> equippedStack.getItem() == this && this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("shock").canPlayerUse(entity)
                        && !this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("shock").getMode().equals("disabled"));

        if (RelicStackingUtils.isControllerStack(stack, activeShockStacks)) {
            var cooldown = this.getCooldown(stack);
            var maxRings = (int) Math.ceil(RelicStackingUtils.sumValue(entity, stack, "shock", "rings",
                    abilityData -> !abilityData.getMode().equals("disabled")));
            var rings = Math.min(this.getRings(stack), maxRings);
            if (rings != this.getRings(stack))
                this.setRings(stack, rings);

            var cooldownTicks = (int) Math.round(RelicStackingUtils.bestValue(entity, stack, "shock", "cooldown", abilityData -> !abilityData.getMode().equals("disabled")) * 20D);
            var radius = RelicStackingUtils.bestValue(entity, stack, "shock", "radius", abilityData -> !abilityData.getMode().equals("disabled"));
            var knockback = RelicStackingUtils.bestValue(entity, stack, "shock", "knockback", abilityData -> !abilityData.getMode().equals("disabled"));
            var paralysis = RelicStackingUtils.bestValue(entity, stack, "shock", "paralysis", abilityData -> !abilityData.getMode().equals("disabled"));
            var distance = RelicStackingUtils.bestValue(entity, stack, "shock", "distance", abilityData -> !abilityData.getMode().equals("disabled"));
            var bounces = (int) Math.round(RelicStackingUtils.bestValue(entity, stack, "shock", "bounces", abilityData -> !abilityData.getMode().equals("disabled")));
            var damage = RelicStackingUtils.bestValue(entity, stack, "shock", "damage", abilityData -> !abilityData.getMode().equals("disabled"));
            var conductorUnlocked = activeShockStacks.stream().anyMatch(equippedStack -> this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("shock").getRankModifierData("conductor").isEnabled());
            var chargeUnlocked = activeShockStacks.stream().anyMatch(equippedStack -> this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("shock").getRankModifierData("charge").isEnabled());
            var damageModifier = conductorUnlocked ? RelicStackingUtils.maxValue(entity, stack, "shock", "damage_modifier", abilityData -> !abilityData.getMode().equals("disabled")) : 0D;
            var chargeDurationTicks = (int) Math.round(RelicStackingUtils.bestValue(entity, stack, "shock", "duration", abilityData -> !abilityData.getMode().equals("disabled")) * 20D);
            var flawless = activeShockStacks.stream().anyMatch(equippedStack -> this.getRelicData(entity, equippedStack).isVisuallyFlawless());

            if (rings < maxRings) {
                if (cooldown > 0)
                    this.addCooldown(stack, -1);
                else {
                    this.addRings(stack, 1);
                    this.setCooldown(stack, cooldownTicks);

                    if (!level.isClientSide()) {
                        this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("shock").getStatisticData().getMetricData("rings_accumulated").addValue(1);
                        this.getRelicData(entity, stack).getLevelingData().addExperience("shock", "rings_accumulating", 1);
                    }
                }
            }

            var affectedEntities = this.getAffectedEntities(stack);

            Predicate<LivingEntity> predicate = entry -> {
                var uuid = entry.getStringUUID();

                return !uuid.equals(entity.getStringUUID()) && !affectedEntities.contains(uuid) && TargetingUtils.canHarm(entity, entry, stack, "shock");
            };

            var collidedEntities = level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(0.5F), predicate);

            if (rings > 0 && !collidedEntities.isEmpty()) {
                for (var target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(0.5F).inflate(radius), predicate)) {
                    var diff = target.position().add(0, target.getBbHeight() / 2D, 0).subtract(entity.position());
                    var totalKnockback = 0.5D * knockback;

                    TargetingUtils.addHarmfulEffect(target, new MobEffectInstance(RelicsMobEffects.PARALYSIS, (int) (paralysis * 20), 0, false, true), entity, stack, "shock");

                    if (!level.isClientSide())
                        this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("shock").getStatisticData().getMetricData("rings_paralysis").addValue(paralysis);

                    target.setDeltaMovement(diff.normalize().multiply(totalKnockback, totalKnockback / 2F, totalKnockback));

                    var spark = new ElectricSparkEntity(RelicsEntities.ELECTRIC_SPARK.get(), level);

                    spark.setDamageModifier(conductorUnlocked ? (float) damageModifier : 0F);
                    spark.setDistance((float) distance);
                    spark.setBounces(bounces);
                    spark.setDamage((float) damage);
                    spark.setPos(entity.position().add(0F, entity.getBbHeight() / 2F, 0D));
                    spark.setFlawless(flawless);
                    spark.setTarget(target);
                    spark.setOwner(entity);
                    spark.setStack(stack);

                    level.addFreshEntity(spark);

                    this.addAffectedEntities(stack, target.getStringUUID());

                    if (!level.isClientSide())
                        this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("shock").getStatisticData().getMetricData("arcs_spawned").addValue(1);
                }

                this.addRings(stack, -1);

                if (chargeUnlocked)
                    this.setDuration(stack, chargeDurationTicks);
            }

            if (!affectedEntities.isEmpty())
                this.removeAffectedEntities(stack, affectedEntities.stream()
                        .filter(Predicate.not(collidedEntities.stream()
                                .map(Entity::getStringUUID)
                                .collect(Collectors.toSet())::contains))
                        .toArray(String[]::new));

            if (chargeUnlocked) {
                var duration = this.getDuration(stack);

                if (duration > 0)
                    this.addDuration(stack, -1);
                else
                    this.setDamagedEntities(stack, new ArrayList<>());
            }

            var controllerCooldown = this.getCooldown(stack);
            var controllerRings = this.getRings(stack);
            var controllerDuration = this.getDuration(stack);
            var controllerDamagedEntities = this.getDamagedEntities(stack);
            var controllerAffectedEntities = this.getAffectedEntities(stack);

            for (var equippedStack : activeShockStacks) {
                if (equippedStack == stack)
                    continue;

                this.setCooldown(equippedStack, controllerCooldown);
                this.setRings(equippedStack, controllerRings);
                this.setDuration(equippedStack, controllerDuration);
                this.setDamagedEntities(equippedStack, new ArrayList<>(controllerDamagedEntities));
                this.setAffectedEntities(equippedStack, new ArrayList<>(controllerAffectedEntities));
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (stack.getItem() == newStack.getItem())
            return;

        var entity = slotContext.entity();
        var activeRegenerationStacks = EntityUtils.findEquippedCurios(entity, RelicsItems.JELLYFISH_NECKLACE.get()).stream()
                .filter(equippedStack -> equippedStack != stack)
                .filter(equippedStack -> equippedStack.getItem() == this
                        && this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("regeneration").canPlayerUse(entity))
                .toList();
        var inWaterOrRain = entity.isInLiquid() || entity.isInRain();

        if (activeRegenerationStacks.isEmpty()) {
            EntityUtils.removeAttribute(entity, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, REGEN_RETENTION_ATTRIBUTE);
            EntityUtils.removeAttribute(entity, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, REGEN_MAX_HEALTH_ATTRIBUTE);
            entity.setAbsorptionAmount(Math.min(entity.getAbsorptionAmount(), entity.getMaxAbsorption()));

            return;
        }

        var remainingMultiplier = (float) activeRegenerationStacks.stream()
                .mapToDouble(equippedStack -> this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("regeneration").getStatData("max_health").getValue())
                .sum();
        var retentionUnlocked = activeRegenerationStacks.stream()
                .anyMatch(equippedStack -> this.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("regeneration").getRankModifierData("retention").isEnabled());

        if (inWaterOrRain) {
            EntityUtils.resetAttribute(entity, Attributes.MAX_HEALTH, remainingMultiplier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, REGEN_MAX_HEALTH_ATTRIBUTE);
            EntityUtils.removeAttribute(entity, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, REGEN_RETENTION_ATTRIBUTE);
            entity.setAbsorptionAmount(Math.min(entity.getAbsorptionAmount(), entity.getMaxAbsorption()));

            return;
        }

        EntityUtils.removeAttribute(entity, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, REGEN_MAX_HEALTH_ATTRIBUTE);

        if (!retentionUnlocked) {
            EntityUtils.removeAttribute(entity, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, REGEN_RETENTION_ATTRIBUTE);
            entity.setAbsorptionAmount(Math.min(entity.getAbsorptionAmount(), entity.getMaxAbsorption()));

            return;
        }

        var retentionCap = Math.max(0F, entity.getMaxHealth() * remainingMultiplier);
        var keptAbsorption = Math.min(entity.getAbsorptionAmount(), retentionCap);

        if (keptAbsorption <= 0F) {
            EntityUtils.removeAttribute(entity, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, REGEN_RETENTION_ATTRIBUTE);
            entity.setAbsorptionAmount(Math.min(entity.getAbsorptionAmount(), entity.getMaxAbsorption()));

            return;
        }

        EntityUtils.resetAttribute(entity, Attributes.MAX_ABSORPTION, keptAbsorption, AttributeModifier.Operation.ADD_VALUE, REGEN_RETENTION_ATTRIBUTE);
        entity.setAbsorptionAmount(keptAbsorption);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var entity = event.getEntity();
            var level = entity.level();

            if (!level.isClientSide() && level.getServer() != null && !level.getServer().isSameThread())
                return;

            if (entity.isInLiquid() || entity.isInRain()) {
                for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.JELLYFISH_NECKLACE.get())) {
                    var relic = (JellyfishNecklaceItem) stack.getItem();

                    var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("regeneration");

                    if (!abilityData.canPlayerUse(entity))
                        continue;

                    var health = Math.min(entity.getMaxHealth(), event.getAmount() * abilityData.getStatData("regeneration").getValue());

                    event.setAmount((float) (event.getAmount() + health));

                    if (!level.isClientSide()) {
                        relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("regeneration").getStatisticData().getMetricData("health_regenerated").addValue(health);

                        relic.getRelicData(entity, stack).getLevelingData().addExperience("regeneration", "health_regeneration", health);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getDirectEntity() instanceof LivingEntity entity))
                return;

            var level = entity.level();
            var stacks = EntityUtils.findEquippedCurios(entity, RelicsItems.JELLYFISH_NECKLACE.get()).stream()
                    .filter(stack -> stack.getItem() instanceof JellyfishNecklaceItem relic
                            && relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("shock").canPlayerUse(entity)
                            && !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("shock").getMode().equals("disabled"))
                    .toList();

            if (stacks.isEmpty())
                return;

            var stack = stacks.getFirst();
            var relic = (JellyfishNecklaceItem) stack.getItem();
            var chargeUnlocked = stacks.stream().anyMatch(
                    equippedStack -> relic.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData("shock").getRankModifierData("charge").isEnabled());

            if (!chargeUnlocked)
                return;

            if (relic.getDuration(stack) <= 0)
                return;

            var targets = relic.getDamagedEntities(stack);
            var target = event.getEntity();
            var uuid = target.getStringUUID();

            if (!targets.contains(uuid)) {
                var paralysis = RelicStackingUtils.bestValue(entity, stack, "shock", "paralysis",
                        abilityData -> !abilityData.getMode().equals("disabled"));

                target.addEffect(new MobEffectInstance(RelicsMobEffects.PARALYSIS, (int) (paralysis * 20), 0, false, false));

                if (!level.isClientSide()) {
                    relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("shock").getStatisticData().getMetricData("hit_paralysis").addValue(paralysis);
                    relic.getRelicData(entity, stack).getLevelingData().addExperience("shock", "hit_paralysis", paralysis);
                }

                relic.addDamagedEntities(stack, uuid);
            }
        }
    }
}

