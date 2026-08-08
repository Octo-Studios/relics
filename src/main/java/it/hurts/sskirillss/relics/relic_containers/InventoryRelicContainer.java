package it.hurts.sskirillss.relics.relic_containers;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class InventoryRelicContainer extends RelicContainer {
    @Override
    public Function<LivingEntity, List<ItemStack>> gatherRelics() {
        return entity -> {
            List<ItemStack> relics = new ArrayList<>();

            if (!(entity instanceof Player player))
                return relics;

            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);

                if (stack.getItem() instanceof IRelicItem)
                    relics.add(stack);
            }

            return relics;
        };
    }

    @Override
    public Function<Player, List<RelicStackReference>> gatherRelicReferences() {
        return player -> {
            List<RelicStackReference> references = new ArrayList<>();

            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack stack = player.getInventory().getItem(slot);

                if (stack.getItem() instanceof IRelicItem)
                    references.add(new InventoryRelicStackReference(slot));
            }

            return references;
        };
    }
}
