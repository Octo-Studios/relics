package it.hurts.sskirillss.relics.description_categories;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategory;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SynergyDescriptionCategory extends DescriptionCategory {
    public SynergyDescriptionCategory() {
        super("synergy");
    }

    @Override
    public int getOrder(LivingEntity entity, ItemStack stack) {
        return 3;
    }

    // FIXME
    @Override
    public DescriptionScreen getScreen(DescriptionScreen source) {
        return new RelicDescriptionScreen(Minecraft.getInstance().player, source.container, source.slot, source.screen);
    }

    @Override
    public boolean shouldAppear(LivingEntity entity, ItemStack stack) {
        var relic = (IRelicItem) stack.getItem();

        return !relic.getRelicData(entity, stack).getTemplate().getAbilities().getSynergies().isEmpty();
    }
}