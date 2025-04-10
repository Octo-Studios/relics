package it.hurts.sskirillss.relics.upgrade_operations;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.UpgradeOperation;

public class ExponentialSaturationUpgradeOperation extends UpgradeOperation {
    @Override
    public double apply(double value, double modifier, int points) {
        return value + modifier * (1 - Math.exp(-points * modifier));
    }
}