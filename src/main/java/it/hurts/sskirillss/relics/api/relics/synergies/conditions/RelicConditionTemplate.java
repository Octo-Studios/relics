package it.hurts.sskirillss.relics.api.relics.synergies.conditions;

import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RelicConditionTemplate {
    private final Supplier<IRelicItem> relic;

    private final List<AbilityConditionTemplate> abilityConditions;
    private final List<RelicContainer> relicContainers;
    private final int level;
    private final int rank;
    private final int quality;
    private final double progress;

    public static RelicConditionTemplateBuilder builder(Supplier<IRelicItem> relic) {
        return new RelicConditionTemplateBuilder(relic);
    }

    public RelicConditionTemplateBuilder toBuilder() {
        return new RelicConditionTemplateBuilder(this);
    }

    public static class RelicConditionTemplateBuilder {
        private Supplier<IRelicItem> relic;

        private List<AbilityConditionTemplate> abilityConditions = new ArrayList<>();
        private List<RelicContainer> relicContainers = new ArrayList<>();
        private int level = 0;
        private int rank = 0;
        private int quality = 0;
        private double progress = 0;

        public RelicConditionTemplateBuilder(Supplier<IRelicItem> relic) {
            this.relic = relic;
        }

        private RelicConditionTemplateBuilder(RelicConditionTemplate base) {
            this.relic = base.getRelic();

            this.abilityConditions = base.getAbilityConditions();
            this.relicContainers = base.getRelicContainers();
            this.level = base.getLevel();
            this.rank = base.getRank();
            this.quality = base.getQuality();
            this.progress = base.getProgress();
        }

        public RelicConditionTemplateBuilder condition(AbilityConditionTemplate condition) {
            this.abilityConditions.add(condition);

            return this;
        }

        public RelicConditionTemplateBuilder container(RelicContainer... containers) {
            this.relicContainers.addAll(List.of(containers));

            return this;
        }

        public RelicConditionTemplateBuilder level(int level) {
            this.level = level;

            return this;
        }

        public RelicConditionTemplateBuilder rank(int rank) {
            this.rank = rank;

            return this;
        }

        public RelicConditionTemplateBuilder quality(int quality) {
            this.quality = quality;

            return this;
        }

        public RelicConditionTemplateBuilder progress(double progress) {
            this.progress = progress;

            return this;
        }

        public RelicConditionTemplate build() {
            return new RelicConditionTemplate(relic, abilityConditions, relicContainers, level, rank, quality, progress);
        }
    }
}
