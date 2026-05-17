package it.hurts.sskirillss.relics.items.relics.back;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.AbilityConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.RelicConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.stats.SynergyStatTemplate;
import it.hurts.sskirillss.relics.entities.GlitchyIllusionEntity;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

public class GlitchyMantleItem extends WearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("distortion")
                                .rankModifier(1, "disorientation")
                                .stat(AbilityStatTemplate.builder("miss_chance")
                                        .initialValue(0.1D, 0.25D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("miss")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("misses")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 13, 25).star(1, 8, 25).star(2, 8, 18).star(3, 15, 19).star(4, 10, 10)
                                        .link(1, 2).link(2, 4).link(4, 3).link(3, 0)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("illusion")
                                .rankModifier(3, "echo")
                                .modes("enabled", "disabled")
                                .requiredLevel(5)
                                .stat(AbilityStatTemplate.builder("interval")
                                        .initialValue(15D, 10D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun")
                                        .initialValue(0.5D, 1D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun_radius")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("echo_radius")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2.5D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("creation")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("detonation")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("echo_attack")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .rankModifierVisibilityState("echo", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("illusions")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("target_detonations")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("owner_detonations")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_dealt")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .rankModifierVisibilityState("echo", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("stun_duration")
                                                .formatValue(value -> MathUtils.formatTime(value.intValue()))
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("echo_attacks")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("echo", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 7, 28).star(1, 3, 24).star(2, 7, 19).star(3, 14, 25).star(4, 2, 17).star(5, 4, 9).star(6, 10, 11).star(7, 11, 15).star(8, 13, 5).star(9, 17, 8).star(10, 17, 12).star(11, 6, 13)
                                        .link(0, 1).link(1, 2).link(2, 3).link(2, 11).link(11, 4).link(11, 6).link(6, 7).link(11, 5).link(5, 8).link(6, 9).link(7, 10)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("glitch")
                                .rankModifier(5, "phase")
                                .modes("enabled", "disabled")
                                .requiredLevel(10)
                                .stat(AbilityStatTemplate.builder("stability_time")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("fall_damage")
                                        .initialValue(2.5D, 2D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("phase_time")
                                        .initialValue(2.5D, 5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("air_walking")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("phasing")
                                                .modeVisibilityState("disabled", VisibilityState.HIDDEN)
                                                .rankModifierVisibilityState("phase", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("forced_falls")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("solid_air_duration")
                                                .formatValue(value -> MathUtils.formatTime(value.intValue()))
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
                                .research(ResearchTemplate.builder()
                                        .star(0, 10, 28).star(1, 1, 18).star(2, 20, 19).star(3, 20, 13).star(4, 17, 10).star(5, 10, 16).star(6, 5, 13).star(7, 17, 6).star(8, 14, 2).star(9, 7, 4).star(10, 10, 8)
                                        .link(1, 0).link(0, 2).link(2, 3).link(3, 4).link(4, 5).link(5, 6).link(7, 4).link(7, 8).link(8, 9).link(6, 10).link(10, 9).link(1, 6)
                                        .build())
                                .build())
                        .synergy(SynergyTemplate.builder("electricity")
                                .modes("enabled", "disabled")
                                .stat(SynergyStatTemplate.builder("damage")
                                        .thresholdValue(2.5D, 10D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .condition(RelicConditionTemplate.builder(RelicsItems.GLITCHY_MANTLE::get)
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("illusion")
                                                .build())
                                        .build())
                                .condition(RelicConditionTemplate.builder(RelicsItems.JELLYFISH_NECKLACE::get)
                                        .container(RelicsRelicContainers.CURIOS.get())
                                        .condition(AbilityConditionTemplate.builder("shock")
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

    public long getLastAirTick(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_LAST_AIR_TICK, Long.MIN_VALUE);
    }

    public void setLastAirTick(ItemStack stack, long tick) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_LAST_AIR_TICK, tick);
    }

    public long getLastPhaseTick(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_LAST_PHASE_TICK, Long.MIN_VALUE);
    }

    public void setLastPhaseTick(ItemStack stack, long tick) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_LAST_PHASE_TICK, tick);
    }

    public Vec3 getLastSafePosition(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_LAST_SAFE_POS, Vec3.ZERO);
    }

    public void setLastSafePosition(ItemStack stack, Vec3 pos) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_LAST_SAFE_POS, pos);
    }

    public int getIllusionCooldown(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.GLITCHY_MANTLE_ILLUSION_COOLDOWN, 0);
    }

    public void setIllusionCooldown(ItemStack stack, int cooldown) {
        stack.set(RelicsDataComponents.GLITCHY_MANTLE_ILLUSION_COOLDOWN, Math.max(0, cooldown));
    }

    public void addIllusionCooldown(ItemStack stack, int cooldown) {
        this.setIllusionCooldown(stack, this.getIllusionCooldown(stack) + cooldown);
    }

    private static boolean areEyesInsideCollidingBlock(LivingEntity entity) {
        var level = entity.level();
        var pos = BlockPos.containing(entity.getEyePosition());

        return !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isMovingHorizontally(LivingEntity entity) {
        var knownMovement = entity.getKnownMovement().multiply(1D, 0D, 1D).lengthSqr();

        return knownMovement > 1.0E-4D;
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
        var glitchEnabled = this.isGlitchEnabled(entity, stack);

        if (!glitchEnabled) {
            this.setStandTicks(stack, 0);
            this.setForcedFall(stack, false);
            this.setPhaseTicks(stack, 0);
        } else {
            var stable = onGround && belowState.isAir() && !isMovingHorizontally(entity) && !entity.isShiftKeyDown();

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
        }

        if (level.isClientSide())
            return;

        var gameTime = level.getGameTime();

        if (glitchEnabled && belowState.isAir() && this.canCollideWithAirLike(entity, stack) && this.getLastAirTick(stack) != gameTime) {
            this.setLastAirTick(stack, gameTime);

            ability.getStatisticData().getMetricData("solid_air_duration").addValue(1D / 20D);

            if (isMovingHorizontally(entity))
                this.getRelicData(entity, stack).getLevelingData().addExperience("glitch", "air_walking", 1D / 20D);
        }

        var illusion = this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("illusion");

        if (illusion.canPlayerUse(entity) && illusion.getMode().equals("enabled")) {
            if (this.getIllusionCooldown(stack) > 0) {
                this.addIllusionCooldown(stack, -1);
            } else {
                var look = entity.getLookAngle().multiply(1D, 0D, 1D);
                var backward = look.lengthSqr() > 1.0E-6D ? look.normalize().scale(-0.95D) : Vec3.ZERO;
                var spawnPos = entity.position().add(backward);
                var copy = new GlitchyIllusionEntity(RelicsEntities.GLITCHY_ILLUSION.get(), level);

                copy.setOwner(entity);
                copy.setLifetime((int) Math.max(1D, illusion.getStatData("duration").getValue() * 20D));
                copy.setStunDuration((int) Math.max(1D, illusion.getStatData("stun").getValue() * 20D));
                copy.setStunRadius((float) Math.max(0D, illusion.getStatData("stun_radius").getValue()));
                copy.setFlawless(this.getRelicData(entity, stack).isVisuallyFlawless());
                copy.setPos(spawnPos.x(), spawnPos.y(), spawnPos.z());
                copy.setYRot(entity.getYRot());
                copy.setXRot(entity.getXRot());
                copy.setYHeadRot(entity.getYHeadRot());
                copy.setSnapshotXRot(entity.getXRot());
                copy.setPose(entity.getPose());

                if (level.getEntitiesOfClass(GlitchyIllusionEntity.class, copy.getBoundingBox().inflate(0.05D)).isEmpty()) {
                    level.addFreshEntity(copy);

                    this.setIllusionCooldown(stack, (int) Math.max(1D, illusion.getStatData("interval").getValue() * 20D));
                    illusion.getStatisticData().getMetricData("illusions").addValue(1);
                    this.getRelicData(entity, stack).getLevelingData().addExperience("illusion", "creation", 1);
                }
            }
        } else {
            this.setIllusionCooldown(stack, 0);
        }

        if (glitchEnabled && ability.getRankModifierData("phase").isEnabled()) {
            var eyesInsideBlock = areEyesInsideCollidingBlock(entity);

            if (!eyesInsideBlock) {
                this.setLastSafePosition(stack, entity.position());
                this.setPhaseTicks(stack, 0);
            } else {
                if (this.getLastPhaseTick(stack) != gameTime) {
                    this.setLastPhaseTick(stack, gameTime);

                    ability.getStatisticData().getMetricData("phase_duration").addValue(1D / 20D);
                    this.getRelicData(entity, stack).getLevelingData().addExperience("glitch", "phasing", 1D / 20D);
                }

                entity.addEffect(new MobEffectInstance(RelicsMobEffects.GLITCH, 5, 0, false, false));

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

                    level.playSound(null, BlockPos.containing(pos), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 1F);

                    this.setPhaseTicks(stack, 0);
                    ability.getStatisticData().getMetricData("phase_rollbacks").addValue(1);
                }
            }
        }
    }

    @EventBusSubscriber
    public static class CommonEvents {
        private static final ThreadLocal<Boolean> ECHOING_ATTACK = ThreadLocal.withInitial(() -> false);

        @SubscribeEvent
        public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
            if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
                var attacker = event.getSource().getEntity() instanceof LivingEntity livingAttacker ? livingAttacker : null;

                if (attacker == null && event.getSource().getDirectEntity() instanceof Projectile projectile
                        && projectile.getOwner() instanceof LivingEntity projectileOwner) {
                    attacker = projectileOwner;
                }

                if (attacker != null && attacker != player) {
                    for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.GLITCHY_MANTLE.get())) {
                        var relic = (GlitchyMantleItem) stack.getItem();
                        var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("distortion");

                        if (!ability.canPlayerUse(player))
                            continue;

                        if (player.getRandom().nextDouble() >= ability.getStatData("miss_chance").getValue())
                            continue;

                        event.setCanceled(true);

                        player.addEffect(new MobEffectInstance(RelicsMobEffects.GLITCH, 10, 0, false, false));

                        ability.getStatisticData().getMetricData("misses").addValue(1);
                        relic.getRelicData(player, stack).getLevelingData().addExperience("distortion", "miss", 1);

                        if (ability.getRankModifierData("disorientation").isEnabled()) {
                            var rotation = attacker.getRandom().nextFloat() * 360F;

                            if (attacker instanceof ServerPlayer serverAttacker)
                                serverAttacker.teleportTo(serverAttacker.serverLevel(), serverAttacker.getX(), serverAttacker.getY(), serverAttacker.getZ(), rotation, serverAttacker.getXRot());

                            attacker.setYRot(rotation);
                            attacker.setYHeadRot(rotation);
                            attacker.setYBodyRot(rotation);
                            attacker.yRotO = rotation;
                            attacker.yHeadRotO = rotation;
                            attacker.yBodyRotO = rotation;

                            if (attacker instanceof Mob mob) {
                                mob.setTarget(null);

                                var radians = Math.toRadians(rotation);
                                var direction = new Vec3(-Math.sin(radians), 0D, Math.cos(radians));
                                var destination = mob.position().add(direction.scale(3D));

                                mob.getNavigation().stop();
                                mob.getNavigation().moveTo(destination.x(), destination.y(), destination.z(), 1D);
                            }
                        }

                        return;
                    }
                }
            }

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

            var hasSafeFallMantle = false;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.GLITCHY_MANTLE.get())) {
                var relic = (GlitchyMantleItem) stack.getItem();

                if (!relic.isGlitchEnabled(entity, stack))
                    continue;

                if (!relic.isForcedFall(stack)) {
                    hasSafeFallMantle = true;

                    continue;
                }

                var ability = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("glitch");
                var extraMultiplier = 1D + ability.getStatData("fall_damage").getValue();

                event.setDamageMultiplier((float) (event.getDamageMultiplier() * extraMultiplier));

                relic.setForcedFall(stack, false);

                return;
            }

            if (hasSafeFallMantle)
                event.setDamageMultiplier(0F);
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (ECHOING_ATTACK.get() || !(event.getSource().getEntity() instanceof Player source)) {
                return;
            }

            if (!event.getSource().is(DamageTypes.PLAYER_ATTACK) || event.getSource().getDirectEntity() != source)
                return;

            var target = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.GLITCHY_MANTLE.get())) {
                var relic = (GlitchyMantleItem) stack.getItem();
                var ability = relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("illusion");

                if (!ability.canPlayerUse(source) || !ability.getMode().equals("enabled") || !ability.getRankModifierData("echo").isEnabled())
                    continue;

                var radius = source.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) * ability.getStatData("echo_radius").getValue();
                var illusions = source.level().getEntitiesOfClass(GlitchyIllusionEntity.class, target.getBoundingBox().inflate(radius), illusion -> illusion.getOwner() == source)
                        .stream()
                        .sorted(java.util.Comparator.comparingDouble(illusion -> illusion.distanceToSqr(target)))
                        .toList();

                if (illusions.isEmpty())
                    return;

                ECHOING_ATTACK.set(true);

                var damageDealt = 0D;

                try {
                    for (var illusion : illusions) {
                        illusion.startEchoAttack(target, source.getMainHandItem());
                        target.invulnerableTime = 0;

                        if (target.hurt(event.getSource(), event.getNewDamage()))
                            damageDealt += event.getNewDamage();
                    }
                } finally {
                    ECHOING_ATTACK.set(false);
                }

                ability.getStatisticData().getMetricData("echo_attacks").addValue(illusions.size());
                ability.getStatisticData().getMetricData("damage_dealt").addValue(damageDealt);
                relic.getRelicData(source, stack).getLevelingData().addExperience("illusion", "echo_attack", illusions.size());

                return;
            }
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onBlockScreenEffect(RenderBlockScreenEffectEvent event) {
            if (event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.BLOCK)
                return;

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
