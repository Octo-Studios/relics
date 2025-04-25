package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.modules.config.annotations.Prop;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelingConfigData {
    @Prop(comment = "Amount of experience required to level up to relic level 1")
    private double initialCost;
    @Prop(comment = "Maximum level of the relic")
    private int maxLevel;
    @Prop(comment = "Increment in experience required for each subsequent level of the relic")
    private double step;

    public LevelingTemplate toData(IRelicItem relic) {
        var data = relic.getDefaultLevelingTemplate();

        data.setInitialCost(initialCost);
        data.setMaxLevel(maxLevel);
        data.setStep(step);

        return data;
    }
}