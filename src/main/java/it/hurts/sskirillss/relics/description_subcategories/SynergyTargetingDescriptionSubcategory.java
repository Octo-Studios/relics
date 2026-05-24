package it.hurts.sskirillss.relics.description_subcategories;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.synergy.widgets.SynergyTargetingContainerWidget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SynergyTargetingDescriptionSubcategory extends DescriptionSubcategory {
    public SynergyTargetingDescriptionSubcategory() {
        super("synergy_targeting");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 4;
    }

    @Override
    public DescriptionContainerWidget getContainerWidget(DescriptionScreen source) {
        return new SynergyTargetingContainerWidget(source);
    }

    @Override
    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        if (!(source instanceof SynergyDescriptionScreen screen) || screen.getSelectedSynergy() == null || !(stack.getItem() instanceof IRelicItem relic))
            return false;

        var synergyData = relic.getRelicData(entity, stack).getAbilitiesData().getSynergyData(screen.getSelectedSynergy());
        var template = synergyData.getTemplate();

        return template != null && synergyData.isUnlocked() && template.getTargeting().isActive();
    }
}
