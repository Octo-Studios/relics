package it.hurts.sskirillss.relics.api.relics.abilities.stats;

public abstract class UpgradeOperation {
    /**
     * Applies an upgrade operation to a base value using a specified modifier and number of upgrade points.
     *
     * @param value    The base (or relative) stat value before any upgrades are applied.
     * @param modifier The step or magnitude of the modifier applied per upgrade point.
     * @param points   The number of times the modifier should be applied (i.e. the upgrade points).
     * @return The resulting value after applying the upgrade operation.
     */
    public abstract double apply(double value, double modifier, int points);
}