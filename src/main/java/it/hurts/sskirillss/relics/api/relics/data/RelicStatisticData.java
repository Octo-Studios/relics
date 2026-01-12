package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.RelicStatisticComponent;
import it.hurts.sskirillss.relics.api.relics.RelicStatisticTemplate;

public class RelicStatisticData {
    private final RelicData relicData;

    public RelicStatisticData(RelicData relicData) {
        this.relicData = relicData;
    }

    public RelicStatisticComponent getComponent() {
        return relicData.getComponent().getStatistic();
    }

    public void setComponent(RelicStatisticComponent component) {
        relicData.setComponent(relicData.getComponent().toBuilder()
                .statistic(component)
                .build());
    }

    public RelicStatisticTemplate getTemplate() {
        return relicData.getTemplate().getStatistic();
    }

    public RelicMetricData getMetricData(String metric) {
        return new RelicMetricData(this, metric);
    }
}
