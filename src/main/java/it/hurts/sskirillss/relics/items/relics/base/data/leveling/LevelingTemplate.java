package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import it.hurts.sskirillss.relics.config.data.LevelingConfigData;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
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
    private final double rankupCost;
    private final ScalingModel rankScalingModel;
    private final double rankStep;

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
        private ScalingModel scalingModel = RelicsScalingModels.ADDITIVE.get();
        private double initialCost = 100D;
        private double step = 100D;
        private int maxRank = 5;
        private double rankupCost = 2000D;
        private ScalingModel rankScalingModel = RelicsScalingModels.EXPONENTIAL.get();
        private double rankStep = 0.25D;

        private LevelingTemplateBuilder(LevelingTemplate base) {
            this.scalingModel = base.getScalingModel();
            this.initialCost = base.getInitialCost();
            this.step = base.getStep();
            this.maxRank = base.getMaxRank();
            this.rankupCost = base.getRankupCost();
            this.rankScalingModel = base.getRankScalingModel();
            this.rankStep = base.getRankStep();
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

        public LevelingTemplateBuilder rankupCost(double rankupCost) {
            this.rankupCost = rankupCost;

            return this;
        }

        public LevelingTemplateBuilder rankScalingModel(ScalingModel rankScalingModel) {
            this.rankScalingModel = rankScalingModel;

            return this;
        }

        public LevelingTemplateBuilder rankStep(double rankStep) {
            this.rankStep = rankStep;

            return this;
        }

        public LevelingTemplate build() {
            return new LevelingTemplate(scalingModel, initialCost, step, maxRank, rankupCost, rankScalingModel, rankStep);
        }
    }
}