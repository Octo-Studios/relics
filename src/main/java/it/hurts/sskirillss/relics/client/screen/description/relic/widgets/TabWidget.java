package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.client.screen.base.ITabbedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionTab;

import java.util.Locale;

public class TabWidget extends BookmarkWidget {
    private DescriptionScreen target;
    private DescriptionTab tab;

    public TabWidget(int x, int y, DescriptionScreen screen, DescriptionTab tab, DescriptionScreen target) {
        super(x, y, screen);

        this.target = target;
        this.tab = tab;
    }

    @Override
    public void onPress() {
        if (this.isLocked())
            return;

        this.minecraft.setScreen(target);
    }

    @Override
    public boolean isLocked() {
        return this.minecraft.screen instanceof ITabbedDescriptionScreen screen && screen.getTab() == this.tab;
    }

    @Override
    public String getId() {
        return this.tab.name().toLowerCase(Locale.ROOT);
    }
}