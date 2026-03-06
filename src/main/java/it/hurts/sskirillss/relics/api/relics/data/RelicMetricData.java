package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.RelicMetricComponent;

public class RelicMetricData {
    private final RelicStatisticData statisticData;
    private final String metric;

    public RelicMetricData(RelicStatisticData statisticData, String metric) {
        this.statisticData = statisticData;
        this.metric = metric;
    }

    public RelicStatisticData getStatisticData() {
        return this.statisticData;
    }

    public String getId() {
        return this.metric;
    }

    public RelicMetricComponent getComponent() {
        var template = this.getStatisticData().getTemplate().getMetrics().get(this.getId());

        if (template == null)
            return null;

        var statisticComponent = this.getStatisticData().getComponent();
        var metricComponent = statisticComponent.getMetrics().get(this.getId());

        if (metricComponent != null)
            return metricComponent;

        metricComponent = RelicMetricComponent.EMPTY;

        this.getStatisticData().setComponent(statisticComponent.toBuilder()
                .metric(this.getId(), metricComponent)
                .build());

        return metricComponent;
    }

    public void setComponent(RelicMetricComponent component) {
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
