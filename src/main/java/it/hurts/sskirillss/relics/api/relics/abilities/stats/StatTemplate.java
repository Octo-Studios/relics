package it.hurts.sskirillss.relics.api.relics.abilities.stats;

import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import it.hurts.sskirillss.relics.config.data.StatConfigData;
import it.hurts.sskirillss.relics.init.RegistryRegistry;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatTemplate {
    private final String id;

    private final Pair<ScalingModel, Double> upgradeModifier;
    private final Pair<Double, Double> initialValue;
    private final Pair<Double, Double> thresholdValue;
    private final Function<Double, ? extends Number> formatValue;

    public static StatTemplateBuilder builder(String id) {
        return new StatTemplateBuilder(id);
    }

    public StatTemplateBuilder toBuilder() {
        return new StatTemplateBuilder(this);
    }

    public StatConfigData toConfigData() {
        return new StatConfigData(initialValue.getKey(), initialValue.getValue(), thresholdValue.getKey(), thresholdValue.getValue(), RegistryRegistry.SCALING_MODEL_REGISTRY.getKey(upgradeModifier.getKey()).toString(), upgradeModifier.getValue());
    }

    public static class StatTemplateBuilder {
        private final String id;

        private Pair<ScalingModel, Double> upgradeModifier = Pair.of(ScalingModelRegistry.ADDITIVE.get(), 1D);
        private Pair<Double, Double> initialValue = Pair.of(0D, 0D);
        private Pair<Double, Double> thresholdValue = Pair.of(Double.MIN_VALUE, Double.MAX_VALUE);
        private Function<Double, ? extends Number> formatValue = Double::doubleValue;

        public StatTemplateBuilder(String id) {
            this.id = id;
        }

        private StatTemplateBuilder(StatTemplate base) {
            this.id = base.getId();

            this.upgradeModifier = base.getUpgradeModifier();
            this.initialValue = base.getInitialValue();
            this.thresholdValue = base.getThresholdValue();
            this.formatValue = base.getFormatValue();
        }

        public StatTemplateBuilder upgradeModifier(ScalingModel model, double step) {
            this.upgradeModifier = Pair.of(model, step);

            return this;
        }

        public StatTemplateBuilder initialValue(double min, double max) {
            this.initialValue = Pair.of(min, max);

            return this;
        }

        public StatTemplateBuilder thresholdValue(double min, double max) {
            this.thresholdValue = Pair.of(min, max);

            return this;
        }

        public StatTemplateBuilder formatValue(Function<Double, ? extends Number> formatValue) {
            this.formatValue = formatValue;

            return this;
        }

        public StatTemplate build() {
            return new StatTemplate(id, upgradeModifier, initialValue, thresholdValue, formatValue);
        }
    }
}