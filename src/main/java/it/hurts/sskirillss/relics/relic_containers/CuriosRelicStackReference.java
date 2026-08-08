package it.hurts.sskirillss.relics.relic_containers;

import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public record CuriosRelicStackReference(String identifier, int index) implements RelicStackReference {
    @Override
    public ItemStack getStack(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.findCurio(this.identifier, this.index))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}
