package it.hurts.sskirillss.relics.api.relic_containers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface RelicStackReference {
    ItemStack getStack(Player player);
}
