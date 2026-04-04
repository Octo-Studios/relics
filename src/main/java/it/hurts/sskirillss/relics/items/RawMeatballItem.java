package it.hurts.sskirillss.relics.items;

import it.hurts.sskirillss.relics.items.relics.head.ChefHatItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RawMeatballItem extends ItemBase {
    public RawMeatballItem() {
        super(new Properties().food(new FoodProperties.Builder()
                .nutrition(3)
                .saturationModifier(0.3F)
                .build()));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        var duration = super.getUseDuration(stack, entity);
        var multiplier = ChefHatItem.getMeatballConsumptionSpeedMultiplier(entity);

        if (multiplier <= 1D)
            return duration;

        return Math.max(1, (int) Math.ceil(duration / multiplier));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide())
            ChefHatItem.onMeatballConsumed(entity, false);

        return result;
    }
}
