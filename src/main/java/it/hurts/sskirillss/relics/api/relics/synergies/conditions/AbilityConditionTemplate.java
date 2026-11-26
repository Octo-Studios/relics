package it.hurts.sskirillss.relics.api.relics.synergies.conditions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityConditionTemplate {
    private final String id;

    private final double progress;
    private final int quality;

    public static AbilityConditionTemplateBuilder builder(String id) {
        return new AbilityConditionTemplateBuilder(id);
    }

    public AbilityConditionTemplateBuilder toBuilder() {
        return new AbilityConditionTemplateBuilder(this);
    }

    public static class AbilityConditionTemplateBuilder {
        private final String id;

        private double progress = 0;
        private int quality = 0;

        public AbilityConditionTemplateBuilder(String id) {
            this.id = id;
        }

        private AbilityConditionTemplateBuilder(AbilityConditionTemplate base) {
            this.id = base.getId();

            this.progress = base.getProgress();
            this.quality = base.getQuality();
        }

        public AbilityConditionTemplateBuilder progress(double progress) {
            this.progress = progress;

            return this;
        }

        public AbilityConditionTemplateBuilder quality(int quality) {
            this.quality = quality;

            return this;
        }

        public AbilityConditionTemplate build() {
            return new AbilityConditionTemplate(id, progress, quality);
        }
    }
}
