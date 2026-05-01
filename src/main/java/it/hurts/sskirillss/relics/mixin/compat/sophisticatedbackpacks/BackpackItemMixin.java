package it.hurts.sskirillss.relics.mixin.compat.sophisticatedbackpacks;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(BackpackItem.class)
public class BackpackItemMixin {
    @Inject(method = "inventoryTick", at = @At("TAIL"))
    public void tick(ItemStack itemStack, Level level, Entity entity, int itemSlot, boolean isSelected, CallbackInfo ci) {
        relics$tickBackpackContents(itemStack, level, entity, itemSlot, new HashSet<>(), 0);
    }

    @Unique
    private static void relics$tickBackpackContents(ItemStack backpackStack, Level level, Entity entity, int itemSlot, Set<UUID> visitedContents, int depth) {
        if (backpackStack.isEmpty() || depth > 5)
            return;

        var wrapper = BackpackWrapper.fromStack(backpackStack);
        var contentsUuid = wrapper.getContentsUuid();

        if (contentsUuid.isPresent() && !visitedContents.add(contentsUuid.get()))
            return;

        relics$tickInventory(wrapper.getInventoryHandler(), level, entity, itemSlot, visitedContents, depth);
    }

    @Unique
    private static void relics$tickInventory(InventoryHandler handler, Level level, Entity entity, int itemSlot, Set<UUID> visitedContents, int depth) {
        for (var slot = 0; slot < handler.getSlots(); slot++) {
            var stack = handler.getStackInSlot(slot);

            if (stack.isEmpty())
                continue;

            var item = stack.getItem();

            if (item instanceof IRelicItem) {
                item.inventoryTick(stack, level, entity, itemSlot, false);
                handler.setStackInSlot(slot, stack);
            }

            if (item instanceof BackpackItem)
                relics$tickBackpackContents(stack, level, entity, itemSlot, visitedContents, depth + 1);
        }
    }
}