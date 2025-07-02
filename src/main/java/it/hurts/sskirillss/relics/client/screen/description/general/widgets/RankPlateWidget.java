package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class RankPlateWidget extends AbstractPlateWidget {
    public RankPlateWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, screen, "rank");
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    public void onTick() {

    }

    @Override
    public String getValue(ItemStack stack) {
        return String.valueOf(stack.getItem() instanceof IRelicItem relic ? relic.getRelicRank(minecraft.player, stack) : 0);
    }
}