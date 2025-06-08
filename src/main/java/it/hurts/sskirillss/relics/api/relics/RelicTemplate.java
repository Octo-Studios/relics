package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RelicTemplate {
    private final AbilitiesTemplate abilities;
    private final StatisticTemplate statistic;
    private final LevelingTemplate leveling;
    private final StyleTemplate style;
    private final LootTemplate loot;

    public static RelicTemplateBuilder builder() {
        return new RelicTemplateBuilder();
    }

    public RelicTemplateBuilder toBuilder() {
        return new RelicTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class RelicTemplateBuilder {
        private AbilitiesTemplate abilities = AbilitiesTemplate.builder().build();
        private StatisticTemplate statistic = StatisticTemplate.builder().build();
        private LevelingTemplate leveling = LevelingTemplate.builder().build();
        private StyleTemplate style = StyleTemplate.builder().build();
        private LootTemplate loot = LootTemplate.builder().build();

        private RelicTemplateBuilder(RelicTemplate base) {
            this.abilities = base.getAbilities();
            this.statistic = base.getStatistic();
            this.leveling = base.getLeveling();
            this.style = base.getStyle();
            this.loot = base.getLoot();
        }

        public RelicTemplateBuilder abilities(AbilitiesTemplate abilities) {
            this.abilities = abilities;

            return this;
        }

        public RelicTemplateBuilder statistic(StatisticTemplate statistic) {
            this.statistic = statistic;

            return this;
        }

        public RelicTemplateBuilder leveling(LevelingTemplate leveling) {
            this.leveling = leveling;

            return this;
        }

        public RelicTemplateBuilder style(StyleTemplate style) {
            this.style = style;

            return this;
        }

        public RelicTemplateBuilder loot(LootTemplate loot) {
            this.loot = loot;

            return this;
        }

        public RelicTemplate build() {
            return new RelicTemplate(abilities, statistic, leveling, style, loot);
        }
    }
}