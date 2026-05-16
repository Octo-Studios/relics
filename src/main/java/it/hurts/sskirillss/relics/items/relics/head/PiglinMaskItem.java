package it.hurts.sskirillss.relics.items.relics.head;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.entities.GoldenToothEntity;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.WearableRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;

public class PiglinMaskItem extends WearableRelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("neutrality")
                                .rankModifier(1, "legion")
                                .initialMaxLevel(0)
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("target")
                                                .rankModifierVisibilityState("legion", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 6, 9).star(1, 16, 9).star(2, 11, 12).star(3, 3, 15).star(4, 19, 15).star(5, 11, 23)
                                        .link(2, 1).link(1, 4).link(4, 5).link(5, 3).link(3, 0).link(0, 2)
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("target")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .rankModifierVisibilityState("legion", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("barter")
                                .rankModifier(3, "pocket")
                                .stat(AbilityStatTemplate.builder("trades")
                                        .initialValue(1D, 3D)
                                        .targetValue(RelicsScalingModels.ADDITIVE.get(), 11.75D)
                                        .formatValue(Double::intValue)
                                        .build())
                                .stat(AbilityStatTemplate.builder("items_count")
                                        .initialValue(1D, 4D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 64.004D)
                                        .formatValue(Double::intValue)
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("trade")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("pickup")
                                                .rankModifierVisibilityState("pocket", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 9, 7).star(1, 17, 13).star(2, 7, 20)
                                        .link(0, 1).link(1, 2)
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("currency")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("items")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("looting")
                                .requiredLevel(5)
                                .rankModifier(5, "frenzy")
                                .stat(AbilityStatTemplate.builder("chance")
                                        .initialValue(0.1D, 0.15D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.75008D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("health")
                                        .initialValue(10D, 7.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2.5125D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("duration")
                                        .initialValue(5D, 10D)
                                        .targetValue(RelicsScalingModels.LOGARITHMIC.get(), 29.99998D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("attack_damage")
                                        .initialValue(0.005D, 0.01D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.02999D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("attack_speed")
                                        .initialValue(0.005D, 0.01D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.06999D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("drop")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("stack")
                                                .rankModifierVisibilityState("frenzy", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 12, 8).star(1, 4, 13).star(2, 13, 17).star(3, 18, 19).star(4, 14, 22)
                                        .link(2, 3).link(3, 0).link(0, 1).link(1, 4).link(4, 2)
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("teeth_dropped")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("teeth_picked_up")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("additional_damage")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("frenzy", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("effect_duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .rankModifierVisibilityState("frenzy", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("tripled_effect_duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .rankModifierVisibilityState("frenzy", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.BASTION)
                        .build())
                .build();
    }

    public int getStacks(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.PIGLIN_MASK_STACKS, 0);
    }

    public void setStacks(ItemStack stack, int stacks) {
        stack.set(RelicsDataComponents.PIGLIN_MASK_STACKS, Math.clamp(stacks, 0, PiglinMaskItem.getMaxStacks()));
    }

    public void addStacks(ItemStack stack, int stacks) {
        this.setStacks(stack, this.getStacks(stack) + stacks);
    }

    public static int getMaxStacks() {
        return 32;
    }

    public int getDuration(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.PIGLIN_MASK_DURATION, 0);
    }

    public void setDuration(LivingEntity entity, ItemStack stack, int duration) {
        stack.set(RelicsDataComponents.PIGLIN_MASK_DURATION, Math.clamp(duration, 0, this.getMaxDuration(entity, stack)));
    }

    public void addDuration(LivingEntity entity, ItemStack stack, int duration) {
        this.setDuration(entity, stack, this.getDuration(stack) + duration);
    }

    public int getMaxDuration(LivingEntity entity, ItemStack stack) {
        return (int) this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("looting").getStatData("duration").getValue();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();

        var duration = this.getDuration(stack);
        var stacks = this.getStacks(stack);

        if (entity.tickCount % 20 == 0) {
            if (duration > 0 || stacks > 0) {
                this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("looting").getStatisticData().getMetricData("effect_duration").addValue(1);

                if (stacks >= PiglinMaskItem.getMaxStacks())
                    this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("looting").getStatisticData().getMetricData("tripled_effect_duration").addValue(1);
            }

            if (duration > 0) {
                this.addDuration(entity, stack, -1);
            } else if (stacks > 0) {
                if (stacks >= PiglinMaskItem.getMaxStacks())
                    this.setStacks(stack, 0);
                else
                    this.addStacks(stack, -1);
            }

            var modifier = stacks >= PiglinMaskItem.getMaxStacks() ? 3 : 1;

            EntityUtils.resetAttribute(entity, stack, Attributes.ATTACK_SPEED, (float) (stacks * this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("looting").getStatData("attack_speed").getValue() * modifier), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (stack.getItem() == newStack.getItem())
            return;

        var entity = slotContext.entity();

        EntityUtils.removeAttribute(entity, stack, Attributes.ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return this.getRelicData(slotContext.entity(), stack).getAbilitiesData().getAbilityData("neutrality").canPlayerUse(slotContext.entity());
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDamage1(LivingDamageEvent.Post event) {
            var target = event.getEntity();

            if (!(event.getSource().getEntity() instanceof LivingEntity source) || target instanceof AbstractPiglin)
                return;

            var level = source.level();

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.PIGLIN_MASK.get())) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("neutrality").getRankModifierData("legion").isEnabled())
                    continue;

                for (var piglin : level.getEntitiesOfClass(Mob.class, source.getBoundingBox().inflate(32), mob -> mob instanceof Piglin || mob instanceof ZombifiedPiglin)) {
                    if (piglin.getTarget() != null || !piglin.hasLineOfSight(target) || piglin.isBaby())
                        continue;

                    relic.getRelicData(source, stack).getLevelingData().addExperience("neutrality", "target", 1);
                    relic.getRelicData(target, stack).getAbilitiesData().getAbilityData("neutrality").getStatisticData().getMetricData("target").addValue(1);

                    piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, target.getUUID());

                    piglin.setTarget(target);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamage2(LivingDamageEvent.Post event) {
            var source = event.getSource().getEntity();
            var target = event.getEntity();

            if (source == null)
                return;

            var level = source.level();

            for (var stack : EntityUtils.findEquippedCurios(target, RelicsItems.PIGLIN_MASK.get())) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.getRelicData(target, stack).getAbilitiesData().getAbilityData("neutrality").getRankModifierData("legion").isEnabled())
                    continue;

                for (var piglin : level.getEntitiesOfClass(Mob.class, target.getBoundingBox().inflate(32), mob -> mob instanceof Piglin || mob instanceof ZombifiedPiglin)) {
                    if (piglin.getTarget() != null || !piglin.hasLineOfSight(source) || piglin.isBaby())
                        continue;

                    relic.getRelicData(target, stack).getLevelingData().addExperience("neutrality", "target", 1);
                    relic.getRelicData(target, stack).getAbilitiesData().getAbilityData("neutrality").getStatisticData().getMetricData("target").addValue(1);

                    piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, source.getUUID());

                    if (source instanceof LivingEntity)
                        piglin.setTarget((LivingEntity) source);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamage3(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity source))
                return;

            var originalDamage = event.getNewDamage();

            if (originalDamage <= 0F)
                return;

            var uniqueStacks = new java.util.ArrayList<ItemStack>();

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.PIGLIN_MASK.get())) {
                if (uniqueStacks.stream().noneMatch(existing -> existing == stack))
                    uniqueStacks.add(stack);
            }

            var totalModifier = 0D;

            for (var stack : uniqueStacks) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("looting").getRankModifierData("frenzy").isEnabled())
                    continue;

                var stacks = relic.getStacks(stack);
                var multiplier = stacks >= PiglinMaskItem.getMaxStacks() ? 3 : 1;
                var modifier = originalDamage * relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("looting").getStatData("attack_damage").getValue() * stacks * multiplier;

                relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("looting").getStatisticData().getMetricData("additional_damage").addValue(modifier);

                totalModifier += modifier;
            }

            if (totalModifier > 0D)
                event.setNewDamage((float) (originalDamage + totalModifier));
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity source))
                return;

            var entity = event.getEntity();

            var level = source.level();
            if (level.isClientSide())
                return;

            var random = level.getRandom();
            var stacks = new java.util.ArrayList<ItemStack>();

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.PIGLIN_MASK.get())) {
                if (stacks.stream().noneMatch(existing -> existing == stack))
                    stacks.add(stack);
            }

            var totalAmount = 0;

            for (var stack : stacks) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("looting").canPlayerUse(source))
                    continue;

                var amount = MathUtils.multicast(random, relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("looting").getStatData("chance").getValue(), (int) Math.ceil(entity.getMaxHealth() / relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("looting").getStatData("health").getValue()));

                relic.getRelicData(source, stack).getAbilitiesData().getAbilityData("looting").getStatisticData().getMetricData("teeth_dropped").addValue(amount);
                relic.getRelicData(source, stack).getLevelingData().addExperience("looting", "drop", amount);
                totalAmount += amount;
            }

            for (int i = 0; i < totalAmount; i++) {
                var tooth = new GoldenToothEntity(RelicsEntities.GOLDEN_TOOTH.get(), level);

                tooth.setStacks(1);
                tooth.setPos(entity.getEyePosition());
                tooth.setDeltaMovement(MathUtils.randomFloat(random) * 0.35F, 0.25F + random.nextFloat() * 0.25F, MathUtils.randomFloat(random) * 0.35F);

                level.addFreshEntity(tooth);
            }
        }
    }
}
