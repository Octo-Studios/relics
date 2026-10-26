package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.utils.misc.BookmarkTransition;

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

        BookmarkTransition.mark(this.getKey(), BookmarkTransition.pageKey(screen.getSubcategory().getId()));

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

    @Override
    public String getKey() {
        return BookmarkTransition.pageKey(this.getId());
    }
}