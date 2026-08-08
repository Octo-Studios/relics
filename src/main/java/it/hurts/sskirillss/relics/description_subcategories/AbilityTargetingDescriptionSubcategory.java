package it.hurts.sskirillss.relics.description_subcategories;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.AbilityTargetingContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AbilityTargetingDescriptionSubcategory extends DescriptionSubcategory {
    public AbilityTargetingDescriptionSubcategory() {
        super("ability_targeting");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 4;
    }

    @Override
    public DescriptionContainerWidget getContainerWidget(DescriptionScreen source) {
        return new AbilityTargetingContainerWidget(source);
    }

    @Override
    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        if (!(source instanceof AbilityDescriptionScreen screen) || screen.getSelectedAbility() == null || !(stack.getItem() instanceof IRelicItem relic))
            return false;

        var abilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(screen.getSelectedAbility());
        var template = abilityData.getTemplate();

        return template != null && abilityData.canPlayerUse(entity) && template.getTargeting().isActive();
    }
}
