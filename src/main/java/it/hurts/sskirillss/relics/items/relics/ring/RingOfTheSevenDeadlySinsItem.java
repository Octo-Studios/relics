package it.hurts.sskirillss.relics.items.relics.ring;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.utility.ContainerSlotClickEvent;
import it.hurts.sskirillss.relics.api.relics.*;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.misc.ICreativeTabContent;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.ring_of_the_seven_deadly_sins.C2SHurtPlayer;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.common.inventory.CurioSlot;

import java.util.ArrayList;

public class RingOfTheSevenDeadlySinsItem extends WearableRelicItem implements ICreativeTabContent {
    private static ResourceLocation getGluttonyAttributeId(ItemStack stack, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, SlotContext slotContext) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID,
                BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()
                        + "_" + BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()).getPath()
                        + "_" + slotContext.identifier()
                        + "_" + slotContext.index()
                        + "_gluttony");
    }

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("pride")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("multiplier")
                                        .initialValue(0.025D, 0.05D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("height_advantage")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("additional_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_received")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 20, 4).star(1, 20, 18).star(2, 20, 28).star(3, 6, 25).star(4, 2, 14).star(5, 8, 10).star(6, 14, 3).star(7, 3, 4)
                                        .link(5, 4).link(4, 3).link(3, 2).link(3, 1).link(3, 0).link(3, 6).link(5, 7)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("envy")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("outgoing_damage_multiplier")
                                        .initialValue(0.005D, 0.015D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0667D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("incoming_damage_multiplier")
                                        .initialValue(0.75D, 0.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.05D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("health_gap")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("offensive_shift")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("defensive_shift")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 5, 21).star(1, 5, 14).star(2, 12, 7).star(3, 19, 12).star(4, 19, 20).star(5, 16, 28)
                                        .link(0, 1).link(1, 2).link(2, 3).link(3, 4).link(4, 5)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("wrath")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("window")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("early_multiplier")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.3D)
                                        .formatValue(value -> MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("late_multiplier")
                                        .initialValue(1D, 0.75D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.0667D)
                                        .formatValue(value -> MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("timing")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("windows")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("bonus_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("reduced_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 11, 4).star(1, 19, 5).star(2, 18, 19).star(3, 11, 28).star(4, 10, 10)
                                        .link(3, 2).link(4, 2).link(4, 0).link(4, 1)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("sloth")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("time")
                                        .initialValue(10D, 7.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.0867D)
                                        .formatValue(value -> MathUtils.round(value, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("speed")
                                        .initialValue(1D, 0.75D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.0667D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("immortality")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("immortality_duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_from_moving")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 13, 3).star(1, 5, 25).star(2, 11, 14).star(3, 3, 19).star(4, 13, 23).star(5, 7, 7).star(6, 18, 9)
                                        .link(0, 2).link(2, 1).link(2, 5).link(2, 3).link(2, 4).link(2, 6)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("greed")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("luck")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.7D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("looting")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.7D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("chance")
                                        .initialValue(0.1D, 0.075D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.0867D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("ore")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("mob")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("nullification")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("nullified_tables")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 12, 4).star(1, 19, 10).star(2, 6, 16).star(3, 13, 22).star(4, 19, 17).star(5, 4, 7).star(6, 7, 10)
                                        .link(0, 1).link(1, 2).link(2, 3).link(3, 4).link(5, 0).link(5, 6)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("gluttony")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("early_multiplier")
                                        .initialValue(0.0025, 0.005D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1.9D)
                                        .formatValue(value -> MathUtils.round(value * 100, 2))
                                        .build())
                                .stat(AbilityStatTemplate.builder("late_multiplier")
                                        .initialValue(0.1D, 0.05)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.08D)
                                        .formatValue(value -> MathUtils.round(value * 100, 2))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("food")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("positive_duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("negative_duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 2, 14).star(1, 5, 22).star(2, 11, 21).star(3, 12, 14).star(4, 9, 12).star(5, 5, 12).star(6, 13, 8).star(7, 4, 8)
                                        .link(0, 1).link(2, 3).link(0, 5).link(3, 4).link(5, 7).link(4, 6)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("lust")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("amount")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("time")
                                        .initialValue(360D, 420D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 30D)
                                        .formatValue(value -> MathUtils.round(value, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("offspring")
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("extra_offspring")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("breeding_attempts")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 13, 7).star(1, 11, 12).star(2, 7, 7).star(3, 7, 14).star(4, 15, 12).star(5, 19, 7).star(6, 19, 13).star(7, 2, 16).star(8, 13, 23).star(9, 20, 17)
                                        .link(6, 5).link(5, 4).link(4, 0).link(0, 1).link(1, 2).link(2, 3).link(3, 7).link(7, 8).link(8, 9).link(9, 6)
                                        .build())
                                .build())
                        .build())
                .statistic(RelicStatisticTemplate.builder()
                        .metric(RelicMetricTemplate.builder("retention_time") // FIXME @RelicTemplate
                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                .component((entity, stack) -> Component.translatable("relics.description.statistic.relic.retention_time"))
                                .build())
                        .metric(RelicMetricTemplate.builder("unequip_attempts")
                                .formatValue((value) -> String.valueOf(value.intValue()))
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxRank(0)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_NETHER, LootEntries.NETHER_LIKE)
                        .build())
                .build();
    }

    public int getHurtTimer(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_HURT_TIMER.get(), 0);
    }

    public void setHurtTimer(ItemStack stack, int value) {
        stack.set(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_HURT_TIMER.get(), Math.max(0, value));
    }

    public void addHurtTimer(ItemStack stack, int value) {
        this.setHurtTimer(stack, Math.max(this.getHurtTimer(stack), value));
    }

    public static int getHurtTimer(Player player) {
        var max = 0;

        for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get()))
            max = Math.max(max, stack.getOrDefault(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_HURT_TIMER.get(), 0));

        return max;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();

        if (entity instanceof Player player && !player.level().isClientSide()) {
            var foodLevel = player.getFoodData().getFoodLevel();
            var center = 10;

            var above = Math.max(0, foodLevel - center);
            var below = Math.max(0, center - foodLevel);

            var positive = above * this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gluttony").getStatData("early_multiplier").getValue();
            var negative = below * this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("gluttony").getStatData("late_multiplier").getValue();

            var modifier = Math.clamp(positive - negative, -0.9D, 1.0D);

            if (modifier > 0.0001D)
                this.getRelicData(player, stack).getAbilitiesData().getAbilityData("gluttony").getStatisticData().getMetricData("positive_duration").addValue(1D / 20D);
            else if (modifier < -0.0001D)
                this.getRelicData(player, stack).getAbilitiesData().getAbilityData("gluttony").getStatisticData().getMetricData("negative_duration").addValue(1D / 20D);

            var blacklist = RelicsConfigs.RELICS_CONFIG.getRingOfSDSGluttonyAttributesBlacklist();

            for (var instance : entity.getAttributes().attributes.values()) {
                var attribute = instance.getAttribute();

                if (blacklist.contains(attribute.getRegisteredName()))
                    continue;

                EntityUtils.resetAttribute(entity, attribute, (float) modifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, getGluttonyAttributeId(stack, attribute, slotContext));
            }
        }

        var slothData = stack.getOrDefault(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_SLOTH.get(), SlothData.create(entity.position(), entity.getYRot(), entity.getXRot(), 0));

        var positionDelta = entity.position().distanceToSqr(new Vec3(slothData.x(), slothData.y(), slothData.z()));
        var rotationDelta = Math.abs(entity.getYRot() - slothData.yaw()) + Math.abs(entity.getXRot() - slothData.pitch());

        var stillTicks = slothData.stillTicks();
        var gainedImmortality = false;

        if (positionDelta > 0.0001D || rotationDelta > 0.001F) {
            stillTicks = 0;
        } else {
            stillTicks++;

            var requiredTicks = Math.max(1, (int) Math.round(this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("sloth").getStatData("time").getValue() * 20D));
            var hasImmortality = entity.hasEffect(RelicsMobEffects.IMMORTALITY);

            if (stillTicks >= requiredTicks) {
                entity.addEffect(new MobEffectInstance(RelicsMobEffects.IMMORTALITY, 5, 0, false, false, true));

                if (!hasImmortality)
                    gainedImmortality = true;
            }
        }

        stack.set(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_SLOTH.get(), slothData.with(entity.position(), entity.getYRot(), entity.getXRot(), stillTicks));

        if (gainedImmortality)
            this.getRelicData(entity, stack).getLevelingData().addExperience("sloth", "immortality", 1);

        if (entity.hasEffect(RelicsMobEffects.IMMORTALITY))
            this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("sloth").getStatisticData().getMetricData("immortality_duration").addValue(1D / 20D);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        var timer = this.getHurtTimer(stack);

        if (timer > 0)
            this.setHurtTimer(stack, timer - 1);
    }

    @Override
    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
        var entity = slotContext.entity();

        if (entity == null)
            return super.getFortuneLevel(slotContext, lootContext, stack);

        return (int) Math.round(this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("greed").getStatData("luck").getValue());
    }

    @Override
    public int getLootingLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
        var entity = slotContext.entity();

        if (entity == null)
            return super.getLootingLevel(slotContext, lootContext, stack);

        return (int) Math.round(this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("greed").getStatData("looting").getValue());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (stack.getItem() == newStack.getItem())
            return;

        var entity = slotContext.entity();

        for (var instance : entity.getAttributes().attributes.values())
            EntityUtils.removeAttribute(entity, instance.getAttribute(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, getGluttonyAttributeId(stack, instance.getAttribute(), slotContext));
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();

        var hasFreeSlot = false;

        var curios = CuriosApi.getCuriosInventory(entity);

        if (curios.isPresent()) {
            var handler = curios.get();

            var rings = handler.getStacksHandler("ring");

            if (rings.isPresent()) {
                var stacks = rings.get().getStacks();

                for (var i = 0; i < stacks.getSlots(); i++) {
                    if (stacks.getStackInSlot(i).isEmpty()) {
                        hasFreeSlot = true;

                        break;
                    }
                }
            }
        }

        return hasFreeSlot;
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

            if (verticalDelta <= 0)
                return;

            var attackerRings = EntityUtils.findEquippedCurios(attacker, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get());

            if (!attackerRings.isEmpty()) {
                var totalBonus = 0D;
                var ringBonuses = new ArrayList<Double>(attackerRings.size());

                for (var stack : attackerRings) {
                    var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                    var perBlock = relic.getRelicData(attacker, stack).getAbilitiesData().getAbilityData("pride").getStatData("multiplier").getValue();
                    var bonus = verticalDelta * perBlock;

                    ringBonuses.add(bonus);
                    totalBonus += bonus;
                }

                if (totalBonus > 0) {
                    event.setNewDamage((float) (original * (1 + totalBonus)));

                    for (var i = 0; i < attackerRings.size(); i++) {
                        var stack = attackerRings.get(i);
                        var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                        var extraDamage = original * ringBonuses.get(i);

                        relic.getRelicData(attacker, stack).getLevelingData().addExperience("pride", "height_advantage", extraDamage);
                        relic.getRelicData(attacker, stack).getAbilitiesData().getAbilityData("pride").getStatisticData().getMetricData("additional_damage").addValue(extraDamage);
                    }

                    return;
                }
            }

            if (target instanceof LivingEntity victim) {
                var victimRings = EntityUtils.findEquippedCurios(victim, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get());

                if (victimRings.isEmpty())
                    return;

                var totalPenalty = 0D;
                var ringPenalties = new ArrayList<Double>(victimRings.size());

                for (var stack : victimRings) {
                    var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                    var perBlock = relic.getRelicData(victim, stack).getAbilitiesData().getAbilityData("pride").getStatData("multiplier").getValue();
                    var penalty = verticalDelta * perBlock;

                    ringPenalties.add(penalty);
                    totalPenalty += penalty;
                }

                if (totalPenalty <= 0)
                    return;

                event.setNewDamage((float) (original * (1 + totalPenalty)));

                for (var i = 0; i < victimRings.size(); i++) {
                    var stack = victimRings.get(i);
                    var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                    var extraDamage = original * ringPenalties.get(i);

                    relic.getRelicData(victim, stack).getLevelingData().addExperience("pride", "height_advantage", Math.abs(extraDamage));
                    relic.getRelicData(victim, stack).getAbilitiesData().getAbilityData("pride").getStatisticData().getMetricData("damage_received").addValue(extraDamage);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingHurt2(LivingDamageEvent.Pre event) {
            var target = event.getEntity();
            var speed = target.getKnownMovement().multiply(1, 0, 1).length();

            if (speed <= 0)
                return;

            var rings = EntityUtils.findEquippedCurios(target, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get());

            if (rings.isEmpty())
                return;

            var baseDamage = event.getNewDamage();
            var totalPenalty = 0D;
            var ringPenalties = new ArrayList<Double>(rings.size());

            for (var stack : rings) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                var speedMultiplier = relic.getRelicData(target, stack).getAbilitiesData().getAbilityData("sloth").getStatData("speed").getValue();
                var penalty = speed * speedMultiplier;

                ringPenalties.add(penalty);
                totalPenalty += penalty;
            }

            if (totalPenalty <= 0)
                return;

            event.setNewDamage((float) (baseDamage * (1 + totalPenalty)));

            for (var i = 0; i < rings.size(); i++) {
                var stack = rings.get(i);
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                var extraDamage = baseDamage * ringPenalties.get(i);

                relic.getRelicData(target, stack).getAbilitiesData().getAbilityData("sloth").getStatisticData().getMetricData("damage_from_moving").addValue(extraDamage);
            }
        }

        @SubscribeEvent
        public static void onLivingHurt3(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity attacker))
                return;

            var rings = EntityUtils.findEquippedCurios(attacker, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get());

            if (rings.isEmpty())
                return;

            var now = attacker.level().getGameTime();
            var baseDamage = event.getNewDamage();
            var totalModifier = 1D;
            var ringModifiers = new ArrayList<Double>(rings.size());

            for (var stack : rings) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                var windowTicks = Math.max(1, (int) Math.round(relic.getRelicData(attacker, stack).getAbilitiesData().getAbilityData("wrath").getStatData("window").getValue() * 20D));
                var state = stack.getOrDefault(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_WRATH.get(), WrathData.create(now));
                var delta = now - state.lastHitTick();

                double modifier = 1D;

                if (delta < windowTicks) {
                    var mid = windowTicks / 2D;
                    var earlyMultiplier = relic.getRelicData(attacker, stack).getAbilitiesData().getAbilityData("wrath").getStatData("early_multiplier").getValue();
                    var lateMultiplier = relic.getRelicData(attacker, stack).getAbilitiesData().getAbilityData("wrath").getStatData("late_multiplier").getValue();

                    if (delta <= mid) {
                        var t = delta / mid;
                        var start = 1D + earlyMultiplier;

                        modifier = start + (1D - start) * t;
                    } else {
                        var t = (delta - mid) / Math.max(1D, windowTicks - mid);
                        var end = 1D - lateMultiplier;

                        modifier = 1D + (end - 1D) * t;
                    }
                }

                ringModifiers.add(modifier);
                totalModifier *= modifier;
            }

            event.setNewDamage((float) (baseDamage * totalModifier));

            for (var i = 0; i < rings.size(); i++) {
                var stack = rings.get(i);
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                var modifier = ringModifiers.get(i);
                var extraDamage = baseDamage * (modifier - 1D);

                relic.getRelicData(attacker, stack).getAbilitiesData().getAbilityData("wrath").getStatisticData().getMetricData("windows").addValue(1);
                relic.getRelicData(attacker, stack).getLevelingData().addExperience("wrath", "timing", Math.abs(extraDamage));

                if (extraDamage > 0)
                    relic.getRelicData(attacker, stack).getAbilitiesData().getAbilityData("wrath").getStatisticData().getMetricData("bonus_damage").addValue(extraDamage);
                else if (extraDamage < 0)
                    relic.getRelicData(attacker, stack).getAbilitiesData().getAbilityData("wrath").getStatisticData().getMetricData("reduced_damage").addValue(Math.abs(extraDamage));

                stack.set(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_WRATH.get(), new WrathData(now));
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
            var rings = EntityUtils.findEquippedCurios(wearer, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get());

            if (rings.isEmpty())
                return false;

            var wearerHp = wearer.getHealth();
            var otherHp = other.getHealth();
            var diff = (otherHp - wearerHp) / 5F;
            var stat = wearerIsAttacker ? "outgoing_damage_multiplier" : "incoming_damage_multiplier";
            var modifiers = new ArrayList<Double>(rings.size());
            var totalModifier = 0D;

            for (var stack : rings) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                var perPoint = Math.abs(relic.getRelicData(wearer, stack).getAbilitiesData().getAbilityData("envy").getStatData(stat).getValue());
                var modifier = (wearerIsAttacker ? diff : -diff) * perPoint;

                modifiers.add(modifier);

                if (modifier > 0)
                    totalModifier += modifier;
            }

            if (totalModifier <= 0)
                return false;

            var baseDamage = event.getNewDamage();

            event.setNewDamage((float) (baseDamage * (1 + totalModifier)));

            for (var i = 0; i < rings.size(); i++) {
                var modifier = modifiers.get(i);

                if (modifier <= 0)
                    continue;

                var stack = rings.get(i);
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();
                var extraDamage = baseDamage * modifier;

                relic.getRelicData(wearer, stack).getLevelingData().addExperience("envy", "health_gap", extraDamage);

                if (wearerIsAttacker)
                    relic.getRelicData(wearer, stack).getAbilitiesData().getAbilityData("envy").getStatisticData().getMetricData("offensive_shift").addValue(extraDamage);
                else
                    relic.getRelicData(wearer, stack).getAbilitiesData().getAbilityData("envy").getStatisticData().getMetricData("defensive_shift").addValue(extraDamage);
            }

            return true;
        }

        @SubscribeEvent
        public static void onBabySpawn(BabyEntitySpawnEvent event) {
            var player = event.getCausedByPlayer();

            if (player == null)
                return;

            long totalDeadlineExtension = 0L;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                var parentA = event.getParentA();
                var parentB = event.getParentB();
                var child = event.getChild();

                var level = player.level();
                var random = level.getRandom();

                if (child != null) {
                    var maxOffspring = Math.max(1, (int) Math.round(relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("lust").getStatData("amount").getValue()));
                    var count = random.nextInt(maxOffspring + 1);

                    for (int i = 0; i < count; i++) {
                        var extra = (AgeableMob) child.getType().create(level);

                        if (extra == null)
                            continue;

                        extra.setBaby(true);
                        extra.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), random.nextFloat() * 360F, 0F);
                        level.addFreshEntity(extra);
                    }

                    relic.getRelicData(player, stack).getLevelingData().addExperience("lust", "offspring", count);
                    relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("lust").getStatisticData().getMetricData("extra_offspring").addValue(count);
                }

                relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("lust").getStatisticData().getMetricData("breeding_attempts").addValue(1);

                totalDeadlineExtension += (long) (relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("lust").getStatData("time").getValue() * 20);
            }

            if (totalDeadlineExtension > 0L) {
                var deadline = player.level().getGameTime() + totalDeadlineExtension;

                CommonEvents.applyLustDeadline(event.getParentA(), deadline);
                CommonEvents.applyLustDeadline(event.getParentB(), deadline);
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
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            var player = event.getPlayer();
            var state = event.getState();

            if (!state.is(Tags.Blocks.ORES))
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                relic.getRelicData(player, stack).getLevelingData().addExperience("greed", "ore", 1);
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getSource().getEntity() instanceof Player player))
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                relic.getRelicData(player, stack).getLevelingData().addExperience("greed", "mob", 1);
            }
        }

        @SubscribeEvent
        public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
            var entity = event.getEntity();

            if (!(entity instanceof Player player))
                return;

            var consumed = event.getItem();
            var foodProperties = consumed.getFoodProperties(entity);

            if (foodProperties == null)
                return;

            var nutrition = foodProperties.nutrition();

            if (nutrition <= 0)
                return;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get())) {
                var relic = (RingOfTheSevenDeadlySinsItem) stack.getItem();

                relic.getRelicData(player, stack).getLevelingData().addExperience("gluttony", "food", nutrition);
            }
        }

        @SubscribeEvent
        public static void onSlotClick(ContainerSlotClickEvent event) {
            var entity = event.getEntity();

            if (entity.isCreative() || !(event.getSlot() instanceof CurioSlot slot)
                    || !(event.getSlotStack().getItem() instanceof RingOfTheSevenDeadlySinsItem))
                return;

            if (entity.level().isClientSide())
                NetworkHandler.sendToServer(new C2SHurtPlayer(slot.getIdentifier(), slot.getSlotIndex()));

            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onLivingHurt4(LivingDamageEvent.Post event) {
            RingOfTheSevenDeadlySinsItem.CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            RingOfTheSevenDeadlySinsItem.CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            RingOfTheSevenDeadlySinsItem.CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onBlockBreakAttempt(PlayerEvent.BreakSpeed event) {
            RingOfTheSevenDeadlySinsItem.CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            RingOfTheSevenDeadlySinsItem.CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onItemToss(ItemTossEvent event) {
            RingOfTheSevenDeadlySinsItem.CommonEvents.onInteract(event.getPlayer());
        }

        @SubscribeEvent
        public static void onItemPickup(ItemEntityPickupEvent.Post event) {
            RingOfTheSevenDeadlySinsItem.CommonEvents.onInteract(event.getPlayer());
        }

        private static void onInteract(LivingEntity entity) {
            if (entity.getCommandSenderWorld().isClientSide())
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.RING_OF_THE_SEVEN_DEADLY_SINS.get()))
                ServerScheduler.schedule(1, () -> stack.set(RelicsDataComponents.RING_OF_THE_SEVEN_DEADLY_SINS_SLOTH.get(), SlothData.create(entity.position(), entity.getYRot(), entity.getXRot(), 0)));
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
