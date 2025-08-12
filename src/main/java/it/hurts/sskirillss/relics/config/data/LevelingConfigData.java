package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.module.config.annotation.Prop;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LevelingConfigData {
    @Prop(comment = "Amount of experience required to level up to relic level 1")
    private int initialCost;
    @Prop(comment = "Maximum level of the relic")
    private int maxLevel;
    @Prop(comment = "Increment in experience required for each subsequent level of the relic")
    private int step;

    public LevelingData toData(IRelicItem relic) {
        LevelingData data = relic.getLevelingData();

        data.setInitialCost(initialCost);
        data.setMaxLevel(maxLevel);
        data.setStep(step);

        return data;
    }
}