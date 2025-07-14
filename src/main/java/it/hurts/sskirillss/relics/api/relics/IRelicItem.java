package it.hurts.sskirillss.relics.api.relics;

import com.google.common.collect.*;
import io.netty.util.internal.UnstableApi;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.api.relics.events.RelicExperienceChangeEvent;
import it.hurts.sskirillss.relics.api.relics.events.RelicLevelChangeEvent;
import it.hurts.sskirillss.relics.api.relics.events.RelicLevelingPointsChangeEvent;
import it.hurts.sskirillss.relics.config.data.RelicConfigData;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.PredicateType;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface IRelicItem extends IRelicTemplateHolder, IRelicDataHolder, IRelicUtilities {
    /**
     * Returns the {@link Item} instance associated with this object.
     *
     * @return the associated {@link Item} instance
     * @throws IllegalStateException if this object does not implement {@link Item}
     */
    default Item getItem() {
        if (this instanceof Item item)
            return item;

        throw new IllegalStateException("Relic interface is not associated with an Item class");
    }

    /**
     * Returns the current experience value of the relic associated with the given entity and item stack.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @return the current experience value
     */
    default double getRelicExperience(LivingEntity entity, ItemStack stack) {
        return getLevelingData(entity, stack).getExperience();
    }

    /**
     * Sets the experience value for the relic associated with the given entity and item stack.
     *
     * @param entity     the entity holding the relic
     * @param stack      the item stack representing the relic
     * @param experience the experience value to set
     */
    default void setRelicExperience(LivingEntity entity, ItemStack stack, double experience) {
        setLevelingData(entity, stack, getLevelingData(entity, stack).toBuilder()
                .experience(Math.clamp(experience, 0D, getTotalRelicExperienceForLevel(entity, stack, getRelicLevel(entity, stack) + 1)))
                .build());
    }

    /**
     * Adds experience to the relic and handles leveling up or down as necessary.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param amount the amount of experience to add (can be negative)
     * @return true if the experience was changed, false otherwise
     */
    default boolean addRelicExperience(LivingEntity entity, ItemStack stack, double amount) {
        var event = NeoForge.EVENT_BUS.post(new RelicExperienceChangeEvent(entity, stack, amount));

        var delta = event.getDelta();

        if (event.isCanceled() || delta == 0)
            return false;

        var xp = getRelicExperience(entity, stack);
        var level = getRelicLevel(entity, stack);
        var oldLevel = level;
        var maxLevel = getLevelingTemplate(entity, stack).getMaxLevel();

        while ((delta > 0 && level < maxLevel) || (delta < 0 && level > 0)) {
            if (delta > 0) {
                var requirement = getTotalRelicExperienceBetweenLevels(entity, stack, level, level + 1) - xp;

                if (delta >= requirement) {
                    delta -= requirement;

                    level++;

                    xp = 0;
                } else {
                    xp += delta;

                    delta = 0;
                }
            } else {
                if (xp + delta >= 0) {
                    xp += delta;

                    delta = 0;
                } else {
                    delta += xp;

                    level--;

                    xp = getTotalRelicExperienceBetweenLevels(entity, stack, level, level + 1);
                }
            }
        }

        if (delta < 0)
            xp = 0;

        setRelicExperience(entity, stack, xp);

        if (level != oldLevel)
            addRelicLevel(entity, stack, level - oldLevel);

        return true;
    }

    /**
     * Returns the current level of the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @return the current level of the relic
     */
    default int getRelicLevel(LivingEntity entity, ItemStack stack) {
        return getLevelingData(entity, stack).getLevel();
    }

    /**
     * Sets the level of the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param level  the new level to set
     */
    default void setRelicLevel(LivingEntity entity, ItemStack stack, int level) {
        setLevelingData(entity, stack, getLevelingData(entity, stack).toBuilder().level(Math.max(0, level)).build());
    }

    /**
     * Adds levels from the relic, respecting level bounds.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param level  the number of levels to add (can be negative)
     * @return true if the level was changed, false otherwise
     */
    default boolean addRelicLevel(LivingEntity entity, ItemStack stack, int level) {
        var currentLevel = getRelicLevel(entity, stack);
        var maxLevel = getLevelingTemplate(entity, stack).getMaxLevel();

        var allowedDelta = level > 0
                ? Math.min(level, maxLevel - currentLevel)
                : (level < 0 ? Math.max(level, -currentLevel) : 0);

        if (allowedDelta == 0)
            return false;

        var event = new RelicLevelChangeEvent(entity, stack, allowedDelta);

        if (NeoForge.EVENT_BUS.post(event).isCanceled() || event.getDelta() == 0)
            return false;

        var delta = event.getDelta();
        var newLevel = currentLevel + delta;

        setRelicLevel(entity, stack, newLevel);

        addRelicLevelingPoints(entity, stack, delta);

        return true;
    }

    /**
     * Returns the number of available leveling points for the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @return the number of leveling points
     */
    default int getRelicLevelingPoints(LivingEntity entity, ItemStack stack) {
        return getLevelingData(entity, stack).getPoints();
    }

    /**
     * Sets the number of leveling points for the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param amount the number of leveling points to set
     */
    default void setRelicLevelingPoints(LivingEntity entity, ItemStack stack, int amount) {
        setLevelingData(entity, stack, getLevelingData(entity, stack).toBuilder().points(Math.max(0, amount)).build());
    }

    /**
     * Adds leveling points to the relic, adjusting abilities if the resulting point total goes negative.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param amount the number of points to add (can be negative)
     * @return true if points or abilities were adjusted, false otherwise
     */
    default boolean addRelicLevelingPoints(LivingEntity entity, ItemStack stack, int amount) {
        var event = new RelicLevelingPointsChangeEvent(entity, stack, amount);

        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return false;

        var delta = event.getDelta();
        var currentPoints = getRelicLevelingPoints(entity, stack);
        var maxLevel = getLevelingTemplate(entity, stack).getMaxLevel();
        var newPoints = currentPoints + delta;

        if (newPoints < 0) {
            var deficit = -newPoints;

            var abilities = getAbilitiesTemplate(entity, stack).getAbilities().keySet().stream()
                    .filter(ability -> getAbilityLevel(entity, stack, ability) > 0)
                    .toList();

            var comparator = Comparator.comparingInt((String ability) -> getAbilityTemplate(entity, stack, ability).getRequiredPoints());

            var sortedAbilities = deficit == 1
                    ? abilities.stream().sorted(comparator).toList()
                    : abilities.stream().sorted(comparator.reversed()).toList();

            for (var ability : sortedAbilities) {
                while (deficit > 0 && getAbilityLevel(entity, stack, ability) > 0) {
                    var cost = getAbilityTemplate(entity, stack, ability).getRequiredPoints();

                    addAbilityLevel(entity, stack, ability, -1);

                    deficit -= cost;
                }

                if (deficit <= 0)
                    break;
            }

            newPoints = deficit < 0 ? -deficit : 0;
        }

        setRelicLevelingPoints(entity, stack, Math.clamp(newPoints, 0, maxLevel));

        return true;
    }

    /**
     * Returns the rank of the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @return the current rank of the relic
     */
    default int getRelicRank(LivingEntity entity, ItemStack stack) {
        return getLevelingData(entity, stack).getRank();
    }

    /**
     * Sets the rank of the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param amount the rank value to set
     */
    default void setRelicRank(LivingEntity entity, ItemStack stack, int amount) {
        setLevelingData(entity, stack, getLevelingData(entity, stack).toBuilder().rank(Math.max(0, amount)).build());
    }

    /**
     * Adds to the current rank of the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param amount the amount of rank to add (can be negative)
     */
    default void addRelicRank(LivingEntity entity, ItemStack stack, int amount) {
        setRelicRank(entity, stack, getRelicRank(entity, stack) + amount);
    }

    @UnstableApi
    @ApiStatus.Experimental
    default String getAbilityMode(LivingEntity entity, ItemStack stack, String ability) {
        var mode = this.getAbilityComponent(entity, stack, ability).getMode();

        return mode.isEmpty() ? this.getAbilityTemplate(entity, stack, ability).getModes().getFirst() : mode;
    }

    @UnstableApi
    @ApiStatus.Experimental
    default void setAbilityMode(LivingEntity entity, ItemStack stack, String ability, String mode) {
        setAbilityComponent(entity, stack, ability, getAbilityComponent(entity, stack, ability).toBuilder().mode(mode).build());
    }

    @UnstableApi
    @ApiStatus.Experimental
    default boolean isAbilityRankModifierUnlocked(LivingEntity entity, ItemStack stack, String ability, String rankModifier) {
        var modifiers = Multimaps.invertFrom(this.getAbilityTemplate(entity, stack, ability).getRankModifiers(), HashMultimap.create());

        return this.getRelicRank(entity, stack) >= Collections.max(modifiers.get(rankModifier));
    }

    @Override
    default RelicTemplate getRelicTemplate(LivingEntity entity, ItemStack stack) {
        var base = IRelicTemplateHolder.super.getRelicTemplate(entity, stack);
        var rank = getRelicRank(entity, stack);

        var multiplier = 0.25D;

        var leveling = base.getLeveling();
        var abilities = base.getAbilities();

        var originalAbilities = abilities.getAbilities();

        var updatedAbilitiesMap = originalAbilities.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            var template = entry.getValue();

                            var updatedMax = IntStream.range(0, rank)
                                    .reduce(template.getMaxLevel(), (lvl, i) -> lvl + (int) Math.ceil(lvl * multiplier));

                            return template.toBuilder()
                                    .maxLevel(updatedMax)
                                    .build();
                        }
                ));

        var abilitiesBuilder = abilities.toBuilder();

        updatedAbilitiesMap.forEach((key, tmpl) -> abilitiesBuilder.ability(
                tmpl.toBuilder()
                        .maxLevel(tmpl.getMaxLevel())
                        .build()
        ));

        var deltaSum = updatedAbilitiesMap.entrySet().stream()
                .mapToInt(e -> e.getValue().getMaxLevel() - originalAbilities.get(e.getKey()).getMaxLevel())
                .sum();

        return base.toBuilder()
                .leveling(leveling.toBuilder()
                        .maxLevel(leveling.getMaxLevel() + deltaSum)
                        .build())
                .abilities(abilitiesBuilder.build())
                .build();
    }

    // TODO: Replace with relative integration
    @Deprecated(forRemoval = true)
    String getConfigRoute();

    // TODO: Probably remove?
    @Nullable
    default RelicConfigData constructDefaultConfigData(@NotNull RelicConfigData config) {
        return config;
    }

    default double getStatValue(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return getStatValueForLevel(entity, stack, ability, stat, getAbilityLevel(entity, stack, ability));
    }

    default int getStatMaxQuality(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return 10;
    }

    default int getAbilityMaxQuality(LivingEntity entity, ItemStack stack, String ability) {
        return 10;
    }

    default int calculateAbilityQuality(LivingEntity entity, ItemStack stack, String ability) {
        var stats = this.getAbilityTemplate(entity, stack, ability).getStats();

        if (stats.isEmpty())
            return this.getAbilityMaxQuality(entity, stack, ability);

        var avg = stats.keySet().stream()
                .mapToInt(stat -> this.getOrCalculateStatQuality(entity, stack, ability, stat))
                .average()
                .orElse(0);

        var min = 0;
        var max = this.getAbilityMaxQuality(entity, stack, ability);

        if (avg == min)
            return min;

        if (avg == max)
            return max;

        return (int) Mth.clamp(Math.floor(avg), min + 1, max - 1);
    }

    default int getRelicMaxQuality(LivingEntity entity, ItemStack stack) {
        return 10;
    }

    default int calculateRelicQuality(LivingEntity entity, ItemStack stack) {
        var abilities = this.getAbilitiesTemplate(entity, stack).getAbilities();

        if (abilities.isEmpty())
            return 0;

        var filtered = abilities.keySet().stream()
                .filter(abilityTemplate -> this.canBeUpgraded(entity, stack, abilityTemplate) && isAbilityUnlocked(entity, stack, abilityTemplate))
                .mapToInt(abilityTemplate -> this.calculateAbilityQuality(entity, stack, abilityTemplate))
                .toArray();

        if (filtered.length == 0)
            return 0;

        var avg = Arrays.stream(filtered).average().orElse(0);

        var min = 0;
        var max = this.getRelicMaxQuality(entity, stack);

        if (avg == min)
            return min;

        if (avg == max)
            return max;

        return (int) Mth.clamp(Math.floor(avg), min + 1, max - 1);
    }

    default double calculateRelicProgress(LivingEntity entity, ItemStack stack) {
        var unspentPoints = this.getRelicLevelingPoints(entity, stack);
        var template = this.getLevelingTemplate(entity, stack);
        var rank = this.getRelicRank(entity, stack);
        var maxRank = template.getMaxRank();
        var level = this.getRelicLevel(entity, stack);
        var maxLevel = template.getMaxLevel();
        var quality = this.calculateRelicQuality(entity, stack);
        var maxQuality = this.getRelicMaxQuality(entity, stack);

        var adjustedUnits = Math.max(0.0, Math.min(level - (unspentPoints * 0.5), maxLevel));

        var levelFraction = (maxLevel > 0) ? (adjustedUnits / maxLevel) : 0.0;
        var totalSegments = Math.max(1, maxRank + 1);
        var completedSegments = Math.max(0, Math.min(rank, maxRank));

        var baseProgress = (completedSegments + levelFraction) / totalSegments;
        var qualityRatio = (maxQuality > 0) ? (quality / (double) maxQuality) : 0.0;
        var maxQualityWeight = 0.2;
        var qualityContribution = qualityRatio * maxQualityWeight * (1 - baseProgress);

        var progress = baseProgress + qualityContribution;
        return Math.min(1.0, Math.max(0.0, progress));
    }

    default void castActiveAbility(Player player, ItemStack stack, String ability, CastType type, CastStage stage) {

    }

    default void tickActiveAbilitySelection(ItemStack stack, Player player, String ability) {

    }

    // TODO: Probably remove?
    @Nullable
    @ApiStatus.Internal
    default RelicAttributeModifier getRelicAttributeModifiers(LivingEntity entity, ItemStack stack) {
        return RelicAttributeModifier.builder().build();
    }

    // TODO: Probably remove?
    @Nullable
    @ApiStatus.Internal
    default RelicSlotModifier getSlotModifiers(LivingEntity entity, ItemStack stack) {
        return RelicSlotModifier.builder().build();
    }

    @UnstableApi
    default boolean isLevelingSourceUnlocked(LivingEntity entity, ItemStack stack, String source) {
        var data = getLevelingSourceTemplate(entity, stack, source);
        var ability = data.getRequiredAbility();

        return isLevelingSourceEnabled(entity, stack, source) && getRelicLevel(entity, stack) >= data.getRequiredLevel() && (ability.isEmpty() || isAbilityUnlocked(entity, stack, ability));
    }

    @UnstableApi
    default boolean isLevelingSourceEnabled(LivingEntity entity, ItemStack stack, String source) {
        var data = getLevelingSourceTemplate(entity, stack, source);
        var ability = data.getRequiredAbility();

        return data.getRequiredAbility().isEmpty() || isAbilityEnabled(entity, stack, ability);
    }

    @UnstableApi
    default int getLevelingSourceValue(LivingEntity entity, ItemStack stack, String source) {
        var data = getLevelingSourceTemplate(entity, stack, source);

        // TODO: Use component value instead
        return data.getInitialValue();
    }

    @UnstableApi
    default int getLevelingSourceLevel(ItemStack stack, String source) {
        // TODO: Use component value instead
        return 1;
    }

    default LootTemplate getLootTemplate(LivingEntity entity, ItemStack stack) {
        return getRelicTemplate(entity, stack).getLoot();
    }

    default StyleTemplate getStyleTemplate(LivingEntity entity, ItemStack stack) {
        return getRelicTemplate(entity, stack).getStyle();
    }

    default void spreadRelicExperience(@Nullable LivingEntity entity, ItemStack stack, int experience) {
        spreadRelicExperience(entity, stack, experience, 0.25D);
    }

    default void spreadRelicExperience(@Nullable LivingEntity entity, ItemStack stack, int experience, double percentage) {
        var isMaxLevel = isRelicMaxLevel(entity, stack);

        var toSpread = isMaxLevel ? 0 : experience * percentage;

        if (!isMaxLevel)
            addRelicExperience(entity, stack, experience);

        if (toSpread <= 0 || entity == null)
            return;

        var relics = RelicsRegistries.RELIC_CONTAINER_REGISTRY.entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(source -> source.gatherRelics().apply(entity).stream())
                .filter(entry -> entry.getItem() instanceof IRelicItem relic && !relic.isRelicMaxLevel(entity, entry) && !stack.equals(entry))
                .toList();

        if (relics.isEmpty())
            return;

        var relicStack = relics.get(entity.level().getRandom().nextInt(relics.size()));

        if (relicStack.getItem() instanceof IRelicItem relic)
            relic.addRelicExperience(entity, relicStack, toSpread);
    }

    default CastData getAbilityCastData(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityTemplate(entity, stack, ability).getCastData();
    }

    default Map<String, Pair<PredicateType, BiFunction<Player, ItemStack, Boolean>>> getAbilityPredicates(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityCastData(entity, stack, ability).getPredicates();
    }

    default Map<String, BiFunction<Player, ItemStack, Boolean>> getAbilityPredicates(LivingEntity entity, ItemStack stack, String ability, PredicateType type) {
        return getAbilityPredicates(entity, stack, ability).entrySet().stream().filter(entry -> entry.getValue().getKey() == type).collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue()));
    }

    default boolean testAbilityPredicate(Player player, ItemStack stack, String ability, String predicate) {
        return getAbilityPredicates(player, stack, ability).get(predicate).getValue().apply(player, stack);
    }

    default boolean testAbilityPredicates(Player player, ItemStack stack, String ability, PredicateType type) {
        for (Map.Entry<String, BiFunction<Player, ItemStack, Boolean>> entry : getAbilityPredicates(player, stack, ability, type).entrySet())
            if (!testAbilityPredicate(player, stack, ability, entry.getKey()))
                return false;

        return true;
    }

    default void setResearchComponent(LivingEntity entity, ItemStack stack, String ability, ResearchComponent component) {
        setAbilityComponent(entity, stack, ability, getAbilityComponent(entity, stack, ability).toBuilder()
                .research(component)
                .build());
    }

    default Multimap<Integer, Integer> getResearchLinks(LivingEntity entity, ItemStack stack, String ability) {
        return getResearchComponent(entity, stack, ability).getLinks().entrySet().stream()
                .collect(MultimapBuilder.hashKeys().arrayListValues()::build, (multimap, entry) -> multimap.putAll(Integer.parseInt(entry.getKey()), entry.getValue()), Multimap::putAll);
    }

    default void addResearchLink(LivingEntity entity, ItemStack stack, String ability, int from, int to) {
        var links = getResearchLinks(entity, stack, ability);

        links.put(from, to);

        setResearchComponent(entity, stack, ability, getResearchComponent(entity, stack, ability).toBuilder()
                .links(links.asMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                entry -> new ArrayList<>(entry.getValue())
                        )))
                .build());
    }

    default void removeResearchLink(LivingEntity entity, ItemStack stack, String ability, int from, int to) {
        var links = getResearchLinks(entity, stack, ability);

        links.remove(from, to);

        setResearchComponent(entity, stack, ability, getResearchComponent(entity, stack, ability).toBuilder()
                .links(links.asMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                entry -> new ArrayList<>(entry.getValue())
                        )))
                .build());
    }

    default boolean isAbilityResearched(LivingEntity entity, ItemStack stack, String ability) {
        return getResearchTemplate(entity, stack, ability).getStars().isEmpty() || getResearchComponent(entity, stack, ability).isResearched();
    }

    default void setAbilityResearched(LivingEntity entity, ItemStack stack, String ability, boolean researched) {
        setResearchComponent(entity, stack, ability, getResearchComponent(entity, stack, ability).toBuilder()
                .researched(researched)
                .build());
    }

    default Multimap<Integer, Integer> getCorrectResearchLinks(LivingEntity entity, ItemStack stack, String ability) {
        Multimap<Integer, Integer> schema = getResearchTemplate(entity, stack, ability).getLinks();
        Multimap<Integer, Integer> links = getResearchLinks(entity, stack, ability);

        if (schema.isEmpty())
            return LinkedHashMultimap.create();

        Set<Pair<Integer, Integer>> bidirectionalSchema = schema.entries().stream()
                .flatMap(entry -> Stream.of(Pair.of(entry.getKey(), entry.getValue()), Pair.of(entry.getValue(), entry.getKey())))
                .collect(Collectors.toSet());

        return links.entries().stream()
                .filter(entry -> bidirectionalSchema.contains(Pair.of(entry.getKey(), entry.getValue()))
                        || bidirectionalSchema.contains(Pair.of(entry.getValue(), entry.getKey())))
                .collect(LinkedHashMultimap::create, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Multimap::putAll);
    }

    default Multimap<Integer, Integer> getIncorrectResearchLinks(LivingEntity entity, ItemStack stack, String ability) {
        Multimap<Integer, Integer> schema = getResearchTemplate(entity, stack, ability).getLinks();
        Multimap<Integer, Integer> links = getResearchLinks(entity, stack, ability);

        if (schema.isEmpty())
            return LinkedHashMultimap.create();

        Set<Pair<Integer, Integer>> bidirectionalSchema = schema.entries().stream()
                .flatMap(entry -> Stream.of(Pair.of(entry.getKey(), entry.getValue()), Pair.of(entry.getValue(), entry.getKey())))
                .collect(Collectors.toSet());

        return links.entries().stream()
                .filter(entry -> !bidirectionalSchema.contains(Pair.of(entry.getKey(), entry.getValue()))
                        && !bidirectionalSchema.contains(Pair.of(entry.getValue(), entry.getKey())))
                .collect(LinkedHashMultimap::create, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Multimap::putAll);
    }

    default double testAbilityResearchPercentage(LivingEntity entity, ItemStack stack, String ability) {
        Multimap<Integer, Integer> schema = getResearchTemplate(entity, stack, ability).getLinks();
        Multimap<Integer, Integer> links = getResearchLinks(entity, stack, ability);

        if (schema.isEmpty())
            return 0D;

        Set<Pair<Integer, Integer>> bidirectionalSchema = schema.entries().stream()
                .flatMap(entry -> Stream.of(Pair.of(entry.getKey(), entry.getValue()), Pair.of(entry.getValue(), entry.getKey())))
                .collect(Collectors.toSet());

        long matchingLinks = links.entries().stream()
                .filter(entry -> bidirectionalSchema.contains(Pair.of(entry.getKey(), entry.getValue()))
                        || bidirectionalSchema.contains(Pair.of(entry.getValue(), entry.getKey())))
                .count();

        return (double) matchingLinks / schema.size();
    }

    default boolean testAbilityResearch(LivingEntity entity, ItemStack stack, String ability) {
        return testAbilityResearchPercentage(entity, stack, ability) >= 1D;
    }

    default int getResearchHintPlayerExperienceCost(LivingEntity entity, ItemStack stack, String ability) {
        return 50;
    }

    default int getAbilityLevel(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityComponent(entity, stack, ability).getPoints();
    }

    default void setAbilityLevel(LivingEntity entity, ItemStack stack, String ability, int points) {
        setAbilityComponent(entity, stack, ability, getAbilityComponent(entity, stack, ability).toBuilder()
                .points(points)
                .build());
    }

    default void addAbilityLevel(LivingEntity entity, ItemStack stack, String ability, int points) {
        setAbilityLevel(entity, stack, ability, getAbilityLevel(entity, stack, ability) + points);
    }

    default AbilityComponent randomizeAbilityStats(LivingEntity entity, ItemStack stack, String ability) {
        Map<String, StatTemplate> stats = getAbilityTemplate(entity, stack, ability).getStats();

        Random random = new Random();

        double targetQuality;

        do {
            int maxQuality = getAbilityMaxQuality(entity, stack, ability);
            int maxLuck = 10;

            // Random value in the [-1, 1] range
            double randomValue = (random.nextDouble() * 2D) - 1D;

            // Luck effect modifier. Lower value = lower chance to get 5 stars
            double modifier = 5D;

            // Bias based on luck (ranging from -0.5 to 0.5), multiplied by the modifier
            double bias = ((5 - (maxLuck / 2D)) / maxLuck) * modifier;

            // Apply the bias to randomValue and limit the result within the range [-1, 1]
            double biasedValue = Math.tanh(randomValue + bias);

            // Convert the biased result to the range [0, maxQuality]
            double weightedRandom = Math.floor((biasedValue + 1D) / 2D * (maxQuality + 1D));

            // Clamping the value to avoid overflow
            targetQuality = Mth.clamp(weightedRandom, 0, maxQuality);
        } while (targetQuality == calculateAbilityQuality(entity, stack, ability));

        double sumQuality = 0;

        Map<String, Double> generatedQualities = new HashMap<>();

        for (String stat : stats.keySet()) {
            double randomQuality = MathUtils.randomBetween(random, 0, getStatMaxQuality(entity, stack, ability, stat));

            generatedQualities.put(stat, randomQuality);

            sumQuality += randomQuality;
        }

        double currentAverageQuality = sumQuality / stats.size();

        while (Math.abs(currentAverageQuality - targetQuality) > 0.01) {
            if (currentAverageQuality < targetQuality) {
                String minStat = generatedQualities.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();

                double increment = Math.min((targetQuality - currentAverageQuality) * stats.size(), getStatMaxQuality(entity, stack, ability, minStat) - generatedQualities.get(minStat));

                generatedQualities.put(minStat, generatedQualities.get(minStat) + increment);
            } else if (currentAverageQuality > targetQuality) {
                String maxStat = generatedQualities.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();

                double decrement = Math.min((currentAverageQuality - targetQuality) * stats.size(), generatedQualities.get(maxStat));

                generatedQualities.put(maxStat, generatedQualities.get(maxStat) - decrement);
            }

            sumQuality = generatedQualities.values().stream().mapToDouble(Double::doubleValue).sum();

            currentAverageQuality = sumQuality / stats.size();
        }

        for (Map.Entry<String, Double> entry : generatedQualities.entrySet())
            randomizeStat(entity, stack, ability, entry.getKey(), (int) Math.round(entry.getValue()));

        return getAbilityComponent(entity, stack, ability);
    }

    default StatComponent randomizeStat(LivingEntity entity, ItemStack stack, String ability, String stat, int quality) {
        StatTemplate entry = getStatTemplate(entity, stack, ability, stat);

        double minValue = entry.getInitialValue().getKey();
        double maxValue = entry.getInitialValue().getValue();

        double diff = maxValue - minValue;

        double result = minValue + (diff * ((double) quality / getStatMaxQuality(entity, stack, ability, stat)));

        setStatOverrideValue(entity, stack, ability, stat, result);

        return getStatComponent(entity, stack, ability, stat);
    }

    default StatComponent randomizeStat(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return randomizeStat(entity, stack, ability, stat, new Random().nextInt(getStatMaxQuality(entity, stack, ability, stat) + 1));
    }

    default boolean isEnoughLevel(LivingEntity entity, ItemStack stack, String ability) {
        return getRelicLevel(entity, stack) >= getAbilityTemplate(entity, stack, ability).getRequiredLevel();
    }

    @UnstableApi
    default boolean isAbilityEnabled(LivingEntity entity, ItemStack stack, String ability) {
        return true;
    }

    default boolean isAbilityUnlocked(LivingEntity entity, ItemStack stack, String ability) {
        return isAbilityEnabled(entity, stack, ability) && isEnoughLevel(entity, stack, ability) && isLockUnlocked(entity, stack, ability) && isAbilityResearched(entity, stack, ability);
    }

    default boolean hasUnlockedUpgradeableAbility(LivingEntity entity, ItemStack stack) {
        return getAbilitiesTemplate(entity, stack).getAbilities().keySet().stream().anyMatch(ability -> canBeUpgraded(entity, stack, ability) && isAbilityUnlocked(entity, stack, ability));
    }

    default boolean hasUnlockedAbility(LivingEntity entity, ItemStack stack) {
        return getAbilitiesTemplate(entity, stack).getAbilities().keySet().stream().anyMatch(ability -> isAbilityUnlocked(entity, stack, ability));
    }

    default boolean canPlayerUseAbility(LivingEntity entity, ItemStack stack, String ability) {
        return isAbilityUnlocked(entity, stack, ability) && (!(entity instanceof Player player) || testAbilityPredicates(player, stack, ability, PredicateType.CAST)) && getAbilityCooldown(entity, stack, ability) <= 0;
    }

    default boolean canPlayerSeeAbility(Player player, ItemStack stack, String ability) {
        return testAbilityPredicates(player, stack, ability, PredicateType.VISIBILITY);
    }

    default boolean mayUnlock(LivingEntity entity, ItemStack stack, String ability) {
        return isEnoughLevel(entity, stack, ability) && !isLockUnlocked(entity, stack, ability);
    }

    default boolean mayResearch(LivingEntity entity, ItemStack stack, String ability) {
        return isEnoughLevel(entity, stack, ability) && isLockUnlocked(entity, stack, ability) && !isAbilityResearched(entity, stack, ability);
    }

    default int getUpgradePlayerExperienceCost(LivingEntity entity, ItemStack stack, String ability) {
        return (getAbilityLevel(entity, stack, ability) + 1) * 50;
    }

    default boolean canBeUpgraded(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityTemplate(entity, stack, ability).getMaxLevel() > 0 && !getAbilityTemplate(entity, stack, ability).getStats().isEmpty();
    }

    default boolean mayUpgrade(LivingEntity entity, ItemStack stack, String ability) {
        AbilityTemplate entry = getAbilityTemplate(entity, stack, ability);

        return canBeUpgraded(entity, stack, ability) && !isAbilityMaxLevel(entity, stack, ability) && getRelicLevelingPoints(entity, stack) >= entry.getRequiredPoints() && isAbilityUnlocked(entity, stack, ability);
    }

    default boolean mayPlayerUpgrade(Player player, ItemStack stack, String ability) {
        return mayUpgrade(player, stack, ability) && EntityUtils.getPlayerTotalExperience(player) >= getUpgradePlayerExperienceCost(player, stack, ability);
    }

    default boolean upgrade(Player player, ItemStack stack, String ability) {
        if (!mayPlayerUpgrade(player, stack, ability))
            return false;

        player.giveExperiencePoints(-getUpgradePlayerExperienceCost(player, stack, ability));

        setAbilityLevel(player, stack, ability, getAbilityLevel(player, stack, ability) + 1);
        addRelicLevelingPoints(player, stack, -getAbilityTemplate(player, stack, ability).getRequiredPoints());

        return true;
    }

    default int getRerollPlayerExperienceCost(LivingEntity entity, ItemStack stack, String ability) {
        return 50;
    }

    default boolean mayReroll(LivingEntity entity, ItemStack stack, String ability) {
        return !getAbilityTemplate(entity, stack, ability).getStats().isEmpty() && isAbilityUnlocked(entity, stack, ability);
    }

    default boolean mayPlayerReroll(Player player, ItemStack stack, String ability) {
        return mayReroll(player, stack, ability) && EntityUtils.getPlayerTotalExperience(player) >= getRerollPlayerExperienceCost(player, stack, ability);
    }

    default boolean reroll(Player player, ItemStack stack, String ability) {
        if (!mayPlayerReroll(player, stack, ability))
            return false;

        player.giveExperiencePoints(-getRerollPlayerExperienceCost(player, stack, ability));

        randomizeAbilityStats(player, stack, ability);

        return true;
    }

    default int getResetPlayerExperienceCost(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityLevel(entity, stack, ability) * 250;
    }

    default boolean mayReset(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityLevel(entity, stack, ability) > 0 && isAbilityUnlocked(entity, stack, ability);
    }

    default boolean mayPlayerReset(Player player, ItemStack stack, String ability) {
        return !getAbilityTemplate(player, stack, ability).getStats().isEmpty() && mayReset(player, stack, ability) && EntityUtils.getPlayerTotalExperience(player) >= getResetPlayerExperienceCost(player, stack, ability);
    }

    @ApiStatus.Obsolete
    default boolean mayPlayerRankup(Player player, ItemStack stack) {
        var levelingTemplate = this.getLevelingTemplate(player, stack);

        return this.getRelicLevel(player, stack) == levelingTemplate.getMaxLevel() && this.getRelicRank(player, stack) < levelingTemplate.getMaxRank();
    }

    @ApiStatus.Obsolete
    default boolean rankup(Player player, ItemStack stack) {
        if (!mayPlayerRankup(player, stack))
            return false;

        addRelicRank(player, stack, 1);
        setRelicLevel(player, stack, 0);
        setRelicLevelingPoints(player, stack, 0);

        for (var ability : this.getAbilitiesTemplate(player, stack).getAbilities().values())
            this.setAbilityLevel(player, stack, ability.getId(), 0);

        return true;
    }

    default boolean reset(Player player, ItemStack stack, String ability) {
        if (!mayPlayerReset(player, stack, ability))
            return false;

        player.giveExperiencePoints(-getResetPlayerExperienceCost(player, stack, ability));

        addRelicLevelingPoints(player, stack, getAbilityLevel(player, stack, ability) * getAbilityTemplate(player, stack, ability).getRequiredPoints());
        setAbilityLevel(player, stack, ability, 0);

        return true;
    }

    default int getAbilityCooldownCap(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityExtenderComponent(entity, stack, ability).getCooldownCap();
    }

    default void setAbilityCooldownCap(LivingEntity entity, ItemStack stack, String ability, int amount) {
        setAbilityExtenderComponent(entity, stack, ability, getAbilityExtenderComponent(entity, stack, ability).toBuilder()
                .cooldownCap(amount)
                .build());
    }

    default int getAbilityCooldown(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityExtenderComponent(entity, stack, ability).getCooldown();
    }

    default void setAbilityCooldown(LivingEntity entity, ItemStack stack, String ability, int amount) {
        setAbilityExtenderComponent(entity, stack, ability, getAbilityExtenderComponent(entity, stack, ability).toBuilder()
                .cooldownCap(amount)
                .cooldown(amount)
                .build());
    }

    default void addAbilityCooldown(LivingEntity entity, ItemStack stack, String ability, int amount) {
        setAbilityExtenderComponent(entity, stack, ability, getAbilityExtenderComponent(entity, stack, ability).toBuilder()
                .cooldown(getAbilityCooldown(entity, stack, ability) + amount)
                .build());
    }

    default void setAbilityTicking(LivingEntity entity, ItemStack stack, String ability, boolean ticking) {
        setAbilityExtenderComponent(entity, stack, ability, getAbilityExtenderComponent(entity, stack, ability).toBuilder()
                .ticking(ticking)
                .build());
    }

    default boolean isAbilityTicking(LivingEntity entity, ItemStack stack, String ability) {
        return isAbilityUnlocked(entity, stack, ability) && getAbilityExtenderComponent(entity, stack, ability).isTicking();
    }

    default boolean isAbilityOnCooldown(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityCooldown(entity, stack, ability) > 0;
    }

    default boolean isAbilityUpgradeEnabled(LivingEntity entity, ItemStack stack, String ability) {
        return isAbilityUnlocked(entity, stack, ability) && !getAbilityTemplate(entity, stack, ability).getStats().isEmpty();
    }

    default boolean isAbilityRerollEnabled(LivingEntity entity, ItemStack stack, String ability) {
        return isAbilityUnlocked(entity, stack, ability) && !getAbilityTemplate(entity, stack, ability).getStats().isEmpty();
    }

    default boolean isAbilityResetEnabled(LivingEntity entity, ItemStack stack, String ability) {
        return isAbilityUnlocked(entity, stack, ability) && !getAbilityTemplate(entity, stack, ability).getStats().isEmpty();
    }
}