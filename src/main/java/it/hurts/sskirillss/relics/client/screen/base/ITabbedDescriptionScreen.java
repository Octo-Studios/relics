package it.hurts.sskirillss.relics.client.screen.base;

import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionTab;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionCache;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;

public interface ITabbedDescriptionScreen {
    DescriptionTab getTab();

    default void updateCache(IRelicItem relic) {
        DescriptionCache.setEntry(relic, DescriptionCache.getEntry(relic).toBuilder()
                .selectedPage(getTab())
                .build());
    }
}