package it.hurts.sskirillss.relics.relic_containers;

import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record InventoryRelicStackReference(int slot) implements RelicStackReference {
    @Override
    public ItemStack getStack(Player player) {
        return this.slot >= 0 && this.slot < player.getInventory().getContainerSize()
                ? player.getInventory().getItem(this.slot)
                : ItemStack.EMPTY;
    }
}
