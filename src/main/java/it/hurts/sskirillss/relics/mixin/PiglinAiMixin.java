package it.hurts.sskirillss.relics.mixin;

import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.head.PiglinMaskItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
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

    @Inject(method = "stopHoldingOffHandItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;throwItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;Ljava/util/List;)V"), cancellable = true)
    private static void tweakBartering(Piglin piglin, boolean bool, CallbackInfo ci) {
        var optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

        if (optional.isEmpty())
            return;

        var player = optional.get();
        var random = player.getRandom();

        var shouldCancel = false;

        for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.PIGLIN_MASK.get())) {
            var relic = (PiglinMaskItem) stack.getItem();

            if (!relic.canPlayerUseAbility(player, stack, "barter"))
                continue;

            shouldCancel = true;

            for (int i = 0; i < random.nextInt((int) relic.getStatValue(player, stack, "barter", "amount") + 1) + 1; i++) {
                PiglinAi.throwItems(piglin, PiglinAiMixin.getBarterResponseItems(piglin));
            }
        }

        if (shouldCancel)
            ci.cancel();
    }
}