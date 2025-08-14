package it.hurts.sskirillss.relics.api.relics;

import io.netty.util.internal.UnstableApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityStatisticTemplate {
    private final LinkedHashMap<String, AbilityMetricTemplate> metrics;

    public static StatisticTemplateBuilder builder() {
        return new StatisticTemplateBuilder();
    }

    public StatisticTemplateBuilder toBuilder() {
        return new StatisticTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class StatisticTemplateBuilder {
        private LinkedHashMap<String, AbilityMetricTemplate> metrics = new LinkedHashMap<>();

        private StatisticTemplateBuilder(AbilityStatisticTemplate base) {
            this.metrics = base.getMetrics();
        }

        public StatisticTemplateBuilder metric(AbilityMetricTemplate metric) {
            this.metrics.put(metric.getId(), metric);

            return this;
        }

        public StatisticTemplateBuilder metric(String metric) {
            return this.metric(AbilityMetricTemplate.builder(metric).build());
        }

        public AbilityStatisticTemplate build() {
            return new AbilityStatisticTemplate(metrics);
        }
    }
}