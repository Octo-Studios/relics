package it.hurts.sskirillss.relics.client.screen.base;

import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;

public interface IPagedDescriptionScreen {
    DescriptionSubcategory getSubcategory();

    void setSubcategory(DescriptionSubcategory subcategory);
}