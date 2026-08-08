package it.hurts.sskirillss.relics.api.relic_containers;

public record AbilityReference(String ability, RelicStackReference stackReference, Type type) {
    public AbilityReference(String ability, RelicStackReference stackReference) {
        this(ability, stackReference, Type.ABILITY);
    }

    public boolean isSynergy() {
        return this.type == Type.SYNERGY;
    }

    public enum Type {
        ABILITY,
        SYNERGY
    }
}
