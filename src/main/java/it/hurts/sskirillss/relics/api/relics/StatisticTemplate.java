package it.hurts.sskirillss.relics.api.relics;

import io.netty.util.internal.UnstableApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatisticTemplate {
    private final Map<String, MetricTemplate> metrics;

    public static StatisticTemplateBuilder builder() {
        return new StatisticTemplateBuilder();
    }

    public StatisticTemplateBuilder toBuilder() {
        return new StatisticTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class StatisticTemplateBuilder {
        private Map<String, MetricTemplate> metrics = new HashMap<>();

        private StatisticTemplateBuilder(StatisticTemplate base) {
            this.metrics = base.getMetrics();
        }

        public StatisticTemplateBuilder metric(MetricTemplate metric) {
            this.metrics.put(metric.getId(), metric);

            return this;
        }

        @UnstableApi
        public StatisticTemplateBuilder metric(String metric) {
            return this.metric(MetricTemplate.builder(metric).build());
        }

        public StatisticTemplate build() {
            return new StatisticTemplate(metrics);
        }
    }
}