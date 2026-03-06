package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricComponent;

public class AbilityMetricData {
    private final AbilityStatisticData statisticData;
    private final String metric;

    public AbilityMetricData(AbilityStatisticData statisticData, String metric) {
        this.statisticData = statisticData;
        this.metric = metric;
    }

    public AbilityStatisticData getStatisticData() {
        return this.statisticData;
    }

    public String getId() {
        return this.metric;
    }

    public AbilityMetricComponent getComponent() {
        var template = this.getStatisticData().getTemplate().getMetrics().get(this.getId());

        if (template == null)
            return null;

        var statisticComponent = this.getStatisticData().getComponent();
        var metricComponent = statisticComponent.getMetrics().get(this.getId());

        if (metricComponent != null)
            return metricComponent;

        metricComponent = AbilityMetricComponent.EMPTY;

        this.getStatisticData().setComponent(statisticComponent.toBuilder()
                .metric(this.getId(), metricComponent)
                .build());

        return metricComponent;
    }

    public void setComponent(AbilityMetricComponent component) {
        this.getStatisticData().setComponent(this.getStatisticData().getComponent().toBuilder()
                .metric(this.getId(), component)
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
