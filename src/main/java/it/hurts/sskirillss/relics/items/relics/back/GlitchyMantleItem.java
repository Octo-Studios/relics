package it.hurts.sskirillss.relics.items.relics.back;

import com.mojang.blaze3d.shaders.FogShape;
import it.hurts.sskirillss.relics.api.events.utility.FluidCollisionEvent;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

public class GlitchyMantleItem extends WearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("glitch")
                                .rankModifier(1, "shockwave")
                                .rankModifier(3, "projectile")
                                .rankModifier(5, "phase")
                                .modes("enabled", "disabled")
                                .stat(AbilityStatTemplate.builder("stability_time")
                                        .initialValue(3D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 12D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("fall_damage")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("shockwave_radius")
                                        .initialValue(0.15D, 0.35D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1.5D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("shockwave_damage")
                                        .initialValue(0.5D, 1.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 7.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("projectile_damage")
                                        .initialValue(0.02D, 0.05D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.35D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("phase_time")
                                        .initialValue(1.5D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("forced_falls")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("solid_air_duration")
                                                .formatValue(value -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("shockwave_damage")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("shockwave", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("projectile_bonus")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("projectile", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("phase_rollbacks")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("phase", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("phase_duration")
                                                .formatValue(value -> MathUtils.formatTime(value.intValue()))
                                                .rankModifierVisibilityState("phase", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.END_LIKE)
                        .entry(LootEntries.THE_END)
                        .build())
                .build();
    }

    public int getStandTicks(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_STAND_TICKS, 0);
    }

    public void setStandTicks(ItemStack stack, int ticks) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_STAND_TICKS, Math.max(0, ticks));
    }

    public void addStandTicks(ItemStack stack, int ticks) {
        this.setStandTicks(stack, this.getStandTicks(stack) + ticks);
    }

    public boolean isForcedFall(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_FORCED_FALL, false);
    }

    public void setForcedFall(ItemStack stack, boolean state) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_FORCED_FALL, state);
    }

    public double getForcedFallStartY(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_FORCED_FALL_START_Y, 0D);
    }

    public void setForcedFallStartY(ItemStack stack, double y) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_FORCED_FALL_START_Y, y);
    }

    public int getPhaseTicks(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_PHASE_TICKS, 0);
    }

    public void setPhaseTicks(ItemStack stack, int ticks) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_PHASE_TICKS, Math.max(0, ticks));
    }

    public void addPhaseTicks(ItemStack stack, int ticks) {
        this.setPhaseTicks(stack, this.getPhaseTicks(stack) + ticks);
    }

    public Vec3 getLastSafePosition(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_LAST_SAFE_POS, Vec3.ZERO);
    }

    public void setLastSafePosition(ItemStack stack, Vec3 pos) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_LAST_SAFE_POS, pos);
    }

    private static boolean areEyesInsideCollidingBlock(LivingEntity entity) {
        var level = entity.level();
        var pos = BlockPos.containing(entity.getEyePosition());

        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    public boolean canCollideWithAirLike(LivingEntity entity, ItemStack stack) {
        return this.isGlitchEnabled(entity, stack) && !entity.isShiftKeyDown() && !this.isForcedFall(stack);
    }

    public boolean canPhaseThroughBlocks(LivingEntity entity, ItemStack stack) {
        return this.isGlitchEnabled(entity, stack)
                && !this.isForcedFall(stack)
                && this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glitch").getRankModifierData("phase").isEnabled();
    }

    public boolean isGlitchEnabled(LivingEntity entity, ItemStack stack) {
        var ability = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glitch");

        return ability.canPlayerUse(entity) && ability.getMode().equals("enabled");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        if (!(slotContext.entity() instanceof LivingEntity entity))
            return;

        var level = entity.level();
        var belowState = level.getBlockState(entity.getBlockPosBelowThatAffectsMyMovement());

        var ability = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glitch");
        var forcedFall = this.isForcedFall(stack);
        var onGround = entity.onGround();

        if (!this.isGlitchEnabled(entity, stack)) {
            this.setStandTicks(stack, 0);
            this.setForcedFall(stack, false);
            this.setPhaseTicks(stack, 0);

            return;
        }

        var horizontalSpeed = entity.getDeltaMovement().horizontalDistanceSqr();
        var stable = onGround && belowState.isAir() && horizontalSpeed < 1.0E-4D && !entity.isShiftKeyDown();

        if (!forcedFall) {
            if (stable) {
                this.addStandTicks(stack, 1);

                if (this.getStandTicks(stack) >= (int) Math.max(1D, ability.getStatData("stability_time").getValue() * 20D)) {
                    this.setForcedFall(stack, true);
                    this.setForcedFallStartY(stack, entity.getY());
                    this.setStandTicks(stack, 0);

                    if (!level.isClientSide())
                        this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glitch").getStatisticData().getMetricData("forced_falls").addValue(1);
                }
            } else {
                this.setStandTicks(stack, 0);
            }
        }

        if (this.isForcedFall(stack) && onGround && !belowState.isAir() && entity.fallDistance <= 0F)
            this.setForcedFall(stack, false);

        if (level.isClientSide())
            return;

        if (onGround && belowState.isAir() && this.canCollideWithAirLike(entity, stack))
            ability.getStatisticData().getMetricData("solid_air_duration").addValue(1);

        if (ability.getRankModifierData("phase").isEnabled()) {
            var eyesInsideBlock = areEyesInsideCollidingBlock(entity);

            if (!eyesInsideBlock) {
                this.setLastSafePosition(stack, entity.position());
                this.setPhaseTicks(stack, 0);
            } else {
                ability.getStatisticData().getMetricData("phase_duration").addValue(1);
                this.addPhaseTicks(stack, 1);

                var limit = Math.max(1, (int) Math.round(ability.getStatData("phase_time").getValue() * 20D));

                if (this.getPhaseTicks(stack) > limit) {
                    var pos = this.getLastSafePosition(stack);

                    if (entity instanceof ServerPlayer serverPlayer) {
                        serverPlayer.teleportTo(serverPlayer.serverLevel(), pos.x(), pos.y(), pos.z(), entity.getYRot(), entity.getXRot());
                        serverPlayer.setDeltaMovement(Vec3.ZERO);
                    } else {
                        entity.teleportTo(pos.x(), pos.y(), pos.z());
                        entity.setDeltaMovement(Vec3.ZERO);
                    }

                    this.setPhaseTicks(stack, 0);
                    ability.getStatisticData().getMetricData("phase_rollbacks").addValue(1);
                }
            }
        }
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
            if (!event.getContainer().getSource().is(DamageTypes.IN_WALL))
                return;

            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.GLITCHY_MANTLE.get())) {
                var relic = (GlitchyMantleItem) stack.getItem();
                var ability = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glitch");

                if (relic.isGlitchEnabled(entity, stack) && ability.getRankModifierData("phase").isEnabled()) {
                    event.setCanceled(true);

                    return;
                }
            }
        }

        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event) {
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.GLITCHY_MANTLE.get())) {
                var relic = (GlitchyMantleItem) stack.getItem();

                if (!relic.isGlitchEnabled(entity, stack) || !relic.isForcedFall(stack))
                    continue;

                var ability = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glitch");
                var extraMultiplier = 1D + ability.getStatData("fall_damage").getValue();

                event.setDamageMultiplier((float) (event.getDamageMultiplier() * extraMultiplier));

                if (ability.getRankModifierData("shockwave").isEnabled()) {
                    var fallenBlocks = Math.max(0D, relic.getForcedFallStartY(stack) - entity.getY());

                    if (fallenBlocks > 0.5D) {
                        var totalRadius = fallenBlocks * ability.getStatData("shockwave_radius").getValue();
                        var totalDamage = fallenBlocks * ability.getStatData("shockwave_damage").getValue();
                        var area = new AABB(entity.blockPosition()).inflate(totalRadius, 1D, totalRadius);

                        for (var target : entity.level().getEntitiesOfClass(LivingEntity.class, area)) {
                            if (target == entity)
                                continue;

                            target.hurt(entity.damageSources().mobAttack(entity), (float) totalDamage);
                        }

                        ability.getStatisticData().getMetricData("shockwave_damage").addValue(totalDamage);
                    }
                }

                relic.setForcedFall(stack, false);
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getDirectEntity() instanceof Projectile projectile)
                    || !(projectile.getOwner() instanceof LivingEntity source)) {
                return;
            }

            var target = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.GLITCHY_MANTLE.get())) {
                var relic = (GlitchyMantleItem) stack.getItem();
                var ability = relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("glitch");

                if (!relic.isGlitchEnabled(source, stack))
                    continue;

                if (!ability.getRankModifierData("projectile").isEnabled())
                    continue;

                if (!source.level().getBlockState(source.getBlockPosBelowThatAffectsMyMovement()).isAir())
                    continue;

                var height = Math.max(0D, source.getY() - target.getY());
                var bonus = event.getNewDamage() * ability.getStatData("projectile_damage").getValue() * height;

                event.setNewDamage((float) (event.getNewDamage() + bonus));

                ability.getStatisticData().getMetricData("projectile_bonus").addValue(bonus);
            }
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onBlockScreenEffect(RenderBlockScreenEffectEvent event) {
            for (var stack : EntityUtils.findEquippedCurios(event.getPlayer(), RelicsItems.GLITCHY_MANTLE.get())) {
                if (!(stack.getItem() instanceof GlitchyMantleItem relic))
                    continue;

                if (relic.isGlitchEnabled(event.getPlayer(), stack)
                        && relic.getRelicData(event.getPlayer(), stack).getAbilitiesData().getAbilityData("glitch").getRankModifierData("phase").isEnabled()) {
                    event.setCanceled(true);

                    return;
                }
            }
        }
    }
}
