package it.hurts.sskirillss.relics.api.relics.abilities;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Function3;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.config.data.AbilityConfigData;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityTemplate {
    private final String id;

    private final Function3<Player, ItemStack, String, String> icon;
    private final Map<String, AbilityStatTemplate> stats;
    private final int initialMaxLevel;
    private final double maxLevelRankModifier;
    private final int requiredLevel;
    private final int requiredPoints;
    private final ResearchTemplate researchTemplate;
    private final AbilityStatisticTemplate statistic;
    private final List<String> modes;
    private final ExperienceSourcesTemplate experienceSources;
    private final Multimap<Integer, String> rankModifiers;

    public static AbilityTemplateBuilder builder(String id) {
        return new AbilityTemplateBuilder(id);
    }

    public AbilityTemplateBuilder toBuilder() {
        return new AbilityTemplateBuilder(this);
    }

    public AbilityConfigData toConfigData() {
        return new AbilityConfigData(requiredPoints, requiredLevel, initialMaxLevel, stats.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toConfigData(), (o1, o2) -> o1, LinkedHashMap::new)));
    }

    public static class AbilityTemplateBuilder {
        private final String id;

        private Function3<Player, ItemStack, String, String> icon = (player, stack, ability) -> {
            var relic = (IRelicItem) stack.getItem();
            var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);

            if (abilityData == null)
                return ability;

            var modes = abilityData.getTemplate().getModes();

            return ability + (modes.isEmpty() ? "" : "_" + abilityData.getMode());
        };
        private Map<String, AbilityStatTemplate> stats = new LinkedHashMap<>();
        private int initialMaxLevel = 10;
        private double maxLevelRankModifier = 0.25D;
        private int requiredLevel = 0;
        private int requiredPoints = 1;
        private ResearchTemplate researchTemplate = ResearchTemplate.builder().build();
        private AbilityStatisticTemplate statistic = AbilityStatisticTemplate.builder().build();
        private List<String> modes = new ArrayList<>();
        private ExperienceSourcesTemplate experienceSources = ExperienceSourcesTemplate.builder().build();
        private Multimap<Integer, String> rankModifiers = LinkedHashMultimap.create();

        public AbilityTemplateBuilder(String id) {
            this.id = id;
        }

        private AbilityTemplateBuilder(AbilityTemplate base) {
            this.id = base.getId();

            this.icon = base.getIcon();
            this.stats = new LinkedHashMap<>(base.getStats());
            this.initialMaxLevel = base.getInitialMaxLevel();
            this.maxLevelRankModifier = base.getMaxLevelRankModifier();
            this.requiredLevel = base.getRequiredLevel();
            this.requiredPoints = base.getRequiredPoints();
            this.researchTemplate = base.getResearchTemplate();
            this.statistic = base.getStatistic();
            this.experienceSources = base.getExperienceSources();
            this.modes = base.getModes();
            this.rankModifiers = base.getRankModifiers();
        }

        public AbilityTemplateBuilder icon(Function3<Player, ItemStack, String, String> icon) {
            this.icon = icon;

            return this;
        }

        public AbilityTemplateBuilder stats(Map<String, AbilityStatTemplate> stats) {
            this.stats = stats;

            return this;
        }

        public AbilityTemplateBuilder stat(AbilityStatTemplate stat) {
            this.stats.put(stat.getId(), stat);

            return this;
        }

        public AbilityTemplateBuilder initialMaxLevel(int maxLevel) {
            this.initialMaxLevel = maxLevel;

            return this;
        }

        public AbilityTemplateBuilder maxLevelRankModifier(double maxLevelRankModifier) {
            this.maxLevelRankModifier = maxLevelRankModifier;

            return this;
        }

        public AbilityTemplateBuilder requiredLevel(int requiredLevel) {
            this.requiredLevel = requiredLevel;

            return this;
        }

        public AbilityTemplateBuilder requiredPoints(int requiredPoints) {
            this.requiredPoints = requiredPoints;

            return this;
        }

        public AbilityTemplateBuilder research(ResearchTemplate researchTemplate) {
            this.researchTemplate = researchTemplate;

            return this;
        }

        public AbilityTemplateBuilder statistic(AbilityStatisticTemplate statistic) {
            this.statistic = statistic;

            return this;
        }

        public AbilityTemplateBuilder modes(String... mode) {
            this.modes.addAll(Lists.newArrayList(mode));

            return this;
        }

        public AbilityTemplateBuilder experienceSources(ExperienceSourcesTemplate sources) {
            this.experienceSources = sources;

            return this;
        }

        public AbilityTemplateBuilder rankModifier(int rank, String modifier) {
            this.rankModifiers.put(rank, modifier);

            return this;
        }

        public AbilityTemplate build() {
            return new AbilityTemplate(this.id, this.icon, this.stats, this.initialMaxLevel, this.maxLevelRankModifier, this.requiredLevel, this.requiredPoints, this.researchTemplate, this.statistic, this.modes, this.experienceSources, this.rankModifiers);
        }
    }
}
