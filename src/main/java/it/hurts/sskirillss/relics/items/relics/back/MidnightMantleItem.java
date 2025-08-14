package it.hurts.sskirillss.relics.items.relics.back;

import it.hurts.sskirillss.relics.api.events.leveling.AbilityModeSwitchEvent;
import it.hurts.sskirillss.relics.api.relics.*;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.ConstellationStarEntity;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.FallingStarEntity;
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
import it.hurts.sskirillss.relics.utils.ServerScheduler;
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

public class MidnightMantleItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("phase")
                                .modes("full_moon", "new_moon")
                                .rankModifier(1, "switch")
                                .stat(StatTemplate.builder("attack_damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("attack_speed")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("max_health")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("health_regeneration")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("modifier")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("health_regeneration")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("damage_dealing")
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
                                .stat(StatTemplate.builder("brightness")
                                        .thresholdValue(0D, 1D)
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0545D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("cooldown")
                                        .thresholdValue(0D, Double.MAX_VALUE)
                                        .initialValue(15D, 10D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.01636D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("being_invisible")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("damage_dealing")
                                                .rankModifierCondition("strike")
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
                                .stat(StatTemplate.builder("star_chance")
                                        .initialValue(0.1D, 0.2D)
                                        .thresholdValue(0D, 1D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.1739D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatTemplate.builder("constellation_radius")
                                        .thresholdValue(0D, Double.MAX_VALUE)
                                        .initialValue(5D, 7D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0468D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("tremor_duration")
                                        .initialValue(0.25D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("explosion_radius")
                                        .initialValue(0.5D, 1D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1182D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("explosion_damage")
                                        .initialValue(1D, 5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("star_lifetime")
                                        .initialValue(10D, 15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1273D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("stun_duration")
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
                                                .build())
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("starfall")
                                .requiredLevel(15)
                                .rankModifier(7, "bounce")
                                .stat(StatTemplate.builder("chance")
                                        .thresholdValue(0D, 1D)
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0364D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0727D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1636D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.3455D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("bounce_chance")
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
                                                .rankModifierCondition("bounce")
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
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xffef9b53)
                                .borderBottom(0xfff3f38f)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_END, LootEntries.END_LIKE)
                        .build())
                .build();
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
        if (!this.canPlayerUseAbility(entity, stack, "phase"))
            return 0D;

        var mode = this.getAbilityMode(entity, stack, "phase");
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

        if (this.isAbilityRankModifierUnlocked(entity, stack, "phase", "switch") && this.getPhaseDuration(stack) > 0)
            effectiveness *= 1F + this.getStatValue(entity, stack, "phase", "modifier");

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

        return ((1D - (skyDarken / maxSkyDarken)) * skyBrightness + blockBrightness) / (maxBrightness * 2D) <= this.getStatValue(entity, stack, "invisibility", "brightness");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();
        var level = entity.level();

        if (level.isClientSide())
            return;

        if (this.canPlayerUseAbility(entity, stack, "phase")) {
            var mode = this.getAbilityMode(entity, stack, "phase");

            if (!mode.isEmpty()) {
                if (entity.tickCount % 20 == 0)
                    this.addAbilityMetricValue(entity, stack, "phase", "duration_" + mode, 1);

                var totalEffectiveness = this.getModeEffectiveness(entity, stack);

                var attackEffectiveness = mode.equals("full_moon") ? totalEffectiveness : 0D;
                var healEffectiveness = mode.equals("new_moon") ? totalEffectiveness : 0D;

                EntityUtils.resetAttribute(entity, stack, Attributes.ATTACK_SPEED, (float) (this.getStatValue(entity, stack, "phase", "attack_speed") * attackEffectiveness), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                EntityUtils.resetAttribute(entity, stack, Attributes.MAX_HEALTH, (float) (this.getStatValue(entity, stack, "phase", "max_health") * healEffectiveness), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            }

            if (this.isAbilityRankModifierUnlocked(entity, stack, "phase", "switch") && this.getPhaseDuration(stack) > 0)
                this.addPhaseDuration(stack, -1);
        }

        if (this.canPlayerUseAbility(entity, stack, "invisibility")) {
            var cooldown = this.getInvisibilityCooldown(stack);

            if (cooldown > 0) {
                if (level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(16)).stream().noneMatch(mob -> mob.getTarget() == entity && mob.hasLineOfSight(entity)))
                    this.addInvisibilityCooldown(stack, -1);
            } else if (this.canHideInTheDarkness(entity, stack)) {
                entity.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 5, 0, false, false));

                if (entity.tickCount % 20 == 0) {
                    this.addAbilityMetricValue(entity, stack, "invisibility", "duration", 1);

                    if (this.canAddRelicExperience(entity, stack, "invisibility", "being_invisible"))
                        this.addRelicExperience(entity, stack, "invisibility", "being_invisible", 1);
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

        EntityUtils.removeAttribute(entity, stack, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        EntityUtils.removeAttribute(entity, stack, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        private static void onInteract(LivingEntity entity) {
            if (entity.getCommandSenderWorld().isClientSide())
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "invisibility"))
                    continue;

                ServerScheduler.schedule(1, () -> relic.setInvisibilityCooldown(stack, (int) (relic.getStatValue(entity, stack, "invisibility", "cooldown") * 20)));
            }
        }

        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var entity = event.getEntity();
            var level = entity.level();

            var pos = entity.blockPosition();

            // WHY!? (cuz of thread locks lol)
            if (!level.hasChunkAt(pos) || !level.isLoaded(pos))
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "phase") || !relic.getAbilityMode(entity, stack, "phase").equals("new_moon"))
                    continue;

                var heal = event.getAmount() * relic.getStatValue(entity, stack, "phase", "health_regeneration") * relic.getModeEffectiveness(entity, stack);

                event.setAmount((float) (event.getAmount() + heal));

                if (!entity.level().isClientSide()) {
                    relic.addAbilityMetricValue(entity, stack, "phase", "health_regeneration", heal);

                    if (relic.canAddRelicExperience(entity, stack, "phase", "health_regeneration"))
                        relic.addRelicExperience(entity, stack, "phase", "health_regeneration", heal);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt1(LivingIncomingDamageEvent event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "phase") || !relic.getAbilityMode(entity, stack, "phase").equals("full_moon"))
                    continue;

                var damage = event.getAmount() * relic.getStatValue(entity, stack, "phase", "attack_damage") * relic.getModeEffectiveness(entity, stack);

                event.setAmount((float) (event.getAmount() + damage));

                if (!entity.level().isClientSide()) {
                    relic.addAbilityMetricValue(entity, stack, "phase", "additional_damage", damage);

                    if (relic.canAddRelicExperience(entity, stack, "phase", "damage_dealing"))
                        relic.addRelicExperience(entity, stack, "phase", "damage_dealing", damage);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt2(LivingIncomingDamageEvent event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "invisibility") || !relic.isAbilityRankModifierUnlocked(entity, stack, "invisibility", "strike")
                        || relic.getInvisibilityCooldown(stack) > 0 || !relic.canHideInTheDarkness(entity, stack))
                    continue;

                var damage = event.getAmount() * relic.getStatValue(entity, stack, "invisibility", "damage");

                event.setAmount((float) (event.getAmount() + damage));

                if (!entity.level().isClientSide()) {
                    relic.addAbilityMetricValue(entity, stack, "invisibility", "additional_damage", damage);

                    if (relic.canAddRelicExperience(entity, stack, "invisibility", "damage_dealing"))
                        relic.addRelicExperience(entity, stack, "invisibility", "damage_dealing", damage);
                }

                relic.setInvisibilityCooldown(stack, (int) relic.getStatValue(entity, stack, "invisibility", "cooldown"));
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
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "phase") || !relic.isAbilityRankModifierUnlocked(entity, stack, "phase", "switch"))
                    continue;

                relic.setPhaseDuration(stack, (int) relic.getStatValue(entity, stack, "phase", "duration") * 20);
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

                if (!relic.canPlayerUseAbility(entity, stack, "constellation") || random.nextDouble() > relic.getStatValue(entity, stack, "constellation", "star_chance"))
                    continue;

                var star = new ConstellationStarEntity(RelicsEntities.CONSTELLATION_STAR.get(), level);

                star.setDeltaMovement(MathUtils.randomFloat(random) * 0.5F, 0.1F + random.nextFloat() * 0.1F, MathUtils.randomFloat(random) * 0.5F);
                star.setConstellationRadius((float) relic.getStatValue(entity, stack, "constellation", "constellation_radius"));
                star.setExplosionRadius((float) relic.getStatValue(entity, stack, "constellation", "explosion_radius"));
                star.setDamage((float) relic.getStatValue(entity, stack, "constellation", "explosion_damage"));
                star.setTremor((float) relic.getStatValue(entity, stack, "constellation", "tremor_duration"));
                star.setLifetime((int) relic.getStatValue(entity, stack, "constellation", "star_lifetime"));
                star.setFlawless(relic.isRelicFlawless(entity, stack));
                star.setPos(entity.getEyePosition());
                star.setOwner(entity);
                star.setStack(stack);

                if (relic.isAbilityRankModifierUnlocked(entity, stack, "constellation", "stun"))
                    star.setStun((int) relic.getStatValue(entity, stack, "constellation", "stun_duration"));

                level.addFreshEntity(star);

                if (!level.isClientSide()) {
                    relic.addAbilityMetricValue(entity, stack, "constellation", "total_stars", 1);

                    if (relic.canAddRelicExperience(entity, stack, "constellation", "star_creation"))
                        relic.addRelicExperience(entity, stack, "constellation", "star_creation", 1);
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

                if (!relic.canPlayerUseAbility(entity, stack, "starfall")
                        || random.nextFloat() > relic.getStatValue(entity, stack, "starfall", "chance"))
                    continue;

                var pos = new Vec3(target.getX() + MathUtils.randomFloat(random) * 10, target.getY() + 25 + random.nextInt(25), target.getZ() + MathUtils.randomFloat(random) * 10);

                if (level.clip(new ClipContext(pos, target.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target)).getType() != HitResult.Type.MISS)
                    continue;

                var motion = target.position().subtract(pos).normalize().scale(2F);

                var star = new FallingStarEntity(RelicsEntities.FALLING_STAR.get(), level);

                star.setDamage((float) (event.getAmount() * relic.getStatValue(entity, stack, "starfall", "damage")));
                star.setRadius((int) Math.round(relic.getStatValue(entity, stack, "starfall", "radius")));
                star.setStun((int) (relic.getStatValue(entity, stack, "starfall", "stun") * 20));
                star.setFlawless(relic.isRelicFlawless(entity, stack));
                star.setDeltaMovement(motion);
                star.setOwner(entity);
                star.setStack(stack);
                star.setPos(pos);

                if (relic.isAbilityRankModifierUnlocked(entity, stack, "starfall", "bounce"))
                    star.setBounceChance((float) relic.getStatValue(entity, stack, "starfall", "bounce_chance"));

                level.addFreshEntity(star);

                if (!level.isClientSide()) {
                    relic.addAbilityMetricValue(entity, stack, "starfall", "total_stars", 1);

                    if (relic.canAddRelicExperience(entity, stack, "starfall", "star_creation"))
                        relic.addRelicExperience(entity, stack, "starfall", "star_creation", 1);
                }
            }
        }
    }
}