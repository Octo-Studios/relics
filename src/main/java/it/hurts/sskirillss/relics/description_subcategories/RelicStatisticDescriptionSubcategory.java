package it.hurts.sskirillss.relics.description_subcategories;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.RelicStatisticContainerWidget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RelicStatisticDescriptionSubcategory extends DescriptionSubcategory {
    public RelicStatisticDescriptionSubcategory() {
        super("relic_statistic");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 2;
    }

    @Override
    public DescriptionContainerWidget getContainerWidget(DescriptionScreen source) {
        return new RelicStatisticContainerWidget(source);
    }

    @Override
    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        if (!(source instanceof RelicDescriptionScreen screen))
            return false;

        var relic = (IRelicItem) stack.getItem();

        return !relic.getRelicData(entity, stack).getStatisticData().getTemplate().getMetrics().isEmpty();
    }
}
