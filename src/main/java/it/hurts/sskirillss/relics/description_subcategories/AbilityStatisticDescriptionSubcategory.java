package it.hurts.sskirillss.relics.description_subcategories;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.AbilityStatisticContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AbilityStatisticDescriptionSubcategory extends DescriptionSubcategory {
    public AbilityStatisticDescriptionSubcategory() {
        super("ability_statistic");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 3;
    }

    @Override
    public DescriptionContainerWidget getContainerWidget(DescriptionScreen source) {
        return new AbilityStatisticContainerWidget(source);
    }

    @Override
    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        if (!(source instanceof AbilityDescriptionScreen screen) || screen.getSelectedAbility() == null)
            return false;

        var relic = (IRelicItem) stack.getItem();
        var ability = screen.getSelectedAbility();

        return relic.canPlayerUseAbility(entity, stack, ability) && !relic.getAbilityStatisticTemplate(entity, stack, ability).getMetrics().isEmpty();
    }
}