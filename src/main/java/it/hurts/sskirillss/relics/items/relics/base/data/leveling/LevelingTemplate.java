package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.config.data.LevelingConfigData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LevelingTemplate {
    @Builder.Default
    private int initialCost = 100;

    @Builder.Default
    private int maxLevel = 10;

    @Builder.Default
    private int step = 100;

    @Builder.Default
    private LevelingSourcesTemplate sources = LevelingSourcesTemplate.builder().build();

    @Deprecated(forRemoval = true)
    public LevelingTemplate(int initialCost, int maxLevel, int step) {
        this.initialCost = initialCost;
        this.maxLevel = maxLevel;
        this.step = step;

        this.sources = LevelingSourcesTemplate.builder().build();
    }

    public LevelingConfigData toConfigData() {
        return new LevelingConfigData(initialCost, maxLevel, step);
    }
}