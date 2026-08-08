package it.hurts.sskirillss.relics.api.relics.abilities.targeting;

public enum SelectorType {
    NONE,
    HARMFUL,
    BENEFICIAL;

    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
