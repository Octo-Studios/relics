package it.hurts.sskirillss.relics.api.relics;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.netty.util.internal.UnstableApi;
import it.hurts.sskirillss.relics.api.events.leveling.ExperienceAddEvent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.api.relics.events.RelicLevelEvent;
import it.hurts.sskirillss.relics.api.relics.events.RelicMaxLevelEvent;
import it.hurts.sskirillss.relics.components.*;
import it.hurts.sskirillss.relics.config.data.RelicConfigData;
import it.hurts.sskirillss.relics.entities.RelicExperienceOrbEntity;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.init.EntityRegistry;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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

public interface IRelicItem extends IRelicTemplateHolder, IRelicDataHolder, IRelicDataProcessor {
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

    // TODO: Experimental. I don't rly think I need to post an event for each setter/getter :/

    default int getRelicMaxLevel(LivingEntity entity, ItemStack stack) {
        return NeoForge.EVENT_BUS.post(new RelicMaxLevelEvent.Get(entity, stack, getLevelingTemplate(entity, stack).getMaxLevel())).getMaxLevel();
    }

    default int getRelicLevel(LivingEntity entity, ItemStack stack) {
        return NeoForge.EVENT_BUS.post(new RelicLevelEvent.Get(entity, stack, getLevelingComponent(stack).getLevel())).getLevel();
    }

    default void setRelicLevel(LivingEntity entity, ItemStack stack, int level) {
        setLevelingComponent(stack, getLevelingComponent(stack).toBuilder().level(Math.max(0, NeoForge.EVENT_BUS.post(new RelicLevelEvent.Set(entity, stack, level)).getLevel())).build());
    }

    default void addRelicLevel(LivingEntity entity, ItemStack stack, int level) {
        setRelicLevel(entity, stack, getRelicLevel(entity, stack) + NeoForge.EVENT_BUS.post(new RelicLevelEvent.Add(entity, stack, level)).getLevel());
    }

    default double getRelicExperience(LivingEntity entity, ItemStack stack) {
        return getLevelingComponent(stack).getExperience();
    }

    default void setRelicExperience(LivingEntity entity, ItemStack stack, double experience) {
        setLevelingComponent(stack, getLevelingComponent(stack).toBuilder()
                .experience(Math.clamp(experience, 0D, getTotalRelicExperienceForLevel(entity, stack, getRelicLevel(entity, stack) + 1)))
                .build());
    }

    default boolean addRelicExperience(@Nullable LivingEntity entity, ItemStack stack, double amount) {
        var event = new ExperienceAddEvent(entity instanceof LivingEntity ? entity : null, stack, amount);

        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled())
            return false;

        var currentExperience = getRelicExperience(entity, stack);
        var currentLevel = getRelicLevel(entity, stack);

        var toAdd = event.getAmount();

        if (toAdd == 0D)
            return false;

        var resultLevel = currentLevel;
        var resultExperience = 0D;

        var maxLevel = getLevelingTemplate(entity, stack).getMaxLevel();

        while (toAdd > 0D) {
            if (resultLevel >= maxLevel)
                break;

            var requiredExperience = getTotalRelicExperienceBetweenLevels(entity, stack, resultLevel, resultLevel + 1);

            var diff = requiredExperience - currentExperience;

            if (toAdd >= diff) {
                toAdd -= diff;

                resultLevel++;

                currentExperience = 0D;
            } else {
                resultExperience = currentExperience + toAdd;

                break;
            }
        }

        setRelicExperience(entity, stack, resultExperience);

        if (currentLevel != resultLevel) {
            setRelicLevel(entity, stack, resultLevel);

            addRelicLevelingPoints(entity, stack, resultLevel - currentLevel);
        }

        return true;
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

    default int getRelicLevelingPoints(LivingEntity entity, ItemStack stack) {
        return getLevelingComponent(stack).points();
    }

    default int getRelicRank(LivingEntity entity, ItemStack stack) {
        return getLevelingComponent(stack).rank();
    }

    default void setRelicRank(LivingEntity entity, ItemStack stack, int rank) {
        setLevelingComponent(stack, getLevelingComponent(stack).toBuilder().rank(Math.max(0, rank)).build());
    }

