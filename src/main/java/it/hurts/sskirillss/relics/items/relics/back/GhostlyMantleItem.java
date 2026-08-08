package it.hurts.sskirillss.relics.items.relics.back;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import it.hurts.sskirillss.relics.entities.GhostlyFogEntity;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.TargetingUtils;
import lombok.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.HashMap;
import java.util.Map;

public class GhostlyMantleItem extends WearableRelicItem {
    public static final String FOG_SUFFOCATION_DAMAGE_TAG = "relics:ghostly_mantle_fog_suffocation_damage";
    public static final String FOG_SUFFOCATION_UNTIL_TAG = "relics:ghostly_mantle_fog_suffocation_until";

    private static final double FOG_GROUND_SEARCH_STEP = 0.25D;
    private static final double MAX_FOG_GROUND_SEARCH = 3D;

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("fog")
                                .modes("enabled", "disabled")
                                .rankModifier(1, "frostbite")
                                .targeting(AbilityTargetingTemplate.builder()
                                        .selector(SelectorType.HARMFUL)
                                        .build())
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(3D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(0.5D, 1D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("air_loss")
                                        .initialValue(1D, 2D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> (int) MathUtils.round(value * 20, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("suffocation_damage")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("tremor")
                                        .initialValue(1D, 2D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("fog_creation")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("fog_exposure")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("fog_clouds")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("fog_exposure")
                                                .formatValue(value -> MathUtils.formatTime(value.intValue()))
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 14, 25).star(1, 6, 19).star(2, 15, 14).star(3, 10, 10).star(4, 15, 4)
                                        .link(0, 1).link(1, 2).link(2, 3).link(3, 4)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("gaze")
                                .requiredLevel(5)
                                .rankModifier(3, "dread")
                                .targeting(AbilityTargetingTemplate.builder()
                                        .selector(SelectorType.HARMFUL)
                                        .build())
                                .stat(AbilityStatTemplate.builder("range")
                                        .initialValue(5D, 10D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 30D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("charges")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("weakness")
                                        .initialValue(0.005D, 0.01D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.075D)
                                        .formatValue(value -> MathUtils.round(value * 100D, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(0.01D, 0.025D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value * 100D, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("eye_contact")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("charges_applied")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_reduced")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_bonus")
                                                .rankModifierVisibilityState("dread", VisibilityState.OBFUSCATED)
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 2, 14).star(1, 7, 7).star(2, 13, 7).star(3, 19, 14).star(4, 20, 28).star(5, 1, 28).star(6, 7, 15).star(7, 14, 15)
                                        .link(5, 0).link(0, 1).link(1, 2).link(2, 3).link(3, 4).link(3, 7).link(6, 0)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("spectral_escape")
                                .requiredLevel(10)
                                .rankModifier(5, "reprisal")
                                .targeting(AbilityTargetingTemplate.builder()
                                        .selector(SelectorType.HARMFUL)
                                        .build())
                                .stat(AbilityStatTemplate.builder("cooldown")
                                        .initialValue(125D, 100D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(0.5D, 1D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("spectral_form")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("death_prevention")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("reprisal_damage")
                                                .rankModifierVisibilityState("reprisal", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("spectral_form_duration")
                                                .formatValue(value -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("prevented_deaths")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("reprisal_damage")
                                                .rankModifierVisibilityState("reprisal", VisibilityState.OBFUSCATED)
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 5, 23).star(1, 1, 16).star(2, 1, 7).star(3, 6, 2).star(4, 12, 5).star(5, 13, 10).star(6, 17, 14).star(7, 9, 26).star(8, 20, 29).star(9, 13, 20).star(10, 4, 9).star(11, 10, 7)
                                        .link(8, 7).link(0, 9).link(9, 6).link(6, 5).link(5, 4).link(4, 3).link(3, 2).link(2, 1).link(4, 11).link(2, 10).link(1, 0)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxRank(5)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_NETHER)
                        .entry(LootEntries.NETHER_LIKE)
                        .build())
                .build();
    }

    public Vec3 getLastFogPos(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GHOSTLY_MANTLE_LAST_FOG_POS, Vec3.ZERO);
    }

    public void setLastFogPos(ItemStack stack, Vec3 pos) {
        stack.set(RelicsDataComponents.GHOSTLY_MANTLE_LAST_FOG_POS, pos);
    }

    public Map<String, GazeChargeData> getGazeCharges(ItemStack stack) {
        return new HashMap<>(stack.getOrDefault(RelicsDataComponents.GHOSTLY_MANTLE_GAZE_CHARGES, Map.of()));
    }

    public void setGazeCharges(ItemStack stack, Map<String, GazeChargeData> charges) {
        stack.set(RelicsDataComponents.GHOSTLY_MANTLE_GAZE_CHARGES, new HashMap<>(charges));
    }

    public int getEscapeCooldown(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GHOSTLY_MANTLE_ESCAPE_COOLDOWN, 0);
    }

    public void setEscapeCooldown(ItemStack stack, int cooldown) {
        stack.set(RelicsDataComponents.GHOSTLY_MANTLE_ESCAPE_COOLDOWN, Math.max(0, cooldown));
    }

    public Map<String, Double> getReprisalMarks(ItemStack stack) {
        return new HashMap<>(stack.getOrDefault(RelicsDataComponents.GHOSTLY_MANTLE_REPRISAL_MARKS, Map.of()));
    }

    public void setReprisalMarks(ItemStack stack, Map<String, Double> marks) {
        stack.set(RelicsDataComponents.GHOSTLY_MANTLE_REPRISAL_MARKS, new HashMap<>(marks));
    }

    public int getTotalCharges(ItemStack stack) {
        return this.getGazeCharges(stack).values().stream().mapToInt(GazeChargeData::charges).sum();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();

        if (entity.level().isClientSide())
            return;

        this.tickFogTrail(entity, stack);
        this.tickGaze(entity, stack);

        if (this.getEscapeCooldown(stack) > 0)
            this.setEscapeCooldown(stack, this.getEscapeCooldown(stack) - 1);

        var spectralEscape = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("spectral_escape");

        if (spectralEscape.canPlayerUse(entity)
                && entity.hasEffect(RelicsMobEffects.FLIGHT)
                && entity.hasEffect(RelicsMobEffects.IMMORTALITY)) {
            spectralEscape.getStatisticData().getMetricData("spectral_form_duration").addValue(1D / 20D);
            this.getRelicData(entity, stack).getLevelingData().addExperience("spectral_escape", "spectral_form", 1D / 20D);
        }
    }

    private void tickFogTrail(LivingEntity entity, ItemStack stack) {
        var ability = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("fog");

        if (!ability.canPlayerUse(entity) || ability.getMode().equals("disabled")) {
            this.setLastFogPos(stack, entity.position());

            return;
        }

        var current = entity.position();
        var last = this.getLastFogPos(stack);

        if (last == Vec3.ZERO || last.distanceToSqr(current) > 256D) {
            this.setLastFogPos(stack, current);

            return;
        }

        var distance = last.distanceTo(current);
        var fogRadius = Math.max(0.1D, ability.getStatData("radius").getValue());

        if (distance < fogRadius)
            return;

        var direction = current.subtract(last).normalize();
        var steps = Math.min(8, (int) Math.floor(distance / fogRadius));
        var gravity = entity.getAttributeValue(Attributes.GRAVITY);
        var maxGroundSearch = FOG_GROUND_SEARCH_STEP;

        if (gravity > 0.001D) {
            var upwardSpeed = entity.getAttributeValue(Attributes.JUMP_STRENGTH);

            while (upwardSpeed > 0D && maxGroundSearch < MAX_FOG_GROUND_SEARCH) {
                maxGroundSearch += upwardSpeed;
                upwardSpeed = (upwardSpeed - gravity) * 0.98D;
            }

            maxGroundSearch = Math.min(maxGroundSearch, MAX_FOG_GROUND_SEARCH);
        }

        var spawned = false;

        for (var i = 1; i <= steps; i++) {
            var pos = last.add(direction.scale(i * fogRadius));
            var y = pos.y;
            var groundFound = false;

            for (var j = 0D; j <= maxGroundSearch; j += FOG_GROUND_SEARCH_STEP) {
                var blockPos = BlockPos.containing(pos.x, pos.y + 0.2D - j, pos.z);
                var state = entity.level().getBlockState(blockPos);

                if (state.isAir())
                    continue;

                var shape = state.getCollisionShape(entity.level(), blockPos);

                if (shape.isEmpty())
                    continue;

                y = blockPos.getY() + shape.max(Direction.Axis.Y) + 0.03D;
                groundFound = true;

                break;
            }

            if (!groundFound)
                continue;

            var fog = new GhostlyFogEntity(RelicsEntities.GHOSTLY_FOG.get(), entity.level());

            fog.setOwner(entity);
            fog.setLifetime((int) Math.max(1D, ability.getStatData("duration").getValue() * 20D));
            fog.setRadius((float) fogRadius);
            fog.setAirDrain((int) Math.max(0D, MathUtils.round(ability.getStatData("air_loss").getValue(), 0)));
            fog.setSuffocationDamage((float) ability.getStatData("suffocation_damage").getValue());
            fog.setTremorTicks(ability.getRankModifierData("frostbite").isEnabled() ? (int) Math.max(1D, ability.getStatData("tremor").getValue() * 20D) : 0);
            fog.setStack(stack);
            fog.setPos(pos.x, y, pos.z);

            entity.level().addFreshEntity(fog);
            spawned = true;

            ability.getStatisticData().getMetricData("fog_clouds").addValue(1);
            this.getRelicData(entity, stack).getLevelingData().addExperience("fog", "fog_creation", 0.1D / steps);
        }

        this.setLastFogPos(stack, spawned ? last.add(direction.scale(steps * fogRadius)) : current);
    }

    private void tickGaze(LivingEntity owner, ItemStack stack) {
        var ability = this.getRelicData(owner, stack).getAbilitiesData().getAbilityData("gaze");
        var charges = this.getGazeCharges(stack);
        var changed = false;

        if (!ability.canPlayerUse(owner)) {
            if (!charges.isEmpty()) {
                charges.clear();
                this.setGazeCharges(stack, charges);
            }

            return;
        }

        var seenThisTick = new java.util.HashSet<String>();
        var range = ability.getStatData("range").getValue();
        var maxCharges = Math.max(1, (int) MathUtils.round(ability.getStatData("charges").getValue(), 0));

        for (var target : owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(range),
                entity -> entity != owner && entity.isAlive() && TargetingUtils.canHarm(owner, entity, stack, "gaze"))) {
            var key = target.getStringUUID();
            var looking = isLookingAt(target, owner) && target.hasLineOfSight(owner);
            var data = charges.getOrDefault(key, GazeChargeData.EMPTY);

            if (!looking)
                continue;

            seenThisTick.add(key);

            var lookTicks = data.lookTicks() + 1;
            var currentCharges = data.charges();

            if (lookTicks >= 20) {
                lookTicks = 0;

                if (currentCharges < maxCharges) {
                    currentCharges++;
                    ability.getStatisticData().getMetricData("charges_applied").addValue(1);
                    this.getRelicData(owner, stack).getLevelingData().addExperience("gaze", "eye_contact", 1);
                }
            }

            charges.put(key, new GazeChargeData(currentCharges, lookTicks, 0));
            changed = true;
        }

        var iterator = charges.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();

            if (seenThisTick.contains(entry.getKey()))
                continue;

            var data = entry.getValue();
            var decayTicks = data.decayTicks() + 1;
            var currentCharges = data.charges();

            if (decayTicks >= 20) {
                decayTicks = 0;
                currentCharges--;
            }

            if (currentCharges <= 0)
                iterator.remove();
            else
                entry.setValue(new GazeChargeData(currentCharges, 0, decayTicks));

            changed = true;
        }

        if (changed)
            this.setGazeCharges(stack, charges);
    }

    private static boolean isLookingAt(LivingEntity watcher, LivingEntity target) {
        var toTarget = target.getEyePosition().subtract(watcher.getEyePosition());

        if (toTarget.lengthSqr() <= 1.0E-6D)
            return false;

        var look = watcher.getLookAngle().normalize();
        var direction = toTarget.normalize();

        return look.dot(direction) > 0.85D;
    }

    @Builder
    public record GazeChargeData(int charges, int lookTicks, int decayTicks) {
        public static final GazeChargeData EMPTY = new GazeChargeData(0, 0, 0);

        public static final Codec<GazeChargeData> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("charges").forGetter(GazeChargeData::charges),
                        Codec.INT.fieldOf("look_ticks").forGetter(GazeChargeData::lookTicks),
                        Codec.INT.fieldOf("decay_ticks").forGetter(GazeChargeData::decayTicks)
                ).apply(instance, GazeChargeData::new)
        );
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDamageModifiers(LivingDamageEvent.Pre event) {
            var target = event.getEntity();
            var source = event.getSource().getEntity();

            if (!target.level().isClientSide() && target.getPersistentData().getLong(FOG_SUFFOCATION_UNTIL_TAG) >= target.level().getGameTime()
                    && event.getSource().is(DamageTypes.IN_WALL)) {
                var modifier = target.getPersistentData().getFloat(FOG_SUFFOCATION_DAMAGE_TAG);

                event.setNewDamage((float) (event.getNewDamage() * (1D + modifier)));
            }

            if (!target.level().isClientSide()) {
                var targetId = target.getStringUUID();

                for (var player : target.level().players()) {
                    for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.GHOSTLY_MANTLE.get())) {
                        var relic = (GhostlyMantleItem) stack.getItem();
                        var marks = relic.getReprisalMarks(stack);
                        var modifier = marks.remove(targetId);

                        if (modifier == null)
                            continue;

                        var original = event.getNewDamage();
                        var modified = original * (1D + modifier);
                        var extra = modified - original;

                        event.setNewDamage((float) modified);
                        relic.setReprisalMarks(stack, marks);

                        var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("spectral_escape");

                        ability.getStatisticData().getMetricData("reprisal_damage").addValue(extra);

                        if (extra > 0D) {
                            var experience = player.getRandom().nextInt((int) (extra + 1D));

                            if (experience > 0)
                                relic.getRelicData(player, stack).getLevelingData().addExperience("spectral_escape", "reprisal_damage", experience);
                        }

                        return;
                    }
                }
            }

            if (!(target instanceof Player player) || !(source instanceof LivingEntity attacker) || source == target)
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.GHOSTLY_MANTLE.get())) {
                var relic = (GhostlyMantleItem) stack.getItem();
                var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("gaze");

                if (!ability.canPlayerUse(player))
                    continue;

                var data = relic.getGazeCharges(stack).get(attacker.getStringUUID());

                if (data == null || data.charges() <= 0)
                    continue;

                var original = event.getNewDamage();
                var reduction = data.charges() * ability.getStatData("weakness").getValue();

                event.setNewDamage((float) Math.max(0D, original * (1D - reduction)));
                ability.getStatisticData().getMetricData("damage_reduced").addValue(original - event.getNewDamage());

                return;
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof Player player))
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.GHOSTLY_MANTLE.get())) {
                var relic = (GhostlyMantleItem) stack.getItem();
                var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("gaze");

                if (!ability.canPlayerUse(player) || !ability.getRankModifierData("dread").isEnabled())
                    continue;

                if (!TargetingUtils.canHarm(player, event.getEntity(), stack, "gaze"))
                    continue;

                var charges = relic.getTotalCharges(stack);

                if (charges <= 0)
                    continue;

                var extra = event.getNewDamage() * charges * ability.getStatData("damage").getValue();

                event.setNewDamage((float) (event.getNewDamage() + extra));
                ability.getStatisticData().getMetricData("damage_bonus").addValue(extra);

                return;
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            var target = event.getEntity();

            if (target.level().isClientSide())
                return;

            if (!(target instanceof Player player))
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.GHOSTLY_MANTLE.get())) {
                var relic = (GhostlyMantleItem) stack.getItem();
                var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("spectral_escape");

                if (!ability.canPlayerUse(player) || relic.getEscapeCooldown(stack) > 0)
                    continue;

                event.setCanceled(true);

                player.setHealth(Math.max(1F, player.getHealth()));
                player.deathTime = 0;

                var duration = (int) Math.max(1D, ability.getStatData("duration").getValue() * 20D);

                player.addEffect(new MobEffectInstance(RelicsMobEffects.FLIGHT, duration, 0, false, false, true));
                player.addEffect(new MobEffectInstance(RelicsMobEffects.IMMORTALITY, duration, 0, false, false, true));
                relic.setEscapeCooldown(stack, (int) Math.max(1D, ability.getStatData("cooldown").getValue() * 20D));

                ability.getStatisticData().getMetricData("prevented_deaths").addValue(1);

                var leveling = relic.getRelicData(player, stack).getLevelingData();
                var experience = leveling.getTotalExperienceBetweenLevels(leveling.getLevel(), leveling.getLevel() + 1) * 0.05D;

                relic.getRelicData(player, stack).getLevelingData().addExperience("spectral_escape", "death_prevention", experience);

                if (ability.getRankModifierData("reprisal").isEnabled() && event.getSource().getEntity() instanceof LivingEntity attacker && attacker != player
                        && TargetingUtils.canHarm(player, attacker, stack, "spectral_escape")) {
                    var marks = relic.getReprisalMarks(stack);

                    marks.put(attacker.getStringUUID(), ability.getStatData("damage").getValue());
                    relic.setReprisalMarks(stack, marks);
                }

                return;
            }
        }
    }
}
