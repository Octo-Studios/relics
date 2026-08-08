package it.hurts.sskirillss.relics.api.relics.abilities.targeting;

import java.util.EnumSet;
import java.util.Set;

public enum AbilityTargetingOption {
    SELF(false, true),
    HOSTILE_MOBS(true, false),
    NEUTRAL_MOBS(true, false),
    PEACEFUL_MOBS(true, false),
    TAMED_CREATURES(false, false),
    OWN_TAMED_CREATURES(false, true),
    ALLIED_TAMED_CREATURES(false, true),
    TEAM_PLAYERS(false, true),
    OTHER_TEAM_PLAYERS(true, false);

    private final boolean harmfulDefault;
    private final boolean beneficialDefault;

    AbilityTargetingOption(boolean harmfulDefault, boolean beneficialDefault) {
        this.harmfulDefault = harmfulDefault;
        this.beneficialDefault = beneficialDefault;
    }

    public boolean getDefaultValue(SelectorType selectorType) {
        return switch (selectorType) {
            case HARMFUL -> this.harmfulDefault;
            case BENEFICIAL -> this.beneficialDefault;
            case NONE -> true;
        };
    }

    public static Set<AbilityTargetingOption> harmfulOptions() {
        return EnumSet.of(HOSTILE_MOBS, NEUTRAL_MOBS, PEACEFUL_MOBS, TAMED_CREATURES, TEAM_PLAYERS, OTHER_TEAM_PLAYERS);
    }

    public static Set<AbilityTargetingOption> beneficialOptions() {
        return EnumSet.of(SELF, OWN_TAMED_CREATURES, ALLIED_TAMED_CREATURES, TEAM_PLAYERS, OTHER_TEAM_PLAYERS, PEACEFUL_MOBS, NEUTRAL_MOBS);
    }
}
