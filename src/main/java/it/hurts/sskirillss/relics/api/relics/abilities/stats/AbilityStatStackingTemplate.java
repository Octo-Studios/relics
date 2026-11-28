package it.hurts.sskirillss.relics.api.relics.abilities.stats;

import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.DoubleBinaryOperator;
import java.util.function.Predicate;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityStatStackingTemplate {
    public static final AbilityStatStackingTemplate EMPTY = builder(StatStackingType.UNDEFINED).build();

    private final StatStackingType type;
    private final double neutralValue;
    private final DoubleBinaryOperator reducer;
    private final Predicate<AbilityData> condition;

    public boolean isAvailable(AbilityData ability) {
        return ability.canPlayerUse(ability.getAbilitiesData().getRelicData().getEntity()) && condition.test(ability);
    }

    public static Builder builder(StatStackingType type) {
        return new Builder(type);
    }

    public Builder toBuilder() {
        return builder(type).neutralValue(neutralValue).reducer(reducer).condition(condition);
    }

    public static class Builder {
        private final StatStackingType type;
        private double neutralValue;
        private DoubleBinaryOperator reducer;
        private Predicate<AbilityData> condition = ability -> true;

        private Builder(StatStackingType type) {
            this.type = type;
        }

        public Builder neutralValue(double neutralValue) {
            this.neutralValue = neutralValue;

            return this;
        }

        public Builder reducer(DoubleBinaryOperator reducer) {
            this.reducer = reducer;

            return this;
        }

        public Builder condition(Predicate<AbilityData> condition) {
            this.condition = condition;

            return this;
        }

        public AbilityStatStackingTemplate build() {
            return new AbilityStatStackingTemplate(type, neutralValue, reducer, condition);
        }
    }
}
