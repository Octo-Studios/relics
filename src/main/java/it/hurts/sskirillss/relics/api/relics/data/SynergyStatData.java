package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.synergies.stats.SynergyStatTemplate;

public class SynergyStatData {
    private final SynergyData synergyData;
    private final String stat;

    public SynergyStatData(SynergyData synergyData, String stat) {
        this.synergyData = synergyData;
        this.stat = stat;
    }

    public SynergyData getSynergyData() {
        return this.synergyData;
    }

    public String getId() {
        return this.stat;
    }

    public SynergyStatTemplate getTemplate() {
        var synergyTemplate = this.getSynergyData().getTemplate();

        return synergyTemplate == null ? null : synergyTemplate.getStats().get(this.getId());
    }

    public double getValue() {
        return this.getValueForProgress(this.getSynergyData().getProgress());
    }

    public double getValueForProgress(double progress) {
        var template = getTemplate();

        if (template == null)
            return 0D;

        var threshold = template.getThresholdValue();
        var min = threshold.getMinValue();
        var max = threshold.getMaxValue();

        if (min == max)
            return min;

        var clamped = Math.clamp(progress, 0D, 1D);

        return min + (max - min) * clamped;
    }
}
