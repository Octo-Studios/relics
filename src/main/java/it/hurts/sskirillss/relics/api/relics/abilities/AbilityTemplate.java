package it.hurts.sskirillss.relics.api.relics.abilities;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Function3;
import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationPredicateContext;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationPredicateType;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationType;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingTemplate;
import it.hurts.sskirillss.relics.config.data.AbilityConfigData;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityTemplate {
    private final String id;

    private final List<RelicContainer> activeContainers;
    private final Function3<Player, ItemStack, String, String> icon;
    private final Map<String, AbilityStatTemplate> stats;
    private final int levelContribution;
    private final double levelContributionRankModifier;
    private final int requiredLevel;
    private final int requiredRank;
    private final int levelWeight;
    private final ResearchTemplate researchTemplate;
    private final AbilityStatisticTemplate statistic;
    private final List<String> modes;
    private final ExperienceSourcesTemplate experienceSources;
    private final Multimap<Integer, String> rankModifiers;
    private final AbilityActivationTemplate activation;
    private final AbilityTargetingTemplate targeting;

    public static AbilityTemplateBuilder builder(String id) {
        return new AbilityTemplateBuilder(id);
    }

    public AbilityTemplateBuilder toBuilder() {
        return new AbilityTemplateBuilder(this);
    }

    @Deprecated(forRemoval = true)
    public int getInitialMaxLevel() {
        return getLevelContribution();
    }

    @Deprecated(forRemoval = true)
    public double getMaxLevelRankModifier() {
        return getLevelContributionRankModifier();
    }

    @Deprecated(forRemoval = true)
    public int getRequiredPoints() {
        return getLevelWeight();
    }

    public int getLevelContribution(int rank) {
        var levels = levelContribution;

        for (var i = 0; i < rank; i++)
            levels += (int) Math.ceil(levels * levelContributionRankModifier);

        return levels;
    }

    public AbilityConfigData toConfigData() {
        return new AbilityConfigData(levelWeight, requiredLevel, levelContribution, stats.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toConfigData(), (o1, o2) -> o1, LinkedHashMap::new)));
    }

    public static class AbilityTemplateBuilder {
        private final String id;

        private List<RelicContainer> activeContainers = List.of();
        private Function3<Player, ItemStack, String, String> icon = (player, stack, ability) -> {
            var relic = (IRelicItem) stack.getItem();
            var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);

            if (abilityData == null)
                return ability;

            var modes = abilityData.getTemplate().getModes();

            return ability + (modes.isEmpty() ? "" : "_" + abilityData.getMode());
        };
        private Map<String, AbilityStatTemplate> stats = new LinkedHashMap<>();
        private int levelContribution = 10;
        private double levelContributionRankModifier = 0.25D;
        private int requiredLevel = 0;
        private int requiredRank = 0;
        private int levelWeight = 1;
        private ResearchTemplate researchTemplate = ResearchTemplate.builder().build();
        private AbilityStatisticTemplate statistic = AbilityStatisticTemplate.builder().build();
        private List<String> modes = new ArrayList<>();
        private ExperienceSourcesTemplate experienceSources = ExperienceSourcesTemplate.builder().build();
        private Multimap<Integer, String> rankModifiers = LinkedHashMultimap.create();
        private AbilityActivationTemplate activation = AbilityActivationTemplate.EMPTY;
        private AbilityTargetingTemplate targeting = AbilityTargetingTemplate.EMPTY;

        public AbilityTemplateBuilder(String id) {
            this.id = id;
        }

        private AbilityTemplateBuilder(AbilityTemplate base) {
            this.id = base.getId();

            this.activeContainers = base.getActiveContainers();
            this.icon = base.getIcon();
            this.stats = new LinkedHashMap<>(base.getStats());
            this.levelContribution = base.getLevelContribution();
            this.levelContributionRankModifier = base.getLevelContributionRankModifier();
            this.requiredLevel = base.getRequiredLevel();
            this.requiredRank = base.getRequiredRank();
            this.levelWeight = base.getLevelWeight();
            this.researchTemplate = base.getResearchTemplate();
            this.statistic = base.getStatistic();
            this.experienceSources = base.getExperienceSources();
            this.modes = base.getModes();
            this.rankModifiers = base.getRankModifiers();
            this.activation = base.getActivation();
            this.targeting = base.getTargeting();
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

        public AbilityTemplateBuilder levelContribution(int levelContribution) {
            this.levelContribution = levelContribution;

            return this;
        }

        @Deprecated(forRemoval = true)
        public AbilityTemplateBuilder initialMaxLevel(int maxLevel) {
            return levelContribution(maxLevel);
        }

        public AbilityTemplateBuilder levelContributionRankModifier(double levelContributionRankModifier) {
            this.levelContributionRankModifier = levelContributionRankModifier;

            return this;
        }

        @Deprecated(forRemoval = true)
        public AbilityTemplateBuilder maxLevelRankModifier(double maxLevelRankModifier) {
            return levelContributionRankModifier(maxLevelRankModifier);
        }

        public AbilityTemplateBuilder requiredLevel(int requiredLevel) {
            this.requiredLevel = requiredLevel;

            return this;
        }

        public AbilityTemplateBuilder requiredRank(int requiredRank) {
            this.requiredRank = requiredRank;

            return this;
        }

        public AbilityTemplateBuilder levelWeight(int levelWeight) {
            this.levelWeight = levelWeight;

            return this;
        }

        @Deprecated(forRemoval = true)
        public AbilityTemplateBuilder requiredPoints(int requiredPoints) {
            return levelWeight(requiredPoints);
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

        public AbilityTemplateBuilder activation(AbilityActivationTemplate activation) {
            this.activation = activation;

            return this;
        }

        public AbilityTemplateBuilder targeting(AbilityTargetingTemplate targeting) {
            this.targeting = targeting;

            return this;
        }

        public AbilityTemplateBuilder active(AbilityActivationTemplate activation) {
            return activation(activation);
        }

        public AbilityTemplateBuilder active(AbilityActivationType type) {
            return activation(AbilityActivationTemplate.builder(type).build());
        }

        public AbilityTemplateBuilder passive() {
            return activation(AbilityActivationTemplate.EMPTY);
        }

        public AbilityTemplateBuilder activationPredicate(String id, AbilityActivationPredicateType type, Predicate<AbilityActivationPredicateContext> predicate) {
            var builder = AbilityActivationTemplate.builder(this.activation.getType())
                    .containers(this.activation.getContainers())
                    .predicates(new LinkedHashMap<>(this.activation.getPredicates()));

            this.activation = builder.predicate(id, type, predicate).build();

            return this;
        }

        public AbilityTemplateBuilder activeContainers(RelicContainer... containers) {
            this.activeContainers = List.of(containers);

            return this;
        }

        public AbilityTemplate build() {
            var activation = this.activation;

            if (!this.modes.isEmpty() && !activation.isActive())
                activation = AbilityActivationTemplate.builder(AbilityActivationType.CYCLE_MODE)
                        .containers(activation.getContainers())
                        .predicates(new LinkedHashMap<>(activation.getPredicates()))
                        .build();

            return new AbilityTemplate(this.id, activeContainers,  this.icon, this.stats, this.levelContribution, this.levelContributionRankModifier, this.requiredLevel, this.requiredRank, this.levelWeight, this.researchTemplate, this.statistic, this.modes, this.experienceSources, this.rankModifiers, activation, this.targeting);
        }
    }
}
