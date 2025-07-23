package it.hurts.sskirillss.relics.description_subcategories;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.AbilityStatisticContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.AbilityDescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AbilityDescriptionDescriptionSubcategory extends DescriptionSubcategory {
    public AbilityDescriptionDescriptionSubcategory() {
        super("ability_description");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 1;
    }

    @Override
    public DescriptionContainerWidget getContainerWidget(DescriptionScreen source) {
        return new AbilityDescriptionContainerWidget(source);
    }

    @Override
    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        return source instanceof AbilityDescriptionScreen;
    }
}