    default void addRelicRank(LivingEntity entity, ItemStack stack, int rank) {
        setRelicLevelingPoints(entity, stack, getRelicLevelingPoints(entity, stack) + rank);
    }

    default void setRelicLevelingPoints(LivingEntity entity, ItemStack stack, int amount) {
        setLevelingComponent(stack, getLevelingComponent(stack).toBuilder().points(Math.max(0, amount)).build());
    }

    default void addRelicLevelingPoints(LivingEntity entity, ItemStack stack, int amount) {
        setRelicLevelingPoints(entity, stack, getRelicLevelingPoints(entity, stack) + amount);
    }

    default int getMaxLuck(LivingEntity entity, ItemStack stack) {
        return 100;
    }

    default double getLuckModifier(LivingEntity entity, ItemStack stack) {
        return 1.15D;
    }

    default int getRelicLuck(LivingEntity entity, ItemStack stack) {
        return getLevelingComponent(stack).luck();
    }

    default void setRelicLuck(LivingEntity entity, ItemStack stack, int amount) {
        setLevelingComponent(stack, getLevelingComponent(stack).toBuilder().luck(Mth.clamp(amount, 0, getMaxLuck(entity, stack))).build());
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

    default void dropRelicExperience(Level level, Vec3 pos, int amount) {
        if (amount <= 0)
            return;

        RandomSource random = level.getRandom();

        int orbs = Math.max(amount / RelicExperienceOrbEntity.getMaxExperience(), random.nextInt(amount) + 1);

        for (int i = 0; i < orbs; i++) {
            RelicExperienceOrbEntity orb = new RelicExperienceOrbEntity(EntityRegistry.RELIC_EXPERIENCE_ORB.get(), level);

            orb.setPos(pos);
            orb.setExperience(amount / orbs);

            orb.setDeltaMovement(
                    (-1 + 2 * random.nextFloat()) * 0.15F,
                    0.1F + random.nextFloat() * 0.2F,
                    (-1 + 2 * random.nextFloat()) * 0.15F
            );

            level.addFreshEntity(orb);
        }
    }

    default double getRelicExperienceLeftForLevelUp(LivingEntity entity, ItemStack stack, int level) {
        int currentLevel = getRelicLevel(entity, stack);

        return getTotalRelicExperienceBetweenLevels(entity, stack, currentLevel, level) - getRelicExperience(entity, stack);
    }

    @Deprecated(forRemoval = true)
    default boolean isSomethingWrongWithLevelingPoints(LivingEntity entity, ItemStack stack) {
        int current = getRelicLevelingPoints(entity, stack);

        for (var data : getAbilitiesTemplate(entity, stack).getAbilities().values())
            current += getAbilityComponent(stack, data.getId()).points() * data.getRequiredPoints();

        return current != getRelicLevel(entity, stack);
    }

    default int getTotalRelicExperienceBetweenLevels(LivingEntity entity, ItemStack stack, int from, int to) {
        return getTotalRelicExperienceForLevel(entity, stack, to) - getTotalRelicExperienceForLevel(entity, stack, from);
    }

    default int getTotalRelicExperienceForLevel(LivingEntity entity, ItemStack stack, int level) {
        if (level <= 0)
            return 0;

        LevelingTemplate levelingData = getLevelingTemplate(entity, stack);

        if (levelingData == null)
            return 0;

        int result = levelingData.getInitialCost();

        for (int i = 1; i < level; i++)
            result += levelingData.getInitialCost() + (levelingData.getStep() * i);

        return result;
    }

    default int getRelicLevelFromExperience(LivingEntity entity, ItemStack stack, int experience) {
        int result = 0;
        int amount;

        do {
            ++result;

            amount = getTotalRelicExperienceForLevel(entity, stack, result);
        } while (amount <= experience);

        return result - 1;
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

    default DataComponent getDataComponent(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.DATA, DataComponent.EMPTY);
    }

    default void setDataComponent(ItemStack stack, DataComponent component) {
        stack.set(DataComponentRegistry.DATA, component);
    }

    default LevelingComponent getLevelingComponent(ItemStack stack) {
        return getDataComponent(stack).leveling();
    }

    default void setLevelingComponent(ItemStack stack, LevelingComponent component) {
        setDataComponent(stack, getDataComponent(stack).toBuilder().leveling(component).build());
    }

    default AbilitiesComponent getAbilitiesComponent(ItemStack stack) {
        return getDataComponent(stack).abilities();
    }

    default void setAbilitiesComponent(ItemStack stack, AbilitiesComponent component) {
        setDataComponent(stack, getDataComponent(stack).toBuilder().abilities(component).build());
    }

    default AbilityComponent getAbilityComponent(ItemStack stack, String ability) {
        AbilitiesComponent abilitiesComponent = getAbilitiesComponent(stack);

        @Nullable AbilityComponent abilityComponent = abilitiesComponent.abilities().get(ability);

        AbilityTemplate abilityData = getAbilityData(ability);

        if (abilityComponent != null)
            return abilityComponent;
        else if (abilityData != null) {
            AbilityComponent.AbilityComponentBuilder builder = AbilityComponent.EMPTY.toBuilder();

            if (abilityData.getCastData().getType() == CastType.TOGGLEABLE)
                builder.extender(AbilityExtenderComponent.EMPTY.toBuilder()
                        .ticking(true)
                        .build());

            if (isEnoughLevel(stack, ability))
                builder.lock(LockComponent.EMPTY.toBuilder()
                        .unlocks(getMaxLockUnlocks())
                        .build());

            abilityComponent = builder.build();

            setAbilitiesComponent(stack, abilitiesComponent.toBuilder()
                    .ability(ability, abilityComponent)
                    .build());

            return abilityComponent;
        } else
            return null;
    }

    default void setAbilityComponent(ItemStack stack, String ability, AbilityComponent component) {
        setAbilitiesComponent(stack, getAbilitiesComponent(stack).toBuilder().ability(ability, component).build());
    }

    default AbilityExtenderComponent getAbilityExtenderComponent(ItemStack stack, String ability) {
        return getAbilityComponent(stack, ability).extender();
    }

    default void setAbilityExtenderComponent(ItemStack stack, String ability, AbilityExtenderComponent component) {
        setAbilityComponent(stack, ability, getAbilityComponent(stack, ability).toBuilder()
                .extender(component)
                .build());
    }

    default LockComponent getLockComponent(ItemStack stack, String ability) {
        return getAbilityComponent(stack, ability).lock();
    }

    default void setLockComponent(ItemStack stack, String ability, LockComponent component) {
        setAbilityComponent(stack, ability, getAbilityComponent(stack, ability).toBuilder()
                .lock(component)
                .build());
    }

    default int getMaxLockUnlocks() {
        return 5;
    }

    default int getLockUnlocks(ItemStack stack, String ability) {
        return getLockComponent(stack, ability).unlocks();
    }

    default void setLockUnlocks(ItemStack stack, String ability, int unlocks) {
        setLockComponent(stack, ability, getLockComponent(stack, ability).toBuilder()
                .unlocks(Mth.clamp(unlocks, 0, getMaxLockUnlocks()))
                .build());
    }

    default void addLockUnlocks(ItemStack stack, String ability, int unlocks) {
        setLockUnlocks(stack, ability, getLockUnlocks(stack, ability) + unlocks);
    }

    default boolean isLockUnlocked(ItemStack stack, String ability) {
        return getLockUnlocks(stack, ability) >= getMaxLockUnlocks();
    }

    default ResearchComponent getResearchComponent(ItemStack stack, String ability) {
        return getAbilityComponent(stack, ability).research();
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

    default int getAbilityLevel(ItemStack stack, String ability) {
        return getAbilityComponent(stack, ability).points();
    }

    @UnstableApi
    default int getAbilityMaxLevel(ItemStack stack, String ability) {
        return getAbilityData(ability).getMaxLevel();
    }

    default void setAbilityLevel(ItemStack stack, String ability, int points) {
        setAbilityComponent(stack, ability, getAbilityComponent(stack, ability).toBuilder()
                .points(points)
                .build());
    }

    default void addAbilityLevel(ItemStack stack, String ability, int points) {
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