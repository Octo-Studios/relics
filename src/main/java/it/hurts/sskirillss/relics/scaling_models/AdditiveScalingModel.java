package it.hurts.sskirillss.relics.scaling_models;

import it.hurts.sskirillss.relics.api.relics.ScalingModel;

public class AdditiveScalingModel extends ScalingModel {
    @Override
    public double evaluate(double baseValue, double modifier, int iterations) {
        return baseValue + (modifier * iterations);
    }
}