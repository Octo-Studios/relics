package it.hurts.sskirillss.relics.items.relics.head;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.GoldenToothEntity;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import top.theillusivec4.curios.api.SlotContext;

public class PiglinMaskItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("neutrality")
                                .rankModifier(1, "legion")
                                .initialMaxLevel(0)
                                .build())
                        .ability(AbilityTemplate.builder("looting")
                                .modes("tooth", "buff")
                                .rankModifier(3, "frenzy")
                                .stat(StatTemplate.builder("chance")
                                        .initialValue(0.1D, 0.15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1212D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("health")
                                        .initialValue(10D, 7.5D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.019D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 5.5811D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("attack_damage")
                                        .initialValue(0.005D, 0.01D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0607D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatTemplate.builder("attack_speed")
                                        .initialValue(0.005D, 0.01D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1946D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .stat(StatTemplate.builder("movement_speed")
                                        .initialValue(0.005D, 0.01D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1946D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("barter")
                                .stat(StatTemplate.builder("amount")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.25D)
                                        .formatValue(Double::intValue)
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
        return (int) this.getStatValue(entity, stack, "looting", "duration");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();

        var duration = this.getDuration(stack);
        var stacks = this.getStacks(stack);

        if (entity instanceof Player player)
            player.displayClientMessage(Component.literal("S: " + stacks + " D: " + duration), true);

        if (entity.tickCount % 20 == 0) {
            if (duration > 0) {
                this.addDuration(entity, stack, -1);
            } else if (stacks > 0) {
                if (stacks >= PiglinMaskItem.getMaxStacks())
                    this.setStacks(stack, 0);
                else
                    this.addStacks(stack, -1);
            }

            var modifier = this.getStacks(stack) >= PiglinMaskItem.getMaxStacks() ? 5 : 1;

            EntityUtils.resetAttribute(entity, stack, Attributes.ATTACK_SPEED, (float) (stacks * this.getStatValue(entity, stack, "looting", "attack_speed") * modifier), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            EntityUtils.resetAttribute(entity, stack, Attributes.MOVEMENT_SPEED, (float) (stacks * this.getStatValue(entity, stack, "looting", "attack_speed") * modifier), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    @Override
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return this.canPlayerUseAbility(slotContext.entity(), stack, "neutrality");
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

                if (!relic.isAbilityRankModifierUnlocked(source, stack, "neutrality", "legion"))
                    continue;

                for (var piglin : level.getEntitiesOfClass(AbstractPiglin.class, source.getBoundingBox().inflate(32))) {
                    if (piglin.getTarget() != null || !piglin.hasLineOfSight(target))
                        continue;

                    piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, target.getUUID());
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

                if (!relic.isAbilityRankModifierUnlocked(target, stack, "neutrality", "legion"))
                    continue;

                for (var piglin : level.getEntitiesOfClass(AbstractPiglin.class, target.getBoundingBox().inflate(32))) {
                    if (piglin.getTarget() != null || !piglin.hasLineOfSight(source))
                        continue;

                    piglin.getBrain().setMemory(MemoryModuleType.ANGRY_AT, source.getUUID());
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamage3(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity source))
                return;

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.PIGLIN_MASK.get())) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.isAbilityRankModifierUnlocked(source, stack, "looting", "frenzy"))
                    continue;

                var damage = event.getNewDamage();

                var modifier = relic.getStacks(stack) >= PiglinMaskItem.getMaxStacks() ? 5 : 1;

                event.setNewDamage((float) (damage + (damage * relic.getStatValue(source, stack, "looting", "attack_speed") * modifier)));
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity source))
                return;

            var entity = event.getEntity();

            var level = source.level();
            var random = level.getRandom();

            for (var stack : EntityUtils.findEquippedCurios(source, RelicsItems.PIGLIN_MASK.get())) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.canPlayerUseAbility(source, stack, "looting"))
                    continue;

                var amount = MathUtils.multicast(random, relic.getStatValue(source, stack, "looting", "chance"), (int) Math.ceil(entity.getMaxHealth() / relic.getStatValue(source, stack, "looting", "health")));

                for (int i = 0; i < amount; i++) {
                    var tooth = new GoldenToothEntity(RelicsEntities.GOLDEN_TOOTH.get(), level);

                    tooth.setStacks(1);
                    tooth.setPos(entity.getEyePosition());
                    tooth.setDeltaMovement(MathUtils.randomFloat(random) * 0.35F, 0.25F + random.nextFloat() * 0.25F, MathUtils.randomFloat(random) * 0.35F);

                    level.addFreshEntity(tooth);
                }
            }
        }
    }
}