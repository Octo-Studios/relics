package it.hurts.sskirillss.relics.description_categories;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategory;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AbilityDescriptionCategory extends DescriptionCategory {
    public AbilityDescriptionCategory() {
        super("ability");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 2;
    }

    @Override
    public DescriptionScreen getScreen(DescriptionScreen source) {
        return new AbilityDescriptionScreen(Minecraft.getInstance().player, source.container, source.slot, source);
    }

    @Override
    public boolean shouldAppear(LivingEntity entity, ItemStack stack) {
        var relic = (IRelicItem) stack.getItem();

        return !relic.getAbilitiesTemplate(entity, stack).getAbilities().isEmpty();
    }
}