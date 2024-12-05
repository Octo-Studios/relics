package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.config.data.StatConfigData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.misc.StatIcon;
import it.hurts.sskirillss.relics.items.relics.base.data.misc.StatIcons;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

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
    private Pair<Double, Double> initialValue;
    @Builder.Default
    private Pair<Double, Double> thresholdValue;

    @Builder.Default
    private Function<Double, ? extends Number> formatValue = Double::doubleValue;

    @Builder.Default
    @Deprecated(forRemoval = true)
    private StatIcon icon = StatIcons.UNKNOWN;

    public StatConfigData toConfigData() {
        return new StatConfigData(initialValue.getKey(), initialValue.getValue(), thresholdValue.getKey(), thresholdValue.getValue(), upgradeModifier.getKey(), upgradeModifier.getValue());
    }

    public static class StatDataBuilder {
        private Pair<UpgradeOperation, Double> upgradeModifier = Pair.of(UpgradeOperation.ADD, 0D);
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

        public StatDataBuilder upgradeModifier(UpgradeOperation operation, double step) {
            upgradeModifier = Pair.of(operation, step);

            return this;
        }
    }
}