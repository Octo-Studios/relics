package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategory;
import it.hurts.sskirillss.relics.client.screen.base.ITabbedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import net.minecraft.client.gui.screens.Screen;

public class TabWidget extends BookmarkWidget {
    private DescriptionCategory category;

    public TabWidget(int x, int y, DescriptionScreen screen, DescriptionCategory category) {
        super(x, y, screen);

        this.category = category;
    }

    @Override
    public void onPress() {
        if (this.isLocked())
            return;

        this.minecraft.setScreen(this.category.getScreen(this.getScreen()));
    }

    @Override
    public boolean isLocked() {
        return this.minecraft.screen instanceof ITabbedDescriptionScreen screen && screen.getCategory() == this.category;
    }

    @Override
    public String getId() {
        return this.category.getId();
    }
}