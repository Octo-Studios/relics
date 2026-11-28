package it.hurts.sskirillss.relics.relic_containers;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import it.hurts.sskirillss.relics.init.RelicsRelicContainers;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record InventoryRelicStackReference(int slot) implements RelicStackReference {
    public static final StreamCodec<ByteBuf, InventoryRelicStackReference> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(InventoryRelicStackReference::new, InventoryRelicStackReference::slot);

    @Override
    public InventoryRelicContainer getContainer() {
        return RelicsRelicContainers.INVENTORY.get();
    }

    @Override
    public ItemStack getStack(Player player) {
        var inventory = player.getInventory();

        return slot >= 0 && slot < inventory.getContainerSize() ? inventory.getItem(slot) : ItemStack.EMPTY;
    }
}
