package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.RelicStatisticComponent;
import it.hurts.sskirillss.relics.api.relics.RelicStatisticTemplate;

public class RelicStatisticData {
    private final RelicData relicData;

    public RelicStatisticData(RelicData relicData) {
        this.relicData = relicData;
    }

    public RelicData getRelicData() {
        return this.relicData;
    }

    public RelicStatisticComponent getComponent() {
        return this.getRelicData().getComponent().getStatistic();
    }

    public void setComponent(RelicStatisticComponent component) {
        this.getRelicData().setComponent(this.getRelicData().getComponent().toBuilder()
                .statistic(component)
                .build());
    }

    public RelicStatisticTemplate getTemplate() {
        return this.getRelicData().getTemplate().getStatistic();
    }

    public RelicMetricData getMetricData(String metric) {
        return new RelicMetricData(this, metric);
    }
}
