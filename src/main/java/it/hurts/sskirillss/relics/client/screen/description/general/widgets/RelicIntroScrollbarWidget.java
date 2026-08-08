package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.IScrollableWidget;
import net.minecraft.resources.ResourceLocation;

public class RelicIntroScrollbarWidget extends ScrollbarWidget {
    public RelicIntroScrollbarWidget(int x, int y, IScrollableWidget container) {
        super(x, y, container, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/bottom_scroll_bar.png"),
                68, -12, -20, true);
    }
}
