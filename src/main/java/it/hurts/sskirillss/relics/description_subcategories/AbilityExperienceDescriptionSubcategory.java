package it.hurts.sskirillss.relics.description_subcategories;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategory;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.AbilityExperienceContainerWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AbilityExperienceDescriptionSubcategory extends DescriptionSubcategory {
    public AbilityExperienceDescriptionSubcategory() {
        super("ability_experience");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 2;
    }

    @Override
    public DescriptionContainerWidget getContainerWidget(DescriptionScreen source) {
        return new AbilityExperienceContainerWidget(source);
    }

    @Override
    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        if (!(source instanceof AbilityDescriptionScreen screen) || screen.getSelectedAbility() == null)
            return false;

        var relic = (IRelicItem) stack.getItem();
        var ability = screen.getSelectedAbility();

        return relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability).canPlayerUse(entity) && !relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability).getTemplate().getExperienceSources().getSources().isEmpty();
    }
}