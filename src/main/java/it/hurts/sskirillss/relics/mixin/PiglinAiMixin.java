package it.hurts.sskirillss.relics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.head.PiglinMaskItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
    @Shadow
    private static List<ItemStack> getBarterResponseItems(Piglin piglin) {
        return new ArrayList<>();
    }

    @WrapOperation(method = "pickUpItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;removeOneItemFromItemEntity(Lnet/minecraft/world/entity/item/ItemEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack wrapRemoveOne(ItemEntity itemEntity, Operation<ItemStack> operation, @Local(argsOnly = true) Piglin piglin) {
        var optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

        if (optional.isEmpty())
            return operation.call(itemEntity);

        var player = optional.get();

        var eligibleStacks = new ArrayList<ItemStack>();
        var totalValue = 0;

        for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.PIGLIN_MASK.get())) {
            var relic = (PiglinMaskItem) stack.getItem();
            var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("barter");

            if (!ability.canPlayerUse(player) || !ability.getRankModifierData("pocket").isEnabled())
                continue;

            var value = (int) ability.getStatData("items_count").getValue();

            if (value <= 0)
                continue;

            eligibleStacks.add(stack);
            totalValue += value;
        }

        if (totalValue <= 0)
            return operation.call(itemEntity);

        var stack = itemEntity.getItem();

        if (!stack.is(ItemTags.PIGLIN_LOVED))
            return operation.call(itemEntity);

        var toSplit = Math.min(stack.getCount(), totalValue);

        if (toSplit < 1)
            return operation.call(itemEntity);

        var taken = stack.split(toSplit);

        if (stack.isEmpty())
            itemEntity.discard();
        else itemEntity.setItem(stack);

        if (taken.getCount() > 1 && !eligibleStacks.isEmpty()) {
            var remaining = taken.getCount();

            for (var maskStack : eligibleStacks) {
                if (remaining <= 0)
                    break;

                var relic = (PiglinMaskItem) maskStack.getItem();
                var ability = relic.getRelicData(player, maskStack).getAbilitiesData().getAbilityData("barter");
                var contribution = Math.min((int) ability.getStatData("items_count").getValue(), remaining);

                if (contribution <= 0)
                    continue;

                if (contribution > 1)
                    relic.getRelicData(player, maskStack).getLevelingData().addExperience("barter", "pickup", contribution - 1);

                ability.getStatisticData().getMetricData("currency").addValue(contribution);
                remaining -= contribution;
            }
        }

        return taken;
    }

    @Inject(method = "stopHoldingOffHandItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;throwItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;Ljava/util/List;)V"), cancellable = true)
    private static void tweakBartering(Piglin piglin, boolean bool, CallbackInfo ci, @Local ItemStack itemstack) {
        var optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

        if (optional.isEmpty())
            return;

        var player = optional.get();
        var random = player.getRandom();

        var count = itemstack.is(ItemTags.PIGLIN_LOVED) ? itemstack.getCount() : 1;

        var shouldCancel = false;

        for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.PIGLIN_MASK.get())) {
            var relic = (PiglinMaskItem) stack.getItem();

            if (!relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("barter").canPlayerUse(player))
                continue;

            shouldCancel = true;

            var base = random.nextInt((int) relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("barter").getStatData("trades").getValue()) + 1;
            var total = Math.max(1, base) * count;

            for (int i = 0; i < total; i++) {
                var items = PiglinAiMixin.getBarterResponseItems(piglin);

                PiglinAi.throwItems(piglin, items);

                var amount = 0;

                for (var tradeEntry : items)
                    amount += tradeEntry.getCount();

                relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("barter").getStatisticData().getMetricData("items").addValue(amount);
                relic.getRelicData(player, stack).getLevelingData().addExperience("barter", "trade", 1);
            }
        }

        if (shouldCancel)
            ci.cancel();
    }
}
