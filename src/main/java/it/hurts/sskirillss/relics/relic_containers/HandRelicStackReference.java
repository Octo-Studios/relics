package it.hurts.sskirillss.relics.relic_containers;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import it.hurts.sskirillss.relics.init.RelicsRelicContainers;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record HandRelicStackReference(int slot) implements RelicStackReference {
    public static final StreamCodec<ByteBuf, HandRelicStackReference> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(HandRelicStackReference::new, HandRelicStackReference::slot);

    @Override
    public HandRelicContainer getContainer() {
        return RelicsRelicContainers.HANDS.get();
    }

    @Override
    public ItemStack getStack(Player player) {
        if (slot == Inventory.SLOT_OFFHAND)
            return player.getOffhandItem();

        return slot == player.getInventory().selected ? player.getMainHandItem() : ItemStack.EMPTY;
    }
}
