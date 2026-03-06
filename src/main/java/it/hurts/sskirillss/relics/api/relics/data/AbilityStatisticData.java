package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.AbilityStatisticComponent;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;

public class AbilityStatisticData {
    private final AbilityData abilityData;

    public AbilityStatisticData(AbilityData abilityData) {
        this.abilityData = abilityData;
    }

    public AbilityData getAbilityData() {
        return this.abilityData;
    }

    public AbilityStatisticComponent getComponent() {
        return this.getAbilityData().getComponent().getStatistic();
    }

    public void setComponent(AbilityStatisticComponent component) {
        this.getAbilityData().setComponent(this.getAbilityData().getComponent().toBuilder()
                .statistic(component)
                .build());
    }

    public AbilityStatisticTemplate getTemplate() {
        return this.getAbilityData().getTemplate().getStatistic();
    }

    public AbilityMetricData getMetricData(String metric) {
        return new AbilityMetricData(this, metric);
    }
}
