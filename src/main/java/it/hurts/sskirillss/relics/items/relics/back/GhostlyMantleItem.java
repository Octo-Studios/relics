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
import it.hurts.sskirillss.relics.entities.GhostlyFogEntity;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
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

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("fog")
                                .modes("enabled", "disabled")
                                .rankModifier(1, "frostbite")
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(2D, 4D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 12D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("radius")
                                        .initialValue(0.75D, 1.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("air_loss")
                                        .initialValue(1D, 2D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 8D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("suffocation_damage")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("tremor")
                                        .initialValue(1D, 2D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 8D)
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
                                        .star(0, 4, 23).star(1, 7, 17).star(2, 13, 20).star(3, 18, 14).star(4, 12, 8)
                                        .link(0, 1).link(1, 2).link(2, 3).link(3, 4)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("gaze")
                                .requiredLevel(5)
                                .rankModifier(3, "dread")
                                .stat(AbilityStatTemplate.builder("range")
                                        .initialValue(8D, 12D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 32D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("charges")
                                        .initialValue(2D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("weakness")
                                        .initialValue(0.03D, 0.05D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(0.01D, 0.02D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
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
                                        .star(0, 3, 9).star(1, 8, 7).star(2, 12, 13).star(3, 16, 7).star(4, 21, 10).star(5, 12, 23)
                                        .link(0, 1).link(1, 2).link(2, 3).link(3, 4).link(2, 5)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("spectral_escape")
                                .requiredLevel(10)
                                .rankModifier(5, "reprisal")
                                .stat(AbilityStatTemplate.builder("cooldown")
                                        .initialValue(120D, 90D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 30D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(2D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 8D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("damage")
                                        .initialValue(0.15D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("escape")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("reprisal")
                                                .rankModifierVisibilityState("reprisal", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("escapes")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("reprisal_marks")
                                                .rankModifierVisibilityState("reprisal", VisibilityState.OBFUSCATED)
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 4).star(1, 5, 12).star(2, 17, 12).star(3, 8, 21).star(4, 14, 21)
                                        .link(0, 1).link(0, 2).link(1, 3).link(2, 4).link(3, 4)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxRank(5)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.SCULK)
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
    }

    private void tickFogTrail(LivingEntity entity, ItemStack stack) {
        var ability = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("fog");

        if (!ability.canPlayerUse(entity) || ability.getMode().equals("disabled"))
            return;

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
        var maxJumpHeight = 0D;
        var upwardSpeed = entity.getAttributeValue(Attributes.JUMP_STRENGTH);

        while (upwardSpeed > 0D) {
            maxJumpHeight += upwardSpeed;
            upwardSpeed = (upwardSpeed - entity.getAttributeValue(Attributes.GRAVITY)) * 0.98D;
        }

        for (var i = 1; i <= steps; i++) {
            var pos = last.add(direction.scale(i * fogRadius));
            var y = pos.y;
            var groundFound = false;

            for (var j = 0D; j <= maxJumpHeight; j += 0.25D) {
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
            fog.setPos(pos.x, y, pos.z);

            entity.level().addFreshEntity(fog);

            ability.getStatisticData().getMetricData("fog_clouds").addValue(1);
            this.getRelicData(entity, stack).getLevelingData().addExperience("fog", "fog_creation", 1D / Math.max(1, steps));
        }

        this.setLastFogPos(stack, last.add(direction.scale(steps * fogRadius)));
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
                entity -> entity != owner && entity.isAlive() && (!(owner instanceof Player player) || !EntityUtils.isAlliedTo(player, entity)))) {
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

                        event.setNewDamage((float) (event.getNewDamage() * (1D + modifier)));
                        relic.setReprisalMarks(stack, marks);

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
                var reduction = Math.min(0.9D, data.charges() * ability.getStatData("weakness").getValue());

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

                ability.getStatisticData().getMetricData("escapes").addValue(1);
                relic.getRelicData(player, stack).getLevelingData().addExperience("spectral_escape", "escape", 1);

                if (ability.getRankModifierData("reprisal").isEnabled() && event.getSource().getEntity() instanceof LivingEntity attacker && attacker != player) {
                    var marks = relic.getReprisalMarks(stack);

                    marks.put(attacker.getStringUUID(), ability.getStatData("damage").getValue());
                    relic.setReprisalMarks(stack, marks);

                    ability.getStatisticData().getMetricData("reprisal_marks").addValue(1);
                    relic.getRelicData(player, stack).getLevelingData().addExperience("spectral_escape", "reprisal", 1);
                }

                return;
            }
        }
    }
}
