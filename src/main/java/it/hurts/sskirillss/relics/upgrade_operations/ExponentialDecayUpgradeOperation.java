package it.hurts.sskirillss.relics.upgrade_operations;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.UpgradeOperation;

public class ExponentialDecayUpgradeOperation extends UpgradeOperation {
    @Override
    public double apply(double value, double modifier, int points) {
        return value + (1 - Math.exp(-points)) * modifier;
    }
}