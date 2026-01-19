package it.hurts.sskirillss.relics.api.relics.synergies.stats;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.misc.ThresholdValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SynergyStatTemplate {
    private final String id;

    private final ThresholdValue thresholdValue;
    private final Function<Double, ? extends Number> formatValue;

    public static StatTemplateBuilder builder(String id) {
        return new StatTemplateBuilder(id);
    }

    public StatTemplateBuilder toBuilder() {
        return new StatTemplateBuilder(this);
    }

    public static class StatTemplateBuilder {
        private final String id;

        private ThresholdValue thresholdValue = new ThresholdValue(Double.MIN_VALUE, Double.MAX_VALUE);
        private Function<Double, ? extends Number> formatValue = Double::doubleValue;

        public StatTemplateBuilder(String id) {
            this.id = id;
        }

        private StatTemplateBuilder(SynergyStatTemplate base) {
            this.id = base.getId();

            this.thresholdValue = base.getThresholdValue();
            this.formatValue = base.getFormatValue();
        }

        public StatTemplateBuilder thresholdValue(double min, double max) {
            this.thresholdValue = new ThresholdValue(min, max);

            return this;
        }

        public StatTemplateBuilder formatValue(Function<Double, ? extends Number> formatValue) {
            this.formatValue = formatValue;

            return this;
        }

        public SynergyStatTemplate build() {
            return new SynergyStatTemplate(id, thresholdValue, formatValue);
        }
    }
}