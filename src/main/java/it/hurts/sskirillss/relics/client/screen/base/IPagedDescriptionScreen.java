package it.hurts.sskirillss.relics.client.screen.base;

import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;

public interface IPagedDescriptionScreen {
    DescriptionPage getPage();

    void setPage(DescriptionPage page);
}