package it.hurts.sskirillss.relics.api.relics.abilities.stats;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.misc.InitialValue;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.misc.ThresholdValue;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.misc.UpgradeModifier;
import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import it.hurts.sskirillss.relics.config.data.StatConfigData;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatTemplate {
    private final String id;

    private final UpgradeModifier upgradeModifier;
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
        return new StatConfigData(initialValue.getMinValue(), initialValue.getMaxValue(), thresholdValue.getMinValue(), thresholdValue.getMaxValue(), RelicsRegistries.SCALING_MODEL_REGISTRY.getKey(upgradeModifier.getScalingModel()).toString(), upgradeModifier.getModifier());
    }

    public static class StatTemplateBuilder {
        private final String id;

        private UpgradeModifier upgradeModifier = new UpgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1D);
        private InitialValue initialValue = new InitialValue(0D, 0D, 0D);
        private ThresholdValue thresholdValue = new ThresholdValue(Double.MIN_VALUE, Double.MAX_VALUE);
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
            this.upgradeModifier = new UpgradeModifier(model, step);

            return this;
        }

        public StatTemplateBuilder initialValue(double min, double max) {
            return initialValue(min, max, 0D);
        }

        public StatTemplateBuilder initialValue(double min, double max, double step) {
            this.initialValue = new InitialValue(min, max, step);

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

        public StatTemplate build() {
            return new StatTemplate(id, upgradeModifier, initialValue, thresholdValue, formatValue);
        }
    }
}