package it.hurts.sskirillss.relics.upgrade_operations;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.UpgradeOperation;

public class LogarithmicUpgradeOperation extends UpgradeOperation {
    @Override
    public double apply(double value, double modifier, int points) {
        return value + Math.log(points + 1) * modifier;
    }
}