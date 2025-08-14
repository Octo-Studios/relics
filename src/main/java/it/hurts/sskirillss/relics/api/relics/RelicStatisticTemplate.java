package it.hurts.sskirillss.relics.api.relics;

import io.netty.util.internal.UnstableApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RelicStatisticTemplate {
    private final LinkedHashMap<String, RelicMetricTemplate> metrics;

    public static StatisticTemplateBuilder builder() {
        return new StatisticTemplateBuilder();
    }

    public StatisticTemplateBuilder toBuilder() {
        return new StatisticTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class StatisticTemplateBuilder {
        private LinkedHashMap<String, RelicMetricTemplate> metrics = new LinkedHashMap<>();

        private StatisticTemplateBuilder(RelicStatisticTemplate base) {
            this.metrics = base.getMetrics();
        }

        public StatisticTemplateBuilder metric(RelicMetricTemplate metric) {
            this.metrics.put(metric.getId(), metric);

            return this;
        }

        public StatisticTemplateBuilder metric(String metric) {
            return this.metric(RelicMetricTemplate.builder(metric).build());
        }

        public RelicStatisticTemplate build() {
            return new RelicStatisticTemplate(metrics);
        }
    }
}