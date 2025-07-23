package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;

import java.util.Locale;

public class PageWidget extends BookmarkWidget {
    private DescriptionSubcategory subcategory;

    public PageWidget(int x, int y, DescriptionScreen screen, DescriptionSubcategory subcategory) {
        super(x, y, screen);

        this.subcategory = subcategory;
    }

    @Override
    public void onPress() {
        if (this.isLocked() || !(this.getScreen() instanceof IPagedDescriptionScreen screen))
            return;

        screen.setSubcategory(this.subcategory);

        this.getScreen().rebuildWidgets();
    }

    @Override
    public boolean isLocked() {
        return this.getScreen() instanceof IPagedDescriptionScreen screen && screen.getSubcategory() == this.subcategory;
    }

    @Override
    public String getId() {
        return this.subcategory.getId();
    }
}