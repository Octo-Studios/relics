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
    private final int maxLevel;
    private final LevelingSourcesTemplate sources;

    public static LevelingTemplateBuilder builder() {
        return new LevelingTemplateBuilder();
    }

    public LevelingTemplateBuilder toBuilder() {
        return new LevelingTemplateBuilder(this);
    }

    public LevelingConfigData toConfigData() {
        return new LevelingConfigData(initialCost, maxLevel, step);
    }

    @NoArgsConstructor
    public static class LevelingTemplateBuilder {
        private ScalingModel scalingModel = ScalingModelRegistry.ADDITIVE.get();
        private double initialCost = 100D;
        private double step = 100D;
        private int maxLevel = 10;
        private LevelingSourcesTemplate sources = LevelingSourcesTemplate.builder().build();

        private LevelingTemplateBuilder(LevelingTemplate base) {
            this.scalingModel = base.getScalingModel();
            this.initialCost = base.getInitialCost();
            this.step = base.getStep();
            this.maxLevel = base.getMaxLevel();
            this.sources = base.getSources();
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

        public LevelingTemplateBuilder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;

            return this;
        }

        public LevelingTemplateBuilder sources(LevelingSourcesTemplate sources) {
            this.sources = sources;

            return this;
        }

        public LevelingTemplate build() {
            return new LevelingTemplate(scalingModel, initialCost, step, maxLevel, sources);
        }
    }
}