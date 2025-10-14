package it.hurts.sskirillss.relics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.head.PiglinMaskItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
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

    @WrapOperation(method = "pickUpItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/Piglin;take(Lnet/minecraft/world/entity/Entity;I)V"))
    private static void wrapTake(Piglin piglin, Entity entity, int originalCount, Operation<Void> original, @Local(argsOnly = true) ItemEntity itemEntity) {
        var count = originalCount;
        var stackCount = itemEntity.getItem().getCount();
        var optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

        if (optional.isPresent()) {
            var player = optional.get();

            var bestStack = ItemStack.EMPTY;
            var bestValue = 0;

            for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.PIGLIN_MASK.get())) {
                var relic = (PiglinMaskItem) stack.getItem();

                if (!relic.isAbilityRankModifierUnlocked(player, stack, "barter", "pocket"))
                    continue;

                var value = (int) relic.getStatValue(player, stack, "barter", "items_count");

                if (value > bestValue) {
                    bestValue = value;
                    bestStack = stack;
                }
            }

            if (bestValue > 0)
                count = Math.min(stackCount, bestValue);

            if (count > 1 && bestStack.isEmpty()) {
                var relic = (PiglinMaskItem) bestStack.getItem();

                relic.addRelicExperience(player, bestStack, "barter", "pickup", count - 1);
            }
        }

        original.call(piglin, entity, count);
    }

    @WrapOperation(method = "pickUpItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;removeOneItemFromItemEntity(Lnet/minecraft/world/entity/item/ItemEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack wrapRemoveOne(ItemEntity itemEntity, Operation<ItemStack> operation, @Local(argsOnly = true) Piglin piglin) {
        var optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

        if (optional.isEmpty())
            return operation.call(itemEntity);

        var player = optional.get();

        var bestStack = ItemStack.EMPTY;
        var bestValue = 0;

        for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.PIGLIN_MASK.get())) {
            var relic = (PiglinMaskItem) stack.getItem();

            if (!relic.isAbilityRankModifierUnlocked(player, stack, "barter", "pocket"))
                continue;

            var value = (int) relic.getStatValue(player, stack, "barter", "items_count");

            if (value > bestValue) {
                bestValue = value;
                bestStack = stack;
            }
        }

        if (bestValue <= 0)
            return operation.call(itemEntity);

        var stack = itemEntity.getItem();
        var toSplit = Math.min(stack.getCount(), bestValue);

        if (toSplit < 1)
            return operation.call(itemEntity);

        var taken = stack.split(toSplit);

        if (stack.isEmpty())
            itemEntity.discard();
        else itemEntity.setItem(stack);

        if (taken.getCount() > 1 && bestStack.isEmpty()) {
            var relic = (PiglinMaskItem) bestStack.getItem();

            relic.addRelicExperience(player, bestStack, "barter", "pickup", taken.getCount() - 1);
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

            if (!relic.canPlayerUseAbility(player, stack, "barter"))
                continue;

            shouldCancel = true;

            var base = random.nextInt((int) relic.getStatValue(player, stack, "barter", "trades") + 1) + 1;
            var total = Math.max(1, base) * count;

            for (int i = 0; i < total; i++) {
                PiglinAi.throwItems(piglin, PiglinAiMixin.getBarterResponseItems(piglin));

                relic.addRelicExperience(player, stack, "barter", "trade", 1);
            }
        }

        if (shouldCancel)
            ci.cancel();
    }
}