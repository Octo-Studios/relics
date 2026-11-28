package it.hurts.sskirillss.relics.relic_containers;

import it.hurts.sskirillss.relics.api.relic_containers.SlottedRelicContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

public class HandRelicContainer extends SlottedRelicContainer<HandRelicStackReference> {
    public HandRelicContainer() {
        super(HandRelicStackReference.STREAM_CODEC);
    }

    @Override
    protected void visitSlots(LivingEntity entity, BiConsumer<HandRelicStackReference, ItemStack> consumer) {
        consumer.accept(new HandRelicStackReference(entity instanceof Player player ? player.getInventory().selected : 0), entity.getMainHandItem());
        consumer.accept(new HandRelicStackReference(Inventory.SLOT_OFFHAND), entity.getOffhandItem());
    }
}
