package it.hurts.sskirillss.relics.items.relics.back;

import it.hurts.sskirillss.relics.api.events.leveling.AbilityModeSwitchEvent;
import it.hurts.sskirillss.relics.api.relics.MetricTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.StatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.ConstellationStarEntity;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.FallingStarEntity;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.midnight_mantle.S2CSyncConstellation;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Scheduler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
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
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

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
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("attack_speed")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("max_health")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("health_regeneration")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("modifier")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .statistic(StatisticTemplate.builder()
                                        .metric(MetricTemplate.builder("duration_new_moon")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(MetricTemplate.builder("duration_full_moon")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(MetricTemplate.builder("additional_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(MetricTemplate.builder("health_regeneration")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("invisibility")
                                .rankModifier(3, "strike")
                                .stat(StatTemplate.builder("brightness")
                                        .thresholdValue(0D, 1D)
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("cooldown")
                                        .thresholdValue(0D, Double.MAX_VALUE)
                                        .initialValue(15D, 10D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), -0.05D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .statistic(StatisticTemplate.builder()
                                        .metric(MetricTemplate.builder("duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(MetricTemplate.builder("additional_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("constellation")
                                .rankModifier(5, "stun")
                                .stat(StatTemplate.builder("stars_amount")
                                        .initialValue(2D, 7D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.05D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("ability_cooldown")
                                        .thresholdValue(0D, Double.MAX_VALUE)
                                        .initialValue(120D, 180D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), -0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("tremor_duration")
                                        .initialValue(0.25D, 1D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("explosion_radius")
                                        .initialValue(0.5D, 1D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("explosion_damage")
                                        .initialValue(1D, 5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("star_lifetime")
                                        .initialValue(10D, 15D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("stun_duration")
                                        .initialValue(1D, 2.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("starfall")
                                .rankModifier(7, "bounce")
                                .stat(StatTemplate.builder("chance")
                                        .thresholdValue(0D, 1D)
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.025D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("bounce_chance")
                                        .initialValue(0.05D, 0.15D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
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
        return stack.getOrDefault(DataComponentRegistry.MIDNIGHT_MANTLE_INVISIBILITY_COOLDOWN.get(), 0);
    }

    public void setInvisibilityCooldown(ItemStack stack, int cooldown) {
        stack.set(DataComponentRegistry.MIDNIGHT_MANTLE_INVISIBILITY_COOLDOWN.get(), cooldown);
    }

    public void addInvisibilityCooldown(ItemStack stack, int cooldown) {
        this.setInvisibilityCooldown(stack, this.getInvisibilityCooldown(stack) + cooldown);
    }

    public int getConstellationCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.MIDNIGHT_MANTLE_CONSTELLATION_COOLDOWN.get(), 0);
    }

    public void setConstellationCooldown(ItemStack stack, int cooldown) {
        stack.set(DataComponentRegistry.MIDNIGHT_MANTLE_CONSTELLATION_COOLDOWN.get(), cooldown);
    }

    public void addConstellationCooldown(ItemStack stack, int cooldown) {
        this.setConstellationCooldown(stack, this.getConstellationCooldown(stack) + cooldown);
    }

    public int getPhaseDuration(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.MIDNIGHT_MANTLE_PHASE_DURATION, 0);
    }

    public void setPhaseDuration(ItemStack stack, int duration) {
        stack.set(DataComponentRegistry.MIDNIGHT_MANTLE_PHASE_DURATION, Math.max(duration, 0));
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
            } else if (this.canHideInTheDarkness(entity, stack))
                entity.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 5, 0, false, false));
        }

        if (this.canPlayerUseAbility(entity, stack, "constellation")) {
            if (this.getConstellationCooldown(stack) > 0)
                this.addConstellationCooldown(stack, -1);
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

                Scheduler.schedule(1, () -> relic.setInvisibilityCooldown(stack, (int) (relic.getStatValue(entity, stack, "invisibility", "cooldown") * 20)));
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

                event.setAmount((float) (event.getAmount() + (event.getAmount() * relic.getModeEffectiveness(entity, stack))));
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

                event.setAmount((float) (event.getAmount() * (1F + (relic.getStatValue(entity, stack, "phase", "attack_damage") * relic.getModeEffectiveness(entity, stack)))));
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

                event.setAmount((float) (event.getAmount() * (1F + relic.getStatValue(entity, stack, "invisibility", "damage"))));

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
        public static void onLivingHurt4(LivingIncomingDamageEvent event) {
            if (event.getAmount() < 1D || !(event.getSource().getEntity() instanceof LivingEntity))
                return;

            var entity = event.getEntity();

            var level = entity.level();
            var random = level.getRandom();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "constellation") || relic.getConstellationCooldown(stack) > 0)
                    continue;

                var stars = new ArrayList<ConstellationStarEntity>();
                var targets = new ArrayList<Vec3>();

                var count = Math.max(random.nextInt((int) relic.getStatValue(entity, stack, "constellation", "stars_amount")), 2);
                var radius = 3F + (count * 0.25F);
                var apexHeight = radius / 3F;
                var gravity = 0.1F;

                var startPos = entity.getEyePosition();
                var groundY = entity.position().y();
                var centerPos = new Vec3(startPos.x(), groundY, startPos.z());

                var minSep = radius / (float) Math.sqrt(count);

                for (var i = 0; i < count; i++) {
                    Vec3 targetPos = null;

                    for (int step = 0; step < 10; step++) {
                        var angle = random.nextDouble() * Math.PI * 2;

                        var randomFactor = random.nextDouble();
                        var radialDistance = radius * Math.sqrt(randomFactor);

                        var tx = centerPos.x() + Math.cos(angle) * radialDistance;
                        var tz = centerPos.z() + Math.sin(angle) * radialDistance;

                        final var candidate = new Vec3(tx, groundY, tz);

                        if (targets.stream().noneMatch(tp -> tp.distanceTo(candidate) < minSep)) {
                            targetPos = candidate;

                            break;
                        }
                    }

                    if (targetPos == null)
                        continue;

                    targets.add(targetPos);

                    var deltaXZ = targetPos.subtract(startPos).multiply(1, 0, 1);
                    var distanceXZ = deltaXZ.length();
                    var directionXZ = deltaXZ.normalize();

                    var startY = startPos.y();
                    var deltaY = groundY - startY;
                    var heightDiffToApex = (Math.max(startY, groundY) + apexHeight) - startY;
                    var flightTime = Math.sqrt((4 * heightDiffToApex - 2 * deltaY) / gravity);

                    var verticalVelocity = (deltaY + 0.5 * gravity * flightTime * flightTime) / flightTime;
                    var horizontalSpeed = distanceXZ / flightTime;
                    var motion = new Vec3(directionXZ.x() * horizontalSpeed, verticalVelocity, directionXZ.z() * horizontalSpeed);

                    var star = new ConstellationStarEntity(RelicsEntities.CONSTELLATION_STAR.get(), level);

                    star.setDamage((float) relic.getStatValue(entity, stack, "constellation", "explosion_damage"));
                    star.setRadius((float) relic.getStatValue(entity, stack, "constellation", "explosion_radius"));
                    star.setTremor((float) relic.getStatValue(entity, stack, "constellation", "tremor_duration"));
                    star.setLifetime((int) relic.getStatValue(entity, stack, "constellation", "star_lifetime"));
                    star.setFlawless(relic.isRelicFlawless(entity, stack));
                    star.setDeltaMovement(motion);
                    star.setCenter(centerPos);
                    star.setPos(startPos);
                    star.setOwner(entity);

                    level.addFreshEntity(star);

                    stars.add(star);
                }

                var uuids = stars.stream()
                        .map(Entity::getUUID)
                        .toList();

                for (var star : stars) {
                    star.setConstellation(uuids);

                    if (level instanceof ServerLevel serverLevel)
                        NetworkHandler.sendToClientsTrackingEntity(new S2CSyncConstellation(star.getId(), star.getConstellation().stream().map(serverLevel::getEntity).filter(Objects::nonNull).map(Entity::getId).collect(Collectors.toList())), star);
                }

                relic.setConstellationCooldown(stack, (int) (relic.getStatValue(entity, stack, "constellation", "ability_cooldown") * 20));
            }
        }

        @SubscribeEvent
        public static void onLivingHurt5(LivingIncomingDamageEvent event) {
            if (event.getAmount() < 1D || !(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            var target = event.getEntity();

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
                star.setPos(pos);

                if (relic.isAbilityRankModifierUnlocked(entity, stack, "starfall", "bounce"))
                    star.setBounceChance((float) relic.getStatValue(entity, stack, "starfall", "bounce_chance"));

                level.addFreshEntity(star);
            }
        }
    }
}