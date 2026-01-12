package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.AbilityStatisticComponent;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;

public class AbilityStatisticData {
    private final AbilityData abilityData;

    public AbilityStatisticData(AbilityData abilityData) {
        this.abilityData = abilityData;
    }

    public AbilityStatisticComponent getComponent() {
        return abilityData.getComponent().getStatistic();
    }

    public void setComponent(AbilityStatisticComponent component) {
        abilityData.setComponent(abilityData.getComponent().toBuilder()
                .statistic(component)
                .build());
    }

    public AbilityStatisticTemplate getTemplate() {
        return abilityData.getTemplate().getStatistic();
    }

    public AbilityMetricData getMetricData(String metric) {
        return new AbilityMetricData(this, metric);
    }
}
