package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class RankPlateWidget extends AbstractPlateWidget {
    public RankPlateWidget(int x, int y, IRelicScreenProvider provider) {
        super(x, y, provider, "rank");
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    public void onTick() {

    }

    @Override
    public String getValue(ItemStack stack) {
        return String.valueOf(stack.getItem() instanceof IRelicItem relic ? relic.getRelicRank(stack) : 0);
    }
}