package it.hurts.sskirillss.relics.badges.relic;

import it.hurts.sskirillss.relics.badges.base.RelicBadge;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.world.item.ItemStack;

public class FlawlessRelicBadge extends RelicBadge {
    public FlawlessRelicBadge() {
        super("flawless_relic");
    }

    @Override
    public boolean isVisible(ItemStack stack) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return false;

        return relic.isRelicFlawless(stack);
    }
}