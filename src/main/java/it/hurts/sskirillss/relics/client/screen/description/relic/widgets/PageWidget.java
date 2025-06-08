package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;

import java.util.Locale;

public class PageWidget extends BookmarkWidget {
    private DescriptionPage page;

    public PageWidget(int x, int y, DescriptionScreen screen, DescriptionPage page) {
        super(x, y, screen);

        this.page = page;
    }

    @Override
    public void onPress() {
        if (this.isLocked() || !(this.getScreen() instanceof IPagedDescriptionScreen screen))
            return;

        screen.setPage(this.page);

        this.getScreen().rebuildWidgets();
    }

    @Override
    public boolean isLocked() {
        return this.getScreen() instanceof IPagedDescriptionScreen screen && screen.getPage() == this.page;
    }

    @Override
    public String getId() {
        return this.page.name().toLowerCase(Locale.ROOT);
    }
}