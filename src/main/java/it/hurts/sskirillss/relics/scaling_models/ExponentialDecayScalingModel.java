package it.hurts.sskirillss.relics.scaling_models;

import it.hurts.sskirillss.relics.api.relics.ScalingModel;

public class ExponentialDecayScalingModel extends ScalingModel {
    @Override
    public double evaluate(double baseValue, double modifier, int iterations) {
        return baseValue + (1 - Math.exp(-iterations)) * modifier;
    }
}