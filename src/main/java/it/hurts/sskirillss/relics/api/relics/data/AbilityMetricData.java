package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricComponent;

public class AbilityMetricData {
    private final AbilityStatisticData statisticData;
    private final String metric;

    public AbilityMetricData(AbilityStatisticData statisticData, String metric) {
        this.statisticData = statisticData;
        this.metric = metric;
    }

    public AbilityMetricComponent getComponent() {
        var template = statisticData.getTemplate().getMetrics().get(metric);

        if (template == null)
            return null;

        var statisticComponent = statisticData.getComponent();
        var metricComponent = statisticComponent.getMetrics().get(metric);

        if (metricComponent != null)
            return metricComponent;

        metricComponent = AbilityMetricComponent.EMPTY;

        statisticData.setComponent(statisticComponent.toBuilder()
                .metric(metric, metricComponent)
                .build());

        return metricComponent;
    }

    public void setComponent(AbilityMetricComponent component) {
        statisticData.setComponent(statisticData.getComponent().toBuilder()
                .metric(metric, component)
                .build());
    }

    public double getValue() {
        var component = getComponent();

        return component == null ? 0D : component.getValue();
    }

    public void setValue(double value) {
        var component = getComponent();

        if (component == null)
            return;

        setComponent(component.toBuilder().value(value).build());
    }

    public void addValue(double value) {
        setValue(getValue() + value);
    }
}
