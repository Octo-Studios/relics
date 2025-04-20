package it.hurts.sskirillss.relics.scaling_models;

import it.hurts.sskirillss.relics.api.relics.ScalingModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AdditiveScalingModel extends ScalingModel {
    @Override
    public double evaluate(LivingEntity entity, ItemStack stack, double baseValue, double modifier, int iterations) {
        return baseValue + (modifier * iterations);
    }
}