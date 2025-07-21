package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import it.hurts.sskirillss.relics.config.data.LevelingConfigData;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LevelingTemplate {
    private final ScalingModel scalingModel;
    private final double initialCost;
    private final double step;
    private final int maxRank;

    public static LevelingTemplateBuilder builder() {
        return new LevelingTemplateBuilder();
    }

    public LevelingTemplateBuilder toBuilder() {
        return new LevelingTemplateBuilder(this);
    }

    public LevelingConfigData toConfigData() {
        return new LevelingConfigData(initialCost, step);
    }

    @NoArgsConstructor
    public static class LevelingTemplateBuilder {
        private ScalingModel scalingModel = ScalingModelRegistry.ADDITIVE.get();
        private double initialCost = 100D;
        private double step = 100D;
        private int maxRank = 5;

        private LevelingTemplateBuilder(LevelingTemplate base) {
            this.scalingModel = base.getScalingModel();
            this.initialCost = base.getInitialCost();
            this.step = base.getStep();
            this.maxRank = base.getMaxRank();
        }

        public LevelingTemplateBuilder scalingModel(ScalingModel scalingModel) {
            this.scalingModel = scalingModel;

            return this;
        }

        public LevelingTemplateBuilder initialCost(double initialCost) {
            this.initialCost = initialCost;

            return this;
        }

        public LevelingTemplateBuilder step(double step) {
            this.step = step;

            return this;
        }

        public LevelingTemplateBuilder maxRank(int maxRank) {
            this.maxRank = maxRank;

            return this;
        }

        public LevelingTemplate build() {
            return new LevelingTemplate(scalingModel, initialCost, step, maxRank);
        }
    }
}