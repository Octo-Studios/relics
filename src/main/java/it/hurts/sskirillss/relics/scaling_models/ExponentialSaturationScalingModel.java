package it.hurts.sskirillss.relics.scaling_models;

import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ExponentialSaturationScalingModel extends ScalingModel {
    @Override
    public double evaluate(LivingEntity entity, ItemStack stack, double baseValue, double modifier, double iterations) {
        return baseValue - modifier * Math.expm1(-iterations * Math.abs(modifier));
    }
}
