package it.hurts.sskirillss.relics.items.relics.back;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.relic.abilities.ability.AbilityModeSwitchEvent;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.ConstellationStarEntity;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.FallingStarEntity;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import top.theillusivec4.curios.api.SlotContext;

public class MidnightMantleItem extends WearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("phase")
                                .modes("full_moon", "new_moon")
                                .rankModifier(1, "switch")
                                .stat(AbilityStatTemplate.builder("attack_damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.07273D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("attack_speed")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.07273D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_health")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.03636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("health_regeneration")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.03636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("modifier")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.09091D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("health_regeneration")
                                                .modeVisibilityState("full_moon", VisibilityState.HIDDEN)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("damage_dealing")
                                                .modeVisibilityState("new_moon", VisibilityState.HIDDEN)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("duration_new_moon")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .modeVisibilityState("full_moon", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("duration_full_moon")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .modeVisibilityState("new_moon", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("additional_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .modeVisibilityState("new_moon", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("health_regeneration")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .modeVisibilityState("full_moon", VisibilityState.HIDDEN)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 6, 10).star(1, 16, 10).star(2, 6, 21).star(3, 16, 21)
                                        .link(0, 1).link(1, 3).link(3, 2).link(2, 0)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("invisibility")
                                .requiredLevel(5)
                                .rankModifier(3, "strike")
                                .stat(AbilityStatTemplate.builder("brightness")
                                        .thresholdValue(0D, 1D)
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.03636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("cooldown")
                                        .thresholdValue(0D, Double.MAX_VALUE)
                                        .initialValue(15D, 10D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.01636D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.07273D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("being_invisible")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("damage_dealing")
                                                .rankModifierVisibilityState("strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("additional_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("strike", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 6).star(1, 11, 10).star(2, 3, 16).star(3, 11, 16).star(4, 19, 16).star(5, 11, 21).star(6, 11, 25)
                                        .link(3, 1).link(3, 2).link(3, 4).link(3, 5).link(1, 4).link(4, 5).link(5, 2).link(2, 1).link(1, 0).link(5, 6).build())
                                .build())
                        .ability(AbilityTemplate.builder("constellation")
                                .requiredLevel(10)
                                .rankModifier(5, "stun")
                                .stat(AbilityStatTemplate.builder("star_chance")
                                        .initialValue(0.1D, 0.2D)
                                        .thresholdValue(0D, 1D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.1739D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("constellation_radius")
                                        .thresholdValue(0D, Double.MAX_VALUE)
                                        .initialValue(5D, 7D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0468D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("tremor_duration")
                                        .initialValue(0.25D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("explosion_radius")
                                        .initialValue(0.5D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1182D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("explosion_damage")
                                        .initialValue(1D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("star_lifetime")
                                        .initialValue(10D, 15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1273D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun_duration")
                                        .initialValue(1D, 2.5D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 1.8632D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 17, 4).star(1, 5, 9).star(2, 20, 15).star(3, 6, 20)
                                        .link(0, 1).link(1, 2).link(2, 3)
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("star_creation")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("star_tremor")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("star_damage")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("total_stars")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("star_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("star_stun")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("stun", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("starfall")
                                .requiredLevel(15)
                                .rankModifier(7, "bounce")
                                .stat(AbilityStatTemplate.builder("chance")
                                        .thresholdValue(0D, 1D)
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0364D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0727D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.3455D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("bounce_chance")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0424D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 4).star(1, 3, 11).star(2, 11, 11).star(3, 19, 11).star(4, 11, 18).star(5, 17, 21).star(6, 12, 23).star(7, 8, 26)
                                        .link(2, 0).link(2, 1).link(2, 3).link(2, 4).link(0, 3).link(3, 4).link(4, 1).link(1, 0).link(4, 6).link(6, 5).link(6, 7)
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("star_creation")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("shockwave_hit")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("star_bounce")
                                                .rankModifierVisibilityState("bounce", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("total_stars")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("shockwave_targets")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("shockwave_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("shockwave_stun")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("star_bounces")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("bounce", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(1000)
                        .maxRank(7)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_END, LootEntries.END_LIKE)
                        .build())
                .build();
    }

    private static ResourceLocation getPhaseAttributeId(ItemStack stack, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, SlotContext slotContext) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID,
                BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()
                        + "_" + BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()).getPath()
                        + "_" + slotContext.identifier()
                        + "_" + slotContext.index()
                        + "_phase");
    }


    public int getInvisibilityCooldown(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.MIDNIGHT_MANTLE_INVISIBILITY_COOLDOWN.get(), 0);
    }

    public void setInvisibilityCooldown(ItemStack stack, int cooldown) {
        stack.set(RelicsDataComponents.MIDNIGHT_MANTLE_INVISIBILITY_COOLDOWN.get(), cooldown);
    }

    public void addInvisibilityCooldown(ItemStack stack, int cooldown) {
        this.setInvisibilityCooldown(stack, this.getInvisibilityCooldown(stack) + cooldown);
    }

    public int getStarfallCooldown(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.MIDNIGHT_MANTLE_STARFALL_COOLDOWN.get(), 0);
    }

    public void setStarfallCooldown(ItemStack stack, int cooldown) {
        stack.set(RelicsDataComponents.MIDNIGHT_MANTLE_STARFALL_COOLDOWN.get(), Math.max(cooldown, 0));
    }

    public void addStarfallCooldown(ItemStack stack, int cooldown) {
        this.setStarfallCooldown(stack, this.getStarfallCooldown(stack) + cooldown);
    }

    public int getPhaseDuration(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.MIDNIGHT_MANTLE_PHASE_DURATION, 0);
    }

    public void setPhaseDuration(ItemStack stack, int duration) {
        stack.set(RelicsDataComponents.MIDNIGHT_MANTLE_PHASE_DURATION, Math.max(duration, 0));
    }

    public void addPhaseDuration(ItemStack stack, int duration) {
        this.setPhaseDuration(stack, this.getPhaseDuration(stack) + duration);
    }

    public double getModeEffectiveness(LivingEntity entity, ItemStack stack) {
        if (!this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").canPlayerUse(entity))
            return 0D;

        var mode = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getMode();
        var level = entity.getCommandSenderWorld();

        if (mode.isEmpty() || level.isDay())
            return 0;

        var newMoonPhase = 4;
        var selectedPhase = mode.equals("new_moon") ? newMoonPhase : 0;
        var currentPhase = level.getMoonPhase();

        var directDistance = Math.abs(currentPhase - selectedPhase);
        var wrappedDistance = 8 - directDistance;
        var minDistance = Math.min(directDistance, wrappedDistance);

        var effectiveness = 1 - (minDistance / 4D);

        if (this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getRankModifierData("switch").isEnabled() && this.getPhaseDuration(stack) > 0)
            effectiveness *= 1F + this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatData("modifier").getValue();

        return effectiveness;
    }

    public boolean canHideInTheDarkness(LivingEntity entity, ItemStack stack) {
        var level = entity.getCommandSenderWorld();
        var pos = entity.blockPosition();

        var blockBrightness = level.getBrightness(LightLayer.BLOCK, pos);
        var skyBrightness = level.getBrightness(LightLayer.SKY, pos);
        var maxBrightness = level.getMaxLightLevel();
        var skyDarken = level.getSkyDarken();
        var maxSkyDarken = 11D;

        var brightness = ((1D - (skyDarken / maxSkyDarken)) * skyBrightness + blockBrightness) / maxBrightness;

        return brightness <= this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").getStatData("brightness").getValue();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();
        var level = entity.level();

        if (level.isClientSide())
            return;

        if (this.getStarfallCooldown(stack) > 0)
            this.addStarfallCooldown(stack, -1);

        if (this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").canPlayerUse(entity)) {
            var mode = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getMode();

            if (!mode.isEmpty()) {
                if (entity.tickCount % 20 == 0)
                    this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatisticData().getMetricData("duration_" + mode).addValue(1);

                var totalEffectiveness = this.getModeEffectiveness(entity, stack);

                var attackEffectiveness = mode.equals("full_moon") ? totalEffectiveness : 0D;
                var healEffectiveness = mode.equals("new_moon") ? totalEffectiveness : 0D;

                EntityUtils.resetAttribute(entity, Attributes.ATTACK_SPEED,
                        (float) (this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatData("attack_speed").getValue() * attackEffectiveness),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                        getPhaseAttributeId(stack, Attributes.ATTACK_SPEED, slotContext));
                EntityUtils.resetAttribute(entity, Attributes.MAX_HEALTH,
                        (float) (this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatData("max_health").getValue() * healEffectiveness),
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                        getPhaseAttributeId(stack, Attributes.MAX_HEALTH, slotContext));
            }

            if (this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getRankModifierData("switch").isEnabled() && this.getPhaseDuration(stack) > 0)
                this.addPhaseDuration(stack, -1);
        }

        if (this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").canPlayerUse(entity)) {
            var cooldown = this.getInvisibilityCooldown(stack);

            if (cooldown > 0) {
                if (level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(16)).stream().noneMatch(mob -> mob.getTarget() == entity && mob.hasLineOfSight(entity)))
                    this.addInvisibilityCooldown(stack, -1);
            } else if (this.canHideInTheDarkness(entity, stack)) {
                entity.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 5, 0, false, false));

                if (entity.tickCount % 20 == 0) {
                    this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").getStatisticData().getMetricData("duration").addValue(1);

                    this.getRelicData(entity, stack).getLevelingData().addExperience("invisibility", "being_invisible", 1);
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (stack.getItem() == newStack.getItem())
            return;

        var entity = slotContext.entity();

        EntityUtils.removeAttribute(entity, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                getPhaseAttributeId(stack, Attributes.ATTACK_SPEED, slotContext));
        EntityUtils.removeAttribute(entity, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                getPhaseAttributeId(stack, Attributes.MAX_HEALTH, slotContext));
    }

    @EventBusSubscriber
    public static class CommonEvents {
        private static void onInteract(LivingEntity entity) {
            if (entity.getCommandSenderWorld().isClientSide())
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").canPlayerUse(entity))
                    continue;

                ServerScheduler.schedule(1, () -> relic.setInvisibilityCooldown(stack, (int) (relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").getStatData("cooldown").getValue() * 20)));
            }
        }

        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var entity = event.getEntity();
            var level = entity.level();

            if (!level.isClientSide() && level.getServer() != null && !level.getServer().isSameThread())
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").canPlayerUse(entity) || !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getMode().equals("new_moon"))
                    continue;

                var heal = event.getAmount() * relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatData("health_regeneration").getValue() * relic.getModeEffectiveness(entity, stack);

                event.setAmount((float) (event.getAmount() + heal));

                if (!entity.level().isClientSide()) {
                    relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatisticData().getMetricData("health_regeneration").addValue(heal);

                    relic.getRelicData(entity, stack).getLevelingData().addExperience("phase", "health_regeneration", heal);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt1(LivingIncomingDamageEvent event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").canPlayerUse(entity) || !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getMode().equals("full_moon"))
                    continue;

                var damage = event.getAmount() * relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatData("attack_damage").getValue() * relic.getModeEffectiveness(entity, stack);

                event.setAmount((float) (event.getAmount() + damage));

                if (!entity.level().isClientSide()) {
                    relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatisticData().getMetricData("additional_damage").addValue(damage);

                    if (damage > 0) {
                        var experience = entity.getRandom().nextInt((int) (damage + 1));

                        if (experience > 0)
                            relic.getRelicData(entity, stack).getLevelingData().addExperience("phase", "damage_dealing", experience);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt2(LivingIncomingDamageEvent event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").canPlayerUse(entity) || !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").getRankModifierData("strike").isEnabled()
                        || relic.getInvisibilityCooldown(stack) > 0 || !relic.canHideInTheDarkness(entity, stack))
                    continue;

                var damage = event.getAmount() * relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").getStatData("damage").getValue();

                event.setAmount((float) (event.getAmount() + damage));

                if (!entity.level().isClientSide()) {
                    relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").getStatisticData().getMetricData("additional_damage").addValue(damage);

                    relic.getRelicData(entity, stack).getLevelingData().addExperience("invisibility", "damage_dealing", 1);
                }

                relic.setInvisibilityCooldown(stack, (int) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("invisibility").getStatData("cooldown").getValue());
            }
        }

        @SubscribeEvent
        public static void onLivingHurt3(LivingIncomingDamageEvent event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onBlockBreakAttempt(PlayerEvent.BreakSpeed event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onItemToss(ItemTossEvent event) {
            CommonEvents.onInteract(event.getPlayer());
        }

        @SubscribeEvent
        public static void onItemPickup(ItemEntityPickupEvent.Post event) {
            CommonEvents.onInteract(event.getPlayer());
        }

        @SubscribeEvent
        public static void onAbilityModeSwitch(AbilityModeSwitchEvent event) {
            var entity = event.getBearer();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").canPlayerUse(entity) || !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getRankModifierData("switch").isEnabled())
                    continue;

                relic.setPhaseDuration(stack, (int) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getStatData("duration").getValue() * 20);
            }
        }

        @SubscribeEvent
        public static void onLivingHurt4(LivingDamageEvent.Post event) {
            if (event.getOriginalDamage() < 1D || !(event.getSource().getEntity() instanceof LivingEntity source))
                return;

            var entity = event.getEntity();

            if (source.getStringUUID().equals(entity.getStringUUID()))
                return;

            var level = entity.level();
            var random = level.getRandom();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").canPlayerUse(entity) || random.nextDouble() > relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getStatData("star_chance").getValue())
                    continue;

                var star = new ConstellationStarEntity(RelicsEntities.CONSTELLATION_STAR.get(), level);

                star.setDeltaMovement(MathUtils.randomFloat(random) * 0.5F, 0.1F + random.nextFloat() * 0.1F, MathUtils.randomFloat(random) * 0.5F);
                star.setConstellationRadius((float) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getStatData("constellation_radius").getValue());
                star.setExplosionRadius((float) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getStatData("explosion_radius").getValue());
                star.setDamage((float) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getStatData("explosion_damage").getValue());
                star.setTremor((float) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getStatData("tremor_duration").getValue());
                star.setLifetime((int) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getStatData("star_lifetime").getValue());
                star.setFlawless(relic.getRelicData(entity, stack).isVisuallyFlawless());
                star.setPos(entity.getEyePosition());
                star.setOwner(entity);
                star.setStack(stack);

                if (relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getRankModifierData("stun").isEnabled())
                    star.setStun((float) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getStatData("stun_duration").getValue());

                level.addFreshEntity(star);

                if (!level.isClientSide()) {
                    relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("constellation").getStatisticData().getMetricData("total_stars").addValue(1);

                    relic.getRelicData(entity, stack).getLevelingData().addExperience("constellation", "star_creation", 1);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt5(LivingIncomingDamageEvent event) {
            if (event.getAmount() < 1D || !(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            var target = event.getEntity();

            if (target.getStringUUID().equals(entity.getStringUUID()))
                return;

            var level = entity.level();
            var random = level.getRandom();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("starfall").canPlayerUse(entity)
                        || relic.getStarfallCooldown(stack) > 0
                        || random.nextFloat() > relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("starfall").getStatData("chance").getValue())
                    continue;

                var pos = new Vec3(target.getX() + MathUtils.randomFloat(random) * 10, target.getY() + 25 + random.nextInt(25), target.getZ() + MathUtils.randomFloat(random) * 10);

                if (level.clip(new ClipContext(pos, target.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target)).getType() != HitResult.Type.MISS)
                    continue;

                var motion = target.position().subtract(pos).normalize().scale(2F);

                var star = new FallingStarEntity(RelicsEntities.FALLING_STAR.get(), level);

                star.setDamage((float) (event.getAmount() * relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("starfall").getStatData("damage").getValue()));
                star.setRadius((int) Math.round(relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("starfall").getStatData("radius").getValue()));
                star.setStun((int) (relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("starfall").getStatData("stun").getValue() * 20));
                star.setFlawless(relic.getRelicData(entity, stack).isVisuallyFlawless());
                star.setDeltaMovement(motion);
                star.setOwner(entity);
                star.setStack(stack);
                star.setPos(pos);

                if (relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("starfall").getRankModifierData("bounce").isEnabled())
                    star.setBounceChance((float) relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("starfall").getStatData("bounce_chance").getValue());

                level.addFreshEntity(star);
                relic.setStarfallCooldown(stack, 20);

                if (!level.isClientSide()) {
                    relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("starfall").getStatisticData().getMetricData("total_stars").addValue(1);

                    relic.getRelicData(entity, stack).getLevelingData().addExperience("starfall", "star_creation", 1);
                }
            }
        }
    }
}

