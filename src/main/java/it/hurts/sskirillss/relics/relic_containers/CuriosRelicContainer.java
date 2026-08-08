package it.hurts.sskirillss.relics.relic_containers;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CuriosRelicContainer extends RelicContainer {
    @Override
    public Function<LivingEntity, List<ItemStack>> gatherRelics() {
        return entity -> {
            List<ItemStack> relics = new ArrayList<>();

            CuriosApi.getCuriosInventory(entity).ifPresent(itemHandler -> {
                for (Map.Entry<String, ICurioStacksHandler> entry : itemHandler.getCurios().entrySet()) {
                    ICurioStacksHandler stacksHandler = entry.getValue();

                    for (int slot = 0; slot < stacksHandler.getSlots(); slot++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(slot);

                        if (stack.getItem() instanceof IRelicItem)
                            relics.add(stack);
                    }
                }
            });

            return relics;
        };
    }

    @Override
    public Function<Player, List<RelicStackReference>> gatherRelicReferences() {
        return player -> {
            List<RelicStackReference> references = new ArrayList<>();

            CuriosApi.getCuriosInventory(player).ifPresent(itemHandler -> {
                for (Map.Entry<String, ICurioStacksHandler> entry : itemHandler.getCurios().entrySet()) {
                    ICurioStacksHandler stacksHandler = entry.getValue();

                    for (int slot = 0; slot < stacksHandler.getSlots(); slot++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(slot);

                        if (stack.getItem() instanceof IRelicItem)
                            references.add(new CuriosRelicStackReference(entry.getKey(), slot));
                    }
                }
            });

            return references;
        };
    }
}
