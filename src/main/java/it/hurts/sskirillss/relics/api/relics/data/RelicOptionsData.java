package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.OptionsComponent;

public class RelicOptionsData {
    private final RelicData relicData;

    public RelicOptionsData(RelicData relicData) {
        this.relicData = relicData;
    }

    public RelicData getRelicData() {
        return this.relicData;
    }

    public OptionsComponent getComponent() {
        return this.getRelicData().getComponent().getOptions();
    }

    public void setComponent(OptionsComponent component) {
        this.getRelicData().setComponent(this.getRelicData().getComponent().toBuilder()
                .options(component)
                .build());
    }

    public boolean isVisuallyFlawless() {
        return this.getComponent().isVisuallyFlawless();
    }

    public void setVisuallyFlawless(boolean visuallyFlawless) {
        this.setComponent(this.getComponent().toBuilder()
                .visuallyFlawless(visuallyFlawless)
                .build());
    }
}
