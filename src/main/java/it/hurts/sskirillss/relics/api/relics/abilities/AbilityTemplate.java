package it.hurts.sskirillss.relics.api.relics.abilities;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Function3;
import io.netty.util.internal.UnstableApi;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.StatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.config.data.AbilityConfigData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

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
    private final Map<String, StatTemplate> stats;
    private final int initialMaxLevel;
    private final double maxLevelRankModifier;
    private final int requiredLevel;
    private final int requiredPoints;
    private final CastData castData;
    private final ResearchTemplate researchTemplate;
    private final StatisticTemplate statistic;
    private final List<String> modes;
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

            var modes = relic.getAbilityTemplate(player, stack, ability).getModes();

            return ability + (modes.isEmpty() ? "" : "_" + relic.getAbilityMode(player, stack, ability));
        };
        private Map<String, StatTemplate> stats = new LinkedHashMap<>();
        private int initialMaxLevel = 10;
        private double maxLevelRankModifier = 0.25D;
        private int requiredLevel = 0;
        private int requiredPoints = 1;
        private CastData castData = CastData.builder().build();
        private ResearchTemplate researchTemplate = ResearchTemplate.builder().build();
        private StatisticTemplate statistic = StatisticTemplate.builder().build();
        private List<String> modes = new ArrayList<>();
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
            this.castData = base.getCastData();
            this.researchTemplate = base.getResearchTemplate();
            this.modes = base.getModes();
            this.rankModifiers = base.getRankModifiers();
        }

        public AbilityTemplateBuilder icon(Function3<Player, ItemStack, String, String> icon) {
            this.icon = icon;

            return this;
        }

        public AbilityTemplateBuilder stats(Map<String, StatTemplate> stats) {
            this.stats = stats;

            return this;
        }

        public AbilityTemplateBuilder stat(StatTemplate stat) {
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

        public AbilityTemplateBuilder castData(CastData castData) {
            this.castData = castData;

            return this;
        }

        public AbilityTemplateBuilder researchTemplate(ResearchTemplate researchTemplate) {
            this.researchTemplate = researchTemplate;

            return this;
        }

        public AbilityTemplateBuilder statistic(StatisticTemplate statistic) {
            this.statistic = statistic;

            return this;
        }

        @UnstableApi
        @ApiStatus.Experimental
        public AbilityTemplateBuilder modes(String... mode) {
            this.modes.addAll(Lists.newArrayList(mode));

            return this;
        }

        public AbilityTemplateBuilder rankModifier(int rank, String modifier) {
            this.rankModifiers.put(rank, modifier);

            return this;
        }

        public AbilityTemplate build() {
            return new AbilityTemplate(this.id, this.icon, this.stats, this.initialMaxLevel, this.maxLevelRankModifier, this.requiredLevel, this.requiredPoints, this.castData, this.researchTemplate, this.statistic, this.modes, this.rankModifiers);
        }
    }
}