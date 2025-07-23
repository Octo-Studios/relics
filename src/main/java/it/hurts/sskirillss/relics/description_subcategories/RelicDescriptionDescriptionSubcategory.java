package it.hurts.sskirillss.relics.description_subcategories;

import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.AbilityDescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RelicDescriptionContainerWidget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RelicDescriptionDescriptionSubcategory extends DescriptionSubcategory {
    public RelicDescriptionDescriptionSubcategory() {
        super("relic_description");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 1;
    }

    @Override
    public DescriptionContainerWidget getContainerWidget(DescriptionScreen source) {
        return new RelicDescriptionContainerWidget(source);
    }

    @Override
    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        return source instanceof RelicDescriptionScreen;
    }
}