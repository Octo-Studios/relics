package it.hurts.sskirillss.relics.mixin.compat.sophisticatedbackpacks;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackpackItem.class)
public class BackpackItemMixin {
    @Inject(method = "inventoryTick", at = @At("TAIL"))
    public void tick(ItemStack itemStack, Level level, Entity entity, int itemSlot, boolean isSelected, CallbackInfo ci) {
        var handler = BackpackWrapper.fromStack(itemStack).getInventoryHandler();

        for (var slot = 0; slot < handler.getSlots(); slot++) {
            var stack = handler.getStackInSlot(slot);

            if (stack.getItem() instanceof IRelicItem)
                stack.getItem().inventoryTick(stack, level, entity, itemSlot, false);
        }
    }
}