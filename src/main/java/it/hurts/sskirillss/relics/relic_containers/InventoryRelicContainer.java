package it.hurts.sskirillss.relics.relic_containers;

import it.hurts.sskirillss.relics.api.relic_containers.SlottedRelicContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

public class InventoryRelicContainer extends SlottedRelicContainer<InventoryRelicStackReference> {
    public InventoryRelicContainer() {
        super(InventoryRelicStackReference.STREAM_CODEC);
    }

    @Override
    protected void visitSlots(LivingEntity entity, BiConsumer<InventoryRelicStackReference, ItemStack> consumer) {
        if (entity instanceof Player player) {
            var inventory = player.getInventory();

            for (var slot = 0; slot < inventory.getContainerSize(); slot++)
                consumer.accept(new InventoryRelicStackReference(slot), inventory.getItem(slot));
        }
    }
}
