package it.hurts.sskirillss.relics.api.relics.description;

import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Data
@AllArgsConstructor
public abstract class DescriptionSubcategory {
    private final String id;

    public abstract int getOrder(LivingEntity entity, ItemStack stack);

    public abstract DescriptionContainerWidget getContainerWidget(DescriptionScreen source);

    public boolean shouldAppear(DescriptionScreen source, LivingEntity entity, ItemStack stack) {
        return true;
    }
}