package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.api.relics.ScalingModel;
import it.hurts.sskirillss.relics.config.data.LevelingConfigData;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LevelingTemplate {
    @Builder.Default
    private ScalingModel scalingModel = ScalingModelRegistry.ADDITIVE.get();

    @Builder.Default
    private double initialCost = 100D;

    @Builder.Default
    private double step = 100D;

    @Builder.Default
    private int maxLevel = 10;

    @Builder.Default
    private LevelingSourcesTemplate sources = LevelingSourcesTemplate.builder().build();

    @Deprecated(forRemoval = true)
    public LevelingTemplate(double initialCost, int maxLevel, double step) {
        this.initialCost = initialCost;
        this.maxLevel = maxLevel;
        this.step = step;

        this.sources = LevelingSourcesTemplate.builder().build();
    }

    public LevelingConfigData toConfigData() {
        return new LevelingConfigData(initialCost, maxLevel, step);
    }
}