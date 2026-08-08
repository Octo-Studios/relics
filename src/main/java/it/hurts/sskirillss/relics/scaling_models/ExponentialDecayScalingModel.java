package it.hurts.sskirillss.relics.scaling_models;

import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ExponentialDecayScalingModel extends ScalingModel {
    @Override
    public double evaluate(LivingEntity entity, ItemStack stack, double baseValue, double modifier, int iterations) {
        return baseValue + (1 - Math.exp(-iterations)) * modifier;
    }

    @Override
    public double calculateModifier(LivingEntity entity, ItemStack stack, double baseValue, double targetValue, int iterations) {
        return iterations <= 0 ? 0D : (targetValue - baseValue) / (1D - Math.exp(-iterations));
    }
}
