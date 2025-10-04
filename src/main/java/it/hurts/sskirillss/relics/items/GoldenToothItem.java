package it.hurts.sskirillss.relics.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class GoldenToothItem extends ItemBase {
    public GoldenToothItem() {
        super(new Properties().rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isPiglinCurrency(ItemStack stack) {
        return true;
    }
}