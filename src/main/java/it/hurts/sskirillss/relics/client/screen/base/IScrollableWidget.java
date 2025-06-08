package it.hurts.sskirillss.relics.client.screen.base;

import it.hurts.sskirillss.relics.client.screen.description.general.widgets.ScrollbarWidget;

public interface IScrollableWidget {
    int getContentHeight();

    int getContainerHeight();

    ScrollbarWidget getScrollbar();
}