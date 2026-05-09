package it.hurts.sskirillss.relics.api.relics.abilities.stats;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.misc.InitialValue;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.misc.TargetValue;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.misc.ThresholdValue;
import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import it.hurts.sskirillss.relics.config.data.StatConfigData;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityStatTemplate {
    private final String id;

    private final TargetValue targetValue;
    private final InitialValue initialValue;
    private final ThresholdValue thresholdValue;
    private final Function<Double, ? extends Number> formatValue;

    public static StatTemplateBuilder builder(String id) {
        return new StatTemplateBuilder(id);
    }

    public StatTemplateBuilder toBuilder() {
        return new StatTemplateBuilder(this);
    }

    public StatConfigData toConfigData() {
        return new StatConfigData(initialValue.getMinValue(), initialValue.getMaxValue(), thresholdValue.getMinValue(), thresholdValue.getMaxValue(), RelicsRegistries.SCALING_MODEL_REGISTRY.getKey(targetValue.getScalingModel()).toString(), targetValue.getTargetValue());
    }

    public static class StatTemplateBuilder {
        private final String id;

        private TargetValue targetValue = new TargetValue(RelicsScalingModels.ADDITIVE.get(), 1D);
        private InitialValue initialValue = new InitialValue(0D, 0D);
        private ThresholdValue thresholdValue = new ThresholdValue(Double.MIN_VALUE, Double.MAX_VALUE);
        private Function<Double, ? extends Number> formatValue = Double::doubleValue;

        public StatTemplateBuilder(String id) {
            this.id = id;
        }

        private StatTemplateBuilder(AbilityStatTemplate base) {
            this.id = base.getId();

            this.targetValue = base.getTargetValue();
            this.initialValue = base.getInitialValue();
            this.thresholdValue = base.getThresholdValue();
            this.formatValue = base.getFormatValue();
        }

        public StatTemplateBuilder targetValue(ScalingModel model, double targetValue) {
            this.targetValue = new TargetValue(model, targetValue);

            return this;
        }

        public StatTemplateBuilder initialValue(double min, double max) {
            this.initialValue = new InitialValue(min, max);

            return this;
        }

        public StatTemplateBuilder thresholdValue(double min, double max) {
            this.thresholdValue = new ThresholdValue(min, max);

            return this;
        }

        public StatTemplateBuilder formatValue(Function<Double, ? extends Number> formatValue) {
            this.formatValue = formatValue;

            return this;
        }

        public AbilityStatTemplate build() {
            return new AbilityStatTemplate(id, targetValue, initialValue, thresholdValue, formatValue);
        }
    }
}
