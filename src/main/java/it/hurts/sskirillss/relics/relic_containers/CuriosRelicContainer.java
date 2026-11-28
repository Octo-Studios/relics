package it.hurts.sskirillss.relics.relic_containers;

import it.hurts.sskirillss.relics.api.relic_containers.SlottedRelicContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.BiConsumer;

public class CuriosRelicContainer extends SlottedRelicContainer<CuriosRelicStackReference> {
    public CuriosRelicContainer() {
        super(CuriosRelicStackReference.STREAM_CODEC);
    }

    @Override
    protected void visitSlots(LivingEntity entity, BiConsumer<CuriosRelicStackReference, ItemStack> consumer) {
        CuriosApi.getCuriosInventory(entity).ifPresent(inventory -> inventory.getCurios().forEach((identifier, handler) -> {
            var stacks = handler.getStacks();

            for (var slot = 0; slot < stacks.getSlots(); slot++)
                consumer.accept(new CuriosRelicStackReference(identifier, slot), stacks.getStackInSlot(slot));
        }));
    }
}
