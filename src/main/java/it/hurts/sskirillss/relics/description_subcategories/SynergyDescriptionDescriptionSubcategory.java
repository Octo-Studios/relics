package it.hurts.sskirillss.relics.description_subcategories;

import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.AbilityDescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.synergy.widgets.SynergyDescriptionContainerWidget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SynergyDescriptionDescriptionSubcategory extends DescriptionSubcategory {
    public SynergyDescriptionDescriptionSubcategory() {
        super("synergy_description");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 1;
    }

    @Override
    public DescriptionContainerWidget getContainerWidget(DescriptionScreen source) {
        return new SynergyDescriptionContainerWidget(source);
    }

    @Override
    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        return source instanceof SynergyDescriptionScreen;
    }
}