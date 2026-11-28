package it.hurts.sskirillss.relics.api.relics.abilities.stats;

public enum StatStackingType {
    UNDEFINED,
    ADDITIVE,
    MULTIPLICATIVE,
    SEQUENTIAL,
    BEST,
    MIXED,
    PARALLEL;

    public boolean ranksCopiesByStat() {
        return this == ADDITIVE || this == MULTIPLICATIVE || this == SEQUENTIAL || this == BEST || this == MIXED;
    }

    public static double combineReductions(double previous, double reduction) {
        return previous + (1D - previous) * reduction;
    }
}
