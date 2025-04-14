package it.hurts.sskirillss.relics.api.relics;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.netty.util.internal.UnstableApi;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.api.relics.events.RelicExperienceChangeEvent;
import it.hurts.sskirillss.relics.api.relics.events.RelicLevelChangeEvent;
import it.hurts.sskirillss.relics.api.relics.events.RelicLevelingPointsChangeEvent;
import it.hurts.sskirillss.relics.components.AbilityComponent;
import it.hurts.sskirillss.relics.components.ResearchComponent;
import it.hurts.sskirillss.relics.components.StatComponent;
import it.hurts.sskirillss.relics.config.data.RelicConfigData;
import it.hurts.sskirillss.relics.init.RegistryRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.CastData;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.PredicateType;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
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
import java.util.function.Function;
import java.util.stream.Collectors;
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
        return getLevelingData(stack).getExperience();
    }

    /**
     * Sets the experience value for the relic associated with the given entity and item stack.
     *
     * @param entity     the entity holding the relic
     * @param stack      the item stack representing the relic
     * @param experience the experience value to set
     */
    default void setRelicExperience(LivingEntity entity, ItemStack stack, double experience) {
        setLevelingData(stack, getLevelingData(stack).toBuilder()
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
        return getLevelingData(stack).getLevel();
    }

    /**
     * Sets the level of the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param level  the new level to set
     */
    default void setRelicLevel(LivingEntity entity, ItemStack stack, int level) {
        setLevelingData(stack, getLevelingData(stack).toBuilder().level(Math.max(0, level)).build());
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
        return getLevelingData(stack).getPoints();
    }

    /**
     * Sets the number of leveling points for the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param amount the number of leveling points to set
     */
    default void setRelicLevelingPoints(LivingEntity entity, ItemStack stack, int amount) {
        setLevelingData(stack, getLevelingData(stack).toBuilder().points(Math.max(0, amount)).build());
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
        return getLevelingData(stack).getRank();
    }

    /**
     * Sets the rank of the relic.
     *
     * @param entity the entity holding the relic
     * @param stack  the item stack representing the relic
     * @param amount the rank value to set
     */
    default void setRelicRank(LivingEntity entity, ItemStack stack, int amount) {
        setLevelingData(stack, getLevelingData(stack).toBuilder().rank(Math.max(0, amount)).build());
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

    // TODO: Huh?
    @Override
    @ApiStatus.Internal
    default LevelingTemplate getLevelingTemplate(LivingEntity entity, ItemStack stack) {
        var template = IRelicTemplateHolder.super.getLevelingTemplate(entity, stack);

        return template.toBuilder()
                .maxLevel(template.getMaxLevel() + getRelicLevel(entity, stack))
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

    default void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {

    }

    default void tickActiveAbilitySelection(ItemStack stack, Player player, String ability) {

    }

    // TODO: Probably remove?
    @Nullable
    @ApiStatus.Internal
    default RelicAttributeModifier getRelicAttributeModifiers(ItemStack stack) {
        return RelicAttributeModifier.builder().build();
    }

    // TODO: Probably remove?
    @Nullable
    @ApiStatus.Internal
    default RelicSlotModifier getSlotModifiers(ItemStack stack) {
        return RelicSlotModifier.builder().build();
    }

    @UnstableApi
    default boolean isLevelingSourceUnlocked(LivingEntity entity, ItemStack stack, String source) {
        var data = getLevelingSourceTemplate(entity, stack, source);
        var ability = data.getRequiredAbility();

        return isLevelingSourceEnabled(entity, stack, source) && getRelicLevel(entity, stack) >= data.getRequiredLevel() && (ability.isEmpty() || isAbilityUnlocked(stack, ability));
    }

    @UnstableApi
    default boolean isLevelingSourceEnabled(LivingEntity entity, ItemStack stack, String source) {
        var data = getLevelingSourceTemplate(entity, stack, source);
        var ability = data.getRequiredAbility();

        return data.getRequiredAbility().isEmpty() || isAbilityEnabled(stack, ability);
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

    default LootTemplate getLootData(LivingEntity entity, ItemStack stack) {
        return getRelicTemplate(entity, stack).getLoot();
    }

    default StyleTemplate getStyleData(LivingEntity entity, ItemStack stack) {
        return getRelicTemplate(entity, stack).getStyle();
    }

    default int getMaxQuality(LivingEntity entity, ItemStack stack) {
        return 10;
    }

    default int getStatQuality(LivingEntity entity, ItemStack stack, String ability, String stat) {
        StatTemplate statData = getStatTemplate(entity, stack, ability, stat);

        if (statData == null)
            return 0;

        Function<Double, ? extends Number> format = statData.getFormatValue();

        double initial = format.apply(getStatInitialValue(stack, ability, stat)).doubleValue();

        double min = format.apply(statData.getInitialValue().getKey()).doubleValue();
        double max = format.apply(statData.getInitialValue().getValue()).doubleValue();

        if (min == max)
            return getMaxQuality(entity, stack);

        if (initial == min)
            return 0;

        if (initial == max)
            return getMaxQuality(entity, stack);

        return Mth.clamp((int) Math.round((initial - min) / ((max - min) / getMaxQuality(entity, stack))), 1, getMaxQuality(entity, stack) - 1);
    }

    default double getStatValueByQuality(LivingEntity entity, ItemStack stack, String ability, String stat, int quality) {
        StatTemplate statData = getStatTemplate(entity, stack, ability, stat);

        if (statData == null)
            return 0;

        double min = statData.getInitialValue().getKey();
        double max = statData.getInitialValue().getValue();

        if (min == max)
            return max;

        return MathUtils.round(min + (((max - min) / getMaxQuality(entity, stack)) * quality), 5);
    }

    default int getAbilityQuality(LivingEntity entity, ItemStack stack, String ability) {
        Map<String, StatTemplate> stats = getAbilityTemplate(entity, stack, ability).getStats();

        if (stats.isEmpty())
            return getMaxQuality(entity, stack);

        double sum = 0;

        for (String stat : stats.keySet())
            sum += getStatQuality(entity, stack, ability, stat);

        sum = (int) Math.floor(sum / stats.size());

        int min = 0;
        int max = getMaxQuality(entity, stack);

        if (sum == min)
            return min;

        if (sum == max)
            return max;

        return (int) Mth.clamp(sum, min + 1, max - 1);
    }

    default int getRelicQuality(LivingEntity entity, ItemStack stack) {
        Map<String, AbilityTemplate> abilities = getAbilitiesTemplate(entity, stack).getAbilities();

        if (abilities.isEmpty())
            return 0;

        int size = abilities.size();
        double sum = 0;

        for (Map.Entry<String, AbilityTemplate> entry : abilities.entrySet()) {
            var ability = entry.getKey();

            if (!canBeUpgraded(stack, ability) || !isAbilityUnlocked(stack, ability)) {
                --size;

                continue;
            }

            sum += getAbilityQuality(entity, stack, entry.getKey());
        }

        sum = (int) Math.floor(sum / size);

        int min = 0;
        int max = getMaxQuality(entity, stack);

        if (sum == min)
            return min;

        if (sum == max)
            return max;

        return (int) Mth.clamp(sum, min + 1, max - 1);
    }

    default int getMaxLuck(LivingEntity entity, ItemStack stack) {
        return 100;
    }

    default double getLuckModifier(LivingEntity entity, ItemStack stack) {
        return 1.15D;
    }

    default int getRelicLuck(LivingEntity entity, ItemStack stack) {
        return getLevelingData(stack).luck();
    }

    default void setRelicLuck(LivingEntity entity, ItemStack stack, int amount) {
        setLevelingData(stack, getLevelingData(stack).toBuilder().luck(Mth.clamp(amount, 0, getMaxLuck(entity, stack))).build());
    }

    default void addRelicLuck(LivingEntity entity, ItemStack stack, int amount) {
        setRelicLuck(entity, stack, getRelicLuck(entity, stack) + amount);
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

        var relics = RegistryRegistry.RELIC_CONTAINER_REGISTRY.entrySet().stream()
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

    @Deprecated(forRemoval = true)
    default boolean isSomethingWrongWithLevelingPoints(LivingEntity entity, ItemStack stack) {
        int current = getRelicLevelingPoints(entity, stack);

        for (var data : getAbilitiesTemplate(entity, stack).getAbilities().values())
            current += getAbilityComponent(stack, data.getId()).points() * data.getRequiredPoints();

        return current != getRelicLevel(entity, stack);
    }

    default boolean isRelicMaxLevel(LivingEntity entity, ItemStack stack) {
        return getRelicLevel(entity, stack) >= getLevelingTemplate(entity, stack).getMaxLevel();
    }

    default boolean isRelicMaxQuality(LivingEntity entity, ItemStack stack) {
        return getRelicQuality(entity, stack) >= getMaxQuality(entity, stack);
    }

    default boolean isRelicFlawless(LivingEntity entity, ItemStack stack) {
        return isRelicMaxLevel(entity, stack) && getAbilitiesTemplate(entity, stack).getAbilities().keySet().stream().filter(ability -> isAbilityEnabled(stack, ability)).allMatch(ability -> isAbilityFlawless(entity, stack, ability));
    }

    default boolean isAbilityMaxLevel(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityLevel(stack, ability) >= getAbilityMaxLevel(stack, ability);
    }

    default boolean isAbilityMaxQuality(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityQuality(entity, stack, ability) >= getMaxQuality(entity, stack);
    }

    default boolean isAbilityFlawless(LivingEntity entity, ItemStack stack, String ability) {
        return isAbilityUnlocked(stack, ability) && isAbilityMaxQuality(entity, stack, ability);
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

    default void setResearchComponent(ItemStack stack, String ability, ResearchComponent component) {
        setAbilityComponent(stack, ability, getAbilityComponent(stack, ability).toBuilder()
                .research(component)
                .build());
    }

    default Multimap<Integer, Integer> getResearchLinks(ItemStack stack, String ability) {
        return getResearchComponent(stack, ability).links().entrySet().stream()
                .collect(MultimapBuilder.hashKeys().arrayListValues()::build, (multimap, entry) -> multimap.putAll(Integer.parseInt(entry.getKey()), entry.getValue()), Multimap::putAll);
    }

    default void addResearchLink(ItemStack stack, String ability, int from, int to) {
        var links = getResearchLinks(stack, ability);

        links.put(from, to);

        setResearchComponent(stack, ability, getResearchComponent(stack, ability).toBuilder()
                .links(links.asMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                entry -> new ArrayList<>(entry.getValue())
                        )))
                .build());
    }

    default void removeResearchLink(ItemStack stack, String ability, int from, int to) {
        var links = getResearchLinks(stack, ability);

        links.remove(from, to);

        setResearchComponent(stack, ability, getResearchComponent(stack, ability).toBuilder()
                .links(links.asMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                entry -> String.valueOf(entry.getKey()),
                                entry -> new ArrayList<>(entry.getValue())
                        )))
                .build());
    }

    default boolean isAbilityResearched(ItemStack stack, String ability) {
        return getResearchData(ability).getStars().isEmpty() || getResearchComponent(stack, ability).researched();
    }

    default void setAbilityResearched(ItemStack stack, String ability, boolean researched) {
        setResearchComponent(stack, ability, getResearchComponent(stack, ability).toBuilder()
                .researched(researched)
                .build());
    }

    default Multimap<Integer, Integer> getCorrectResearchLinks(ItemStack stack, String ability) {
        Multimap<Integer, Integer> schema = getResearchData(ability).getLinks();
        Multimap<Integer, Integer> links = getResearchLinks(stack, ability);

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

    default Multimap<Integer, Integer> getIncorrectResearchLinks(ItemStack stack, String ability) {
        Multimap<Integer, Integer> schema = getResearchData(ability).getLinks();
        Multimap<Integer, Integer> links = getResearchLinks(stack, ability);

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

    default double testAbilityResearchPercentage(ItemStack stack, String ability) {
        Multimap<Integer, Integer> schema = getResearchData(ability).getLinks();
        Multimap<Integer, Integer> links = getResearchLinks(stack, ability);

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

    default boolean testAbilityResearch(ItemStack stack, String ability) {
        return testAbilityResearchPercentage(stack, ability) >= 1D;
    }

    default int getResearchHintPlayerExperienceCost(String ability) {
        return 50;
    }

    default StatComponent getStatComponent(ItemStack stack, String ability, String stat) {
        AbilityComponent abilityComponent = getAbilityComponent(stack, ability);

        @Nullable StatComponent statComponent = abilityComponent.stats().get(stat);

        StatTemplate statData = getStatData(ability, stat);

        if (statComponent != null)
            return statComponent;
        else if (statData != null) {
            statComponent = StatComponent.EMPTY.toBuilder()
                    .initialValue(MathUtils.round(MathUtils.randomBetween(new Random(), statData.getInitialValue().getKey(), statData.getInitialValue().getValue()), 5))
                    .build();

            setAbilityComponent(stack, ability, abilityComponent.toBuilder()
                    .stat(stat, statComponent)
                    .build());

            return statComponent;
        } else
            return null;
    }

    default void setStatComponent(ItemStack stack, String ability, String stat, StatComponent component) {
        setAbilityComponent(stack, ability, getAbilityComponent(stack, ability).toBuilder()
                .stat(stat, component)
                .build());
    }

    default double getStatInitialValue(ItemStack stack, String ability, String stat) {
        return getStatComponent(stack, ability, stat).initialValue();
    }

    default void setStatInitialValue(ItemStack stack, String ability, String stat, double value) {
        setStatComponent(stack, ability, stat, getStatComponent(stack, ability, stat).toBuilder()
                .initialValue(value)
                .build());
    }

    default void addStatInitialValue(ItemStack stack, String ability, String stat, double value) {
        setStatInitialValue(stack, ability, stat, getStatInitialValue(stack, ability, stat) + value);
    }

    default int getAbilityLevel(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityComponent(stack, ability).points();
    }

    default void setAbilityLevel(LivingEntity entity, ItemStack stack, String ability, int points) {
        setAbilityComponent(stack, ability, getAbilityComponent(stack, ability).toBuilder()
                .points(points)
                .build());
    }

    default void addAbilityLevel(LivingEntity entity, ItemStack stack, String ability, int points) {
        setAbilityLevel(stack, ability, getAbilityLevel(stack, ability) + points);
    }

    default AbilityComponent randomizeAbilityStats(ItemStack stack, String ability, int luck) {
        Map<String, StatTemplate> stats = getAbilityData(ability).getStats();

        Random random = new Random();

        double targetQuality;

        do {
            int maxQuality = getMaxQuality();
            int maxLuck = getMaxLuck();

            // Random value in the [-1, 1] range
            double randomValue = (random.nextDouble() * 2D) - 1D;

            // Luck effect modifier. Lower value = lower chance to get 5 stars
            double modifier = getLuckModifier();

            // Bias based on luck (ranging from -0.5 to 0.5), multiplied by the modifier
            double bias = ((luck - (maxLuck / 2D)) / maxLuck) * modifier;

            // Apply the bias to randomValue and limit the result within the range [-1, 1]
            double biasedValue = Math.tanh(randomValue + bias);

            // Convert the biased result to the range [0, maxQuality]
            double weightedRandom = Math.floor((biasedValue + 1D) / 2D * (maxQuality + 1D));

            // Clamping the value to avoid overflow
            targetQuality = Mth.clamp(weightedRandom, 0, maxQuality);
        } while (targetQuality == getAbilityQuality(stack, ability));

        double sumQuality = 0;

        Map<String, Double> generatedQualities = new HashMap<>();

        for (String stat : stats.keySet()) {
            double randomQuality = MathUtils.randomBetween(random, 0, getMaxQuality());

            generatedQualities.put(stat, randomQuality);

            sumQuality += randomQuality;
        }

        double currentAverageQuality = sumQuality / stats.size();

        while (Math.abs(currentAverageQuality - targetQuality) > 0.01) {
            if (currentAverageQuality < targetQuality) {
                String minStat = generatedQualities.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();

                double increment = Math.min((targetQuality - currentAverageQuality) * stats.size(), getMaxQuality() - generatedQualities.get(minStat));

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
            randomizeStat(stack, ability, entry.getKey(), (int) Math.round(entry.getValue()));

        return getAbilityComponent(stack, ability);
    }

    default StatComponent randomizeStat(ItemStack stack, String ability, String stat, int quality) {
        StatTemplate entry = getStatData(ability, stat);

        double minValue = entry.getInitialValue().getKey();
        double maxValue = entry.getInitialValue().getValue();

        double diff = maxValue - minValue;

        double result = minValue + (diff * ((double) quality / getMaxQuality()));

        setStatInitialValue(stack, ability, stat, result);

        return getStatComponent(stack, ability, stat);
    }

    default StatComponent randomizeStat(ItemStack stack, String ability, String stat) {
        return randomizeStat(stack, ability, stat, new Random().nextInt(getMaxQuality() + 1));
    }

    default double getRelativeStatValue(String ability, String stat, double value, int points) {
        var data = getStatData(ability, stat);

        if (data == null)
            return 0D;

        var threshold = data.getThresholdValue();

        return MathUtils.round(Mth.clamp(data.getUpgradeModifier().getKey().apply(value, data.getUpgradeModifier().getValue(), points), threshold.getKey(), threshold.getValue()), 5);
    }

    default double getStatValue(ItemStack stack, String ability, String stat, int points) {
        return getRelativeStatValue(ability, stat, getStatInitialValue(stack, ability, stat), points);
    }

    default double getStatValue(ItemStack stack, String ability, String stat) {
        return getStatValue(stack, ability, stat, getAbilityLevel(stack, ability));
    }

    default boolean isEnoughLevel(ItemStack stack, String ability) {
        return getRelicLevel(stack) >= getAbilityData(ability).getRequiredLevel();
    }

    @UnstableApi
    default boolean isAbilityEnabled(ItemStack stack, String ability) {
        return true;
    }

    default boolean isAbilityUnlocked(ItemStack stack, String ability) {
        return isAbilityEnabled(stack, ability) && isEnoughLevel(stack, ability) && isLockUnlocked(stack, ability) && isAbilityResearched(stack, ability);
    }

    default boolean hasUnlockedUpgradeableAbility(ItemStack stack) {
        return getAbilitiesData().getAbilities().keySet().stream().anyMatch(ability -> canBeUpgraded(stack, ability) && isAbilityUnlocked(stack, ability));
    }

    default boolean hasUnlockedAbility(ItemStack stack) {
        return getAbilitiesData().getAbilities().keySet().stream().anyMatch(ability -> isAbilityUnlocked(stack, ability));
    }

    default boolean canPlayerUseAbility(Player player, ItemStack stack, String ability) {
        return isAbilityUnlocked(stack, ability) && testAbilityPredicates(player, stack, ability, PredicateType.CAST) && getAbilityCooldown(stack, ability) <= 0;
    }

    default boolean canPlayerSeeAbility(Player player, ItemStack stack, String ability) {
        return testAbilityPredicates(player, stack, ability, PredicateType.VISIBILITY);
    }

    default boolean mayUnlock(ItemStack stack, String ability) {
        return isEnoughLevel(stack, ability) && !isLockUnlocked(stack, ability);
    }

    default boolean mayResearch(ItemStack stack, String ability) {
        return isEnoughLevel(stack, ability) && isLockUnlocked(stack, ability) && !isAbilityResearched(stack, ability);
    }

    default int getUpgradePlayerExperienceCost(ItemStack stack, String ability) {
        return (getAbilityLevel(stack, ability) + 1) * 50;
    }

    default boolean canBeUpgraded(ItemStack stack, String ability) {
        return getAbilityMaxLevel(stack, ability) > 0 && !getAbilityData(ability).getStats().isEmpty();
    }

    default boolean mayUpgrade(ItemStack stack, String ability) {
        AbilityTemplate entry = getAbilityData(ability);

        return canBeUpgraded(stack, ability) && !isAbilityMaxLevel(stack, ability) && getRelicLevelingPoints(stack) >= entry.getRequiredPoints() && isAbilityUnlocked(stack, ability);
    }

    default boolean mayPlayerUpgrade(Player player, ItemStack stack, String ability) {
        return mayUpgrade(stack, ability) && EntityUtils.getPlayerTotalExperience(player) >= getUpgradePlayerExperienceCost(stack, ability);
    }

    default boolean upgrade(Player player, ItemStack stack, String ability) {
        if (!mayPlayerUpgrade(player, stack, ability))
            return false;

        player.giveExperiencePoints(-getUpgradePlayerExperienceCost(stack, ability));

        setAbilityLevel(stack, ability, getAbilityLevel(stack, ability) + 1);
        addRelicLevelingPoints(stack, -getAbilityData(ability).getRequiredPoints());

        return true;
    }

    default int getRerollPlayerExperienceCost(ItemStack stack, String ability) {
        return (getRelicLuck(stack) * 5) + 50;
    }

    default boolean mayReroll(ItemStack stack, String ability) {
        return !getAbilityData(ability).getStats().isEmpty() && isAbilityUnlocked(stack, ability);
    }

    default boolean mayPlayerReroll(Player player, ItemStack stack, String ability) {
        return mayReroll(stack, ability) && EntityUtils.getPlayerTotalExperience(player) >= getRerollPlayerExperienceCost(stack, ability);
    }

    default boolean reroll(Player player, ItemStack stack, String ability) {
        if (!mayPlayerReroll(player, stack, ability))
            return false;

        player.giveExperiencePoints(-getRerollPlayerExperienceCost(stack, ability));

        int prevQuality = getAbilityQuality(stack, ability);

        randomizeAbilityStats(stack, ability, getRelicLuck(stack));

        int newQuality = getAbilityQuality(stack, ability);

        if (newQuality < prevQuality)
            addRelicLuck(stack, (int) (Math.ceil((prevQuality - newQuality) / 2D)));

        return true;
    }

    default int getResetPlayerExperienceCost(ItemStack stack, String ability) {
        return getAbilityLevel(stack, ability) * 250;
    }

    default boolean mayReset(ItemStack stack, String ability) {
        return getAbilityLevel(stack, ability) > 0 && isAbilityUnlocked(stack, ability);
    }

    default boolean mayPlayerReset(Player player, ItemStack stack, String ability) {
        return !getAbilityData(ability).getStats().isEmpty() && mayReset(stack, ability) && EntityUtils.getPlayerTotalExperience(player) >= getResetPlayerExperienceCost(stack, ability);
    }

    default boolean reset(Player player, ItemStack stack, String ability) {
        if (!mayPlayerReset(player, stack, ability))
            return false;

        player.giveExperiencePoints(-getResetPlayerExperienceCost(stack, ability));

        addRelicLevelingPoints(stack, getAbilityLevel(stack, ability) * getAbilityData(ability).getRequiredPoints());
        setAbilityLevel(stack, ability, 0);

        return true;
    }

    default int getAbilityCooldownCap(ItemStack stack, String ability) {
        return getAbilityExtenderComponent(stack, ability).cooldownCap();
    }

    default void setAbilityCooldownCap(ItemStack stack, String ability, int amount) {
        setAbilityExtenderComponent(stack, ability, getAbilityExtenderComponent(stack, ability).toBuilder()
                .cooldownCap(amount)
                .build());
    }

    default int getAbilityCooldown(ItemStack stack, String ability) {
        return getAbilityExtenderComponent(stack, ability).cooldown();
    }

    default void setAbilityCooldown(ItemStack stack, String ability, int amount) {
        setAbilityExtenderComponent(stack, ability, getAbilityExtenderComponent(stack, ability).toBuilder()
                .cooldownCap(amount)
                .cooldown(amount)
                .build());
    }

    default void addAbilityCooldown(ItemStack stack, String ability, int amount) {
        setAbilityExtenderComponent(stack, ability, getAbilityExtenderComponent(stack, ability).toBuilder()
                .cooldown(getAbilityCooldown(stack, ability) + amount)
                .build());
    }

    default void setAbilityTicking(ItemStack stack, String ability, boolean ticking) {
        setAbilityExtenderComponent(stack, ability, getAbilityExtenderComponent(stack, ability).toBuilder()
                .ticking(ticking)
                .build());
    }

    default boolean isAbilityTicking(ItemStack stack, String ability) {
        return isAbilityUnlocked(stack, ability) && getAbilityExtenderComponent(stack, ability).ticking();
    }

    default boolean isAbilityOnCooldown(ItemStack stack, String ability) {
        return getAbilityCooldown(stack, ability) > 0;
    }

    default boolean isAbilityUpgradeEnabled(ItemStack stack, String ability) {
        return isAbilityUnlocked(stack, ability) && !getAbilityData(ability).getStats().isEmpty();
    }

    default boolean isAbilityRerollEnabled(ItemStack stack, String ability) {
        return isAbilityUnlocked(stack, ability) && !getAbilityData(ability).getStats().isEmpty();
    }

    default boolean isAbilityResetEnabled(ItemStack stack, String ability) {
        return isAbilityUnlocked(stack, ability) && !getAbilityData(ability).getStats().isEmpty();
    }
}