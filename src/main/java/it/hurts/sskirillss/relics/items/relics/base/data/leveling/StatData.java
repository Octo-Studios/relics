package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.config.data.StatConfigData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.BiFunction;
import java.util.function.Function;

import static it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation.CUSTOM;

@Data
@Builder
public class StatData {
    private final String id;

    public static StatDataBuilder builder(String id) {
        StatDataBuilder builder = new StatDataBuilder();

        builder.id(id);

        return builder;
    }

    @Builder.Default
    private Pair<UpgradeOperation, Double> upgradeModifier;
    @Builder.Default
    private BiFunction<Double, Integer, Double> newUpgradeModifier;
    @Builder.Default
    private Pair<Double, Double> initialValue;
    @Builder.Default
    private Pair<Double, Double> thresholdValue;
    @Builder.Default
    private Function<Double, ? extends Number> formatValue = Double::doubleValue;

    public StatConfigData toConfigData() {
        return new StatConfigData(initialValue.getKey(), initialValue.getValue(), thresholdValue.getKey(), thresholdValue.getValue(), upgradeModifier.getKey(), upgradeModifier.getValue());
    }

    public void setUpgradeModifier(Pair<UpgradeOperation, Double> upgradeModifier) {
        this.upgradeModifier = upgradeModifier;
        switch (upgradeModifier.getKey()) {
            case ADD ->            newUpgradeModifier = (current, points) -> current + points * upgradeModifier.getValue();
            case MULTIPLY_BASE ->  newUpgradeModifier = (current, points) -> current + ((current * upgradeModifier.getValue()) * points);
            case MULTIPLY_TOTAL -> newUpgradeModifier = (current, points) -> current * Math.pow(upgradeModifier.getValue() + 1, points);
        }
    }

    public static class StatDataBuilder {
        private Pair<UpgradeOperation, Double> upgradeModifier = Pair.of(CUSTOM, 0D);
        private BiFunction<Double, Integer, Double> newUpgradeModifier = null;
        private Pair<Double, Double> initialValue = Pair.of(0D, 0D);
        private Pair<Double, Double> thresholdValue = Pair.of(Double.MIN_VALUE, Double.MAX_VALUE);

        private StatDataBuilder id(String id) {
            this.id = id;

            return this;
        }

        public StatDataBuilder initialValue(double min, double max) {
            initialValue = Pair.of(min, max);

            return this;
        }

        public StatDataBuilder thresholdValue(double min, double max) {
            thresholdValue = Pair.of(min, max);

            return this;
        }

        private StatDataBuilder newUpgradeModifier(BiFunction<Double, Integer, Double> function) {
            return this;
        }

        /**
         * @deprecated Use {@code upgradeModifier(BiFunction<Double, Integer, Double> function)} instead
         */
        @Deprecated
        public StatDataBuilder upgradeModifier(UpgradeOperation operation, double step) {
            upgradeModifier = Pair.of(operation, step);
            switch (operation) {
                case ADD ->            newUpgradeModifier = (current, points) -> current + points * step;
                case MULTIPLY_BASE ->  newUpgradeModifier = (current, points) -> current + ((current * step) * points);
                case MULTIPLY_TOTAL -> newUpgradeModifier = (current, points) -> current * Math.pow(step + 1, points);
            }

            return this;
        }

        public StatDataBuilder upgradeModifier(BiFunction<Double, Integer, Double> function) {
            newUpgradeModifier = function;
            return this;
        }
    }
}