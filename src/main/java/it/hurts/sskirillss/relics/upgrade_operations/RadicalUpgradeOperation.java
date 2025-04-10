package it.hurts.sskirillss.relics.upgrade_operations;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.UpgradeOperation;

public class RadicalUpgradeOperation extends UpgradeOperation {
    @Override
    public double apply(double value, double modifier, int points) {
        return value + Math.sqrt(points) * modifier;
    }
}