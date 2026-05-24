package it.hurts.sskirillss.relics.api.relics.abilities.targeting;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.EnumSet;
import java.util.Set;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityTargetingTemplate {
    public static final AbilityTargetingTemplate EMPTY = new AbilityTargetingTemplate(Set.of(), Set.of());

    private final Set<SelectorType> selectors;
    private final Set<AbilityTargetingOption> options;

    public boolean isActive() {
        return !this.selectors.isEmpty() && !this.options.isEmpty();
    }

    public SelectorType getSelector() {
        if (this.selectors.size() != 1)
            return SelectorType.NONE;

        return this.selectors.iterator().next();
    }

    public boolean supportsSelector(SelectorType selectorType) {
        return this.selectors.contains(selectorType);
    }

    public boolean hasOption(AbilityTargetingOption option) {
        return this.options.contains(option);
    }

    public boolean hasOption(SelectorType selectorType, AbilityTargetingOption option) {
        return this.supportsSelector(selectorType) && this.optionsFor(selectorType).contains(option);
    }

    public Set<AbilityTargetingOption> optionsFor(SelectorType selectorType) {
        if (!this.supportsSelector(selectorType))
            return Set.of();

        return switch (selectorType) {
            case HARMFUL -> AbilityTargetingOption.harmfulOptions();
            case BENEFICIAL -> AbilityTargetingOption.beneficialOptions();
            case NONE -> Set.of();
        };
    }

    public static Builder builder(SelectorType selectorType) {
        return new Builder(selectorType);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Set<SelectorType> selectors = EnumSet.noneOf(SelectorType.class);
        private final Set<AbilityTargetingOption> options = EnumSet.noneOf(AbilityTargetingOption.class);

        private Builder() {
        }

        private Builder(SelectorType selectorType) {
            this.selector(selectorType);
        }

        public Builder selector(SelectorType selectorType) {
            if (selectorType != SelectorType.NONE)
                this.selectors.add(selectorType);

            switch (selectorType) {
                case HARMFUL -> this.options.addAll(AbilityTargetingOption.harmfulOptions());
                case BENEFICIAL -> this.options.addAll(AbilityTargetingOption.beneficialOptions());
                case NONE -> {
                }
            }

            return this;
        }

        public Builder option(AbilityTargetingOption option) {
            this.options.add(option);

            return this;
        }

        public Builder options(Set<AbilityTargetingOption> options) {
            this.options.addAll(options);

            return this;
        }

        public AbilityTargetingTemplate build() {
            return new AbilityTargetingTemplate(Set.copyOf(this.selectors), Set.copyOf(this.options));
        }
    }
}
