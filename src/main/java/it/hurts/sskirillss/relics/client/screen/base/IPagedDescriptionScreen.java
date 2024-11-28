package it.hurts.sskirillss.relics.client.screen.base;

import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionCache;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;

public interface IPagedDescriptionScreen {
    DescriptionPage getPage();

    default void updateCache(IRelicItem relic) {
        DescriptionCache.setEntry(relic, DescriptionCache.getEntry(relic).toBuilder()
                .selectedPage(getPage())
                .build());
    }
}