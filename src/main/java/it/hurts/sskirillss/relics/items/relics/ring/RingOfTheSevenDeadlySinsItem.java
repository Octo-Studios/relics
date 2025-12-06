package it.hurts.sskirillss.relics.items.relics.ring;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.common.ContainerSlotClickEvent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.client.postEffects.SevenDeadlySinsPostEffect;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.misc.ICreativeTabContent;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class RingOfTheSevenDeadlySinsItem extends RelicItem implements ICreativeTabContent {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("pride")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("multiplier")
                                        .initialValue(0.1D, 0.15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("envy")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("difference_multiplier")
                                        .initialValue(0.02D, 0.03D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("wrath")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("window")
                                        .initialValue(3D, 4D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("early_multiplier")
                                        .initialValue(1.5D, 2.0D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("late_multiplier")
                                        .initialValue(0.6D, 0.8D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("sloth")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("time")
                                        .initialValue(2D, 4D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("speed")
                                        .initialValue(0.5D, 0.75D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("greed")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("luck")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("looting")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("chance")
                                        .initialValue(0.05D, 0.12D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("gluttony")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("early_multiplier")
                                        .initialValue(0.02D, 0.04D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("late_multiplier")
                                        .initialValue(0.02D, 0.04D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("lust")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("amount")
                                        .initialValue(2D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0857D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("time")
                                        .initialValue(60D, 120D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 8.2142D)
                                        .formatValue(value -> MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_NETHER, LootEntries.NETHER_LIKE)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();

        if (entity instanceof Player player) {
            if (this.canPlayerUseAbility(player, stack, "gluttony")) {
                var foodLevel = player.getFoodData().getFoodLevel();
                var center = 10;

                var above = Math.max(0, foodLevel - center);
                var below = Math.max(0, center - foodLevel);

                var positive = above * this.getStatValue(entity, stack, "gluttony", "early_multiplier");
                var negative = below * this.getStatValue(entity, stack, "gluttony", "late_multiplier");

                var modifier = Math.clamp(positive - negative, -0.9D, 1.0D);

                var blacklist = Lists.newArrayList(Attributes.GRAVITY);

                for (var instance : player.getAttributes().attributes.values()) {
                    var attribute = instance.getAttribute();

                    if (blacklist.contains(attribute))
                        continue;

                    EntityUtils.resetAttribute(entity, stack, attribute, (float) modifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                }
            }
        }

        if (this.canPlayerUseAbility(entity, stack, "sloth")) {
            var slothData = stack.getOrDefault(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_SLOTH.get(), SlothData.create(entity.position(), entity.getYRot(), entity.getXRot(), 0));

            var positionDelta = entity.position().distanceToSqr(new Vec3(slothData.x(), slothData.y(), slothData.z()));
            var rotationDelta = Math.abs(entity.getYRot() - slothData.yaw()) + Math.abs(entity.getXRot() - slothData.pitch());

            var stillTicks = slothData.stillTicks();

            if (positionDelta > 0.0001D || rotationDelta > 0.001F) {
                stillTicks = 0;
            } else {
                stillTicks++;

                var requiredTicks = Math.max(1, (int) Math.round(this.getStatValue(entity, stack, "sloth", "time") * 20D));

                if (stillTicks >= requiredTicks)
                    entity.addEffect(new MobEffectInstance(RelicsMobEffects.IMMORTALITY, 5, 0, false, false, true));
            }

            stack.set(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_SLOTH.get(), slothData.with(entity.position(), entity.getYRot(), entity.getXRot(), stillTicks));
        }
    }

    @Override
    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
        var entity = slotContext.entity();

        if (entity == null || !this.canPlayerUseAbility(entity, stack, "greed"))
            return super.getFortuneLevel(slotContext, lootContext, stack);

        return (int) Math.round(this.getStatValue(entity, stack, "greed", "luck"));
    }

    @Override
    public int getLootingLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
        var entity = slotContext.entity();

        if (entity == null || !this.canPlayerUseAbility(entity, stack, "greed"))
            return super.getLootingLevel(slotContext, lootContext, stack);

        return (int) Math.round(this.getStatValue(entity, stack, "greed", "looting"));
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (stack.getItem() == newStack.getItem())
            return;

        var entity = slotContext.entity();

        for (var instance : entity.getAttributes().attributes.values())
            EntityUtils.removeAttribute(entity, stack, instance.getAttribute(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        private static final String LUST_DEADLINE = Relics.MODID + "LustDeadline";

        @SubscribeEvent
        public static void onLivingHurt1(LivingDamageEvent.Pre event) {
            var target = event.getEntity();
            var sourceEntity = event.getSource().getEntity();

            if (!(sourceEntity instanceof LivingEntity attacker) || attacker == target)
                return;

            var original = event.getNewDamage();
            var targetEye = target.getEyeY();
            var attackerEye = attacker.getEyeY();
            var verticalDelta = attackerEye - targetEye;

            if (verticalDelta > 0) {
                for (var stack : EntityUtils.findEquippedCurios(attacker, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                    var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                    if (!relic.canPlayerUseAbility(attacker, stack, "pride"))
                        continue;

                    var perBlock = relic.getStatValue(attacker, stack, "pride", "multiplier");
                    var bonus = verticalDelta * perBlock;

                    event.setNewDamage((float) (original * (1 + bonus)));

                    return;
                }
            }

            if (verticalDelta > 0 && target instanceof LivingEntity victim) {
                for (var stack : EntityUtils.findEquippedCurios(victim, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                    var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                    if (!relic.canPlayerUseAbility(victim, stack, "pride"))
                        continue;

                    var perBlock = relic.getStatValue(victim, stack, "pride", "multiplier");
                    var penalty = verticalDelta * perBlock;

                    event.setNewDamage((float) (original * (1 + penalty)));

                    break;
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt2(LivingDamageEvent.Pre event) {
            var target = event.getEntity();
            var damage = event.getNewDamage();

            for (var stack : EntityUtils.findEquippedCurios(target, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                if (!relic.canPlayerUseAbility(target, stack, "sloth"))
                    continue;

                var speed = target.getKnownMovement().multiply(1, 0, 1).length();
                var speedMultiplier = relic.getStatValue(target, stack, "sloth", "speed");

                if (speed > 0) {
                    var penalty = speed * speedMultiplier;

                    event.setNewDamage((float) (damage * (1 + penalty)));
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt3(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity attacker))
                return;

            for (var stack : EntityUtils.findEquippedCurios(attacker, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                if (!relic.canPlayerUseAbility(attacker, stack, "wrath"))
                    continue;

                var now = attacker.level().getGameTime();

                var windowTicks = Math.max(1, (int) Math.round(relic.getStatValue(attacker, stack, "wrath", "window") * 20D));
                var state = stack.getOrDefault(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_WRATH.get(), WrathData.create(now));

                var delta = now - state.lastHitTick();

                double modifier = 1.0D;

                if (delta < windowTicks) {
                    double mid = windowTicks / 2.0D;

                    var earlyMultiplier = relic.getStatValue(attacker, stack, "wrath", "early_multiplier");
                    var lateMultiplier = relic.getStatValue(attacker, stack, "wrath", "late_multiplier");

                    if (delta <= mid) {
                        var t = delta / mid;

                        modifier = earlyMultiplier + (1.0D - earlyMultiplier) * t;
                    } else {
                        var t = (delta - mid) / Math.max(1.0D, windowTicks - mid);

                        modifier = 1.0D + (lateMultiplier - 1.0D) * t;
                    }
                }

                event.setNewDamage((float) (event.getNewDamage() * modifier));

                stack.set(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_WRATH.get(), new WrathData(now));

                break;
            }
        }

        @SubscribeEvent
        public static void onLivingHurt4(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity attacker))
                return;

            var target = event.getEntity();

            if (CommonEvents.applyEnvy(event, attacker, target, true))
                return;

            CommonEvents.applyEnvy(event, target, attacker, false);
        }

        private static boolean applyEnvy(LivingDamageEvent.Pre event, LivingEntity wearer, LivingEntity other, boolean wearerIsAttacker) {
            for (var stack : EntityUtils.findEquippedCurios(wearer, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                if (!relic.canPlayerUseAbility(wearer, stack, "envy"))
                    continue;

                var wearerHp = wearer.getHealth();
                var otherHp = other.getHealth();

                var diff = otherHp - wearerHp;

                var perPoint = Math.abs(relic.getStatValue(wearer, stack, "envy", "difference_multiplier"));
                var modifier = (wearerIsAttacker ? diff : -diff) * perPoint;

                event.setNewDamage((float) (event.getNewDamage() * (1 + modifier)));

                return true;
            }

            return false;
        }

        @SubscribeEvent
        public static void onBabySpawn(BabyEntitySpawnEvent event) {
            var player = event.getCausedByPlayer();

            if (player == null)
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                if (!relic.canPlayerUseAbility(player, stack, "lust"))
                    continue;

                var parentA = event.getParentA();
                var parentB = event.getParentB();
                var child = event.getChild();

                var level = player.level();
                var random = level.getRandom();

                if (child != null) {
                    var maxOffspring = Math.max(1, (int) Math.round(relic.getStatValue(player, stack, "lust", "amount")));
                    var count = random.nextInt(maxOffspring + 1);

                    for (int i = 0; i < count; i++) {
                        var extra = (AgeableMob) child.getType().create(level);

                        if (extra == null)
                            continue;

                        extra.setBaby(true);
                        extra.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), random.nextFloat() * 360F, 0F);
                        level.addFreshEntity(extra);
                    }
                }

                var deadline = level.getGameTime() + (long) (relic.getStatValue(player, stack, "lust", "time") * 20);

                CommonEvents.applyLustDeadline(parentA, deadline);
                CommonEvents.applyLustDeadline(parentB, deadline);
            }
        }

        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            if (!(event.getEntity() instanceof Animal animal) || animal.level().isClientSide())
                return;

            var data = animal.getPersistentData();

            if (!data.contains(LUST_DEADLINE))
                return;

            var deadline = data.getLong(LUST_DEADLINE);

            if (animal.level().getGameTime() > deadline && animal.isAlive())
                animal.kill();
        }

        @SubscribeEvent
        public static void onSlitClick(ContainerSlotClickEvent event) {
            var entity = event.getEntity();

            if (entity.isCreative() || !(event.getSlotStack().getItem() instanceof RingOfTheSevenDeadlySinsItem) || !(event.getSlot() instanceof CurioSlot))
                return;

            if (entity.hurt(entity.level().damageSources().magic(), 1F))
                SevenDeadlySinsPostEffect.TIMER = 40;

            event.setCanceled(true);
        }

        private static void applyLustDeadline(Mob entity, long deadline) {
            if (!(entity instanceof Animal animal))
                return;

            animal.getPersistentData().putLong(LUST_DEADLINE, deadline);
        }
    }

    public record SlothData(double x, double y, double z, float yaw, float pitch, int stillTicks) {
        public static final Codec<SlothData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(SlothData::x),
                Codec.DOUBLE.fieldOf("y").forGetter(SlothData::y),
                Codec.DOUBLE.fieldOf("z").forGetter(SlothData::z),
                Codec.FLOAT.fieldOf("yaw").forGetter(SlothData::yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(SlothData::pitch),
                Codec.INT.fieldOf("still_ticks").forGetter(SlothData::stillTicks)
        ).apply(instance, SlothData::new));

        public static SlothData create(Vec3 pos, float yaw, float pitch, int stillTicks) {
            return new SlothData(pos.x, pos.y, pos.z, yaw, pitch, stillTicks);
        }

        public SlothData with(Vec3 pos, float yaw, float pitch, int stillTicks) {
            return new SlothData(pos.x, pos.y, pos.z, yaw, pitch, stillTicks);
        }
    }

    public record WrathData(long lastHitTick) {
        public static final Codec<WrathData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("last_hit_tick").forGetter(WrathData::lastHitTick)
        ).apply(instance, WrathData::new));

        public static WrathData create(long tick) {
            return new WrathData(tick);
        }
    }
}