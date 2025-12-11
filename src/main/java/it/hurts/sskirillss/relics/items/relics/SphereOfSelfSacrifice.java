package it.hurts.sskirillss.relics.items.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class SphereOfSelfSacrifice extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("sacrifice")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("heal")
                                        .initialValue(5D, 7D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("stacks")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.END_LIKE, LootEntries.THE_END)
                        .build())
                .build();
    }

    public List<HealingStack> getHealingStacks(ItemStack stack) {
        return new ArrayList<>(stack.getOrDefault(RelicsDataComponents.SPHERE_OF_SELF_SACRIFICE_STACKS, List.of()));
    }

    public void setHealingStacks(ItemStack stack, List<HealingStack> stacks) {
        stack.set(RelicsDataComponents.SPHERE_OF_SELF_SACRIFICE_STACKS, new ArrayList<>(stacks));
    }

    public void addHealingStack(LivingEntity entity, ItemStack stack, HealingStack healingStack) {
        var stacks = this.getHealingStacks(stack);
        var maxStacks = Math.max(1, (int) Math.ceil(this.getStatValue(entity, stack, "sacrifice", "stacks")));

        while (stacks.size() >= maxStacks)
            stacks.remove(0);

        stacks.add(healingStack);

        this.setHealingStacks(stack, stacks);
    }

    public HealingStack buildHealingStack(LivingEntity entity, ItemStack stack) {
        var ticks = Math.max(1, (int) Math.ceil(this.getStatValue(entity, stack, "sacrifice", "duration") * 20D));
        var healPerTick = (float) (this.getStatValue(entity, stack, "sacrifice", "heal") / ticks);

        return new HealingStack(healPerTick, ticks);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (!this.canPlayerUseAbility(player, stack, "sacrifice"))
            return InteractionResultHolder.pass(stack);

        if (!level.isClientSide()) {
            player.hurt(level.damageSources().magic(), (float) this.getStatValue(player, stack, "sacrifice", "damage"));

            this.addHealingStack(player, stack, this.buildHealingStack(player, stack));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide() || !(entity instanceof LivingEntity livingEntity))
            return;

        var stacks = this.getHealingStacks(stack);

        if (stacks.isEmpty())
            return;

        var updated = new ArrayList<HealingStack>();

        for (var healingStack : stacks) {
            livingEntity.heal(healingStack.healPerTick());

            var remainingTicks = healingStack.remainingTicks() - 1;

            if (remainingTicks > 0)
                updated.add(healingStack.withTicks(remainingTicks));
        }

        this.setHealingStacks(stack, updated);
    }

    public record HealingStack(float healPerTick, int remainingTicks) {
        public static final Codec<HealingStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("heal_per_tick").forGetter(HealingStack::healPerTick),
                Codec.INT.fieldOf("remaining_ticks").forGetter(HealingStack::remainingTicks)
        ).apply(instance, HealingStack::new));

        public HealingStack withTicks(int remainingTicks) {
            return new HealingStack(this.healPerTick, remainingTicks);
        }
    }
}
