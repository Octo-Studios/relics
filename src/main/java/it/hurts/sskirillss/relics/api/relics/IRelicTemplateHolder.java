package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStorage;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * An interface for objects (typically items) that hold and manage a {@link RelicTemplate}.
 * <p>
 * This interface provides both default and context-sensitive access to relic-related templates, including abilities, leveling mechanics, loot sources, and associated metadata. It acts as a bridge between the core data model of a relic and its in-game behavior.
 * </p>
 *
 * <p>
 * The interface supports:
 * <ul>
 *     <li>Default relic template construction and storage</li>
 *     <li>Lazy evaluation of template data and internal caching</li>
 *     <li>Access to nested templates such as {@link AbilitiesTemplate}, {@link LevelingTemplate},
 *         {@link LootTemplate}, and related subcomponents</li>
 *     <li>Context-aware overrides based on {@link LivingEntity} and {@link ItemStack} data</li>
 * </ul>
 * </p>
 *
 * <p>
 * Template construction of {@link #constructDefaultRelicTemplate()} can be decomposed and
 * customized through helper methods such as {@link #constructDefaultAbilitiesTemplate()},
 * {@link #constructDefaultLevelingTemplate()}, and {@link #constructDefaultLootTemplate()} for modular setup.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> This interface is already implemented and integrated in {@link IRelicItem}, which
 * is the primary entry point for working with relic items. In most cases, you should implement
 * {@link IRelicItem} instead of directly implementing this interface, unless you have a specific
 * reason to separate concerns.
 * </p>
 *
 * @implNote While default implementations are provided, overriding these methods is encouraged to accommodate specific relic designs or gameplay mechanics.
 * @see RelicTemplate
 * @see IRelicItem
 */
public interface IRelicTemplateHolder {

    /**
     * Returns the {@link RelicTemplate} that defines the default behavior of this relic item.
     * <p>
     * The template encapsulates the core immutable data of the relic, such as abilities, leveling rules, loot tables, and other configuration parameters.
     * </p>
     *
     * @return the default {@link RelicTemplate} for this relic
     * @implNote Implementations are free to define the template inline using a DSL-like builder for quick setup, or to delegate the construction to a dedicated "template orchestrator" with smaller helper methods for better readability. When applying decomposition, consider grouping logic by concern to maintain a modular structure.
     * <p>
     * As part of decomposition or orchestration, it is encouraged to reuse specialized methods like {@link #constructDefaultAbilitiesTemplate()}, {@link #constructDefaultLevelingTemplate()}, and {@link #constructDefaultLootTemplate()} to encapsulate and isolate logic related to each specific aspect of the relic template.
     * </p>
     */
    @ApiStatus.OverrideOnly
    default RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(constructDefaultAbilitiesTemplate())
                .leveling(constructDefaultLevelingTemplate())
                .loot(constructDefaultLootTemplate())
                .build();
    }

    /**
     * Constructs the {@link AbilitiesTemplate} defining the default set of abilities for this relic.
     *
     * @return the default {@link AbilitiesTemplate} associated with this relic
     */
    @ApiStatus.OverrideOnly
    default AbilitiesTemplate constructDefaultAbilitiesTemplate() {
        return AbilitiesTemplate.builder().build();
    }

    /**
     * Constructs the {@link LevelingTemplate} that governs how this relic progresses over time.
     *
     * @return the default {@link LevelingTemplate} used for this relic
     */
    @ApiStatus.OverrideOnly
    default LevelingTemplate constructDefaultLevelingTemplate() {
        return LevelingTemplate.builder().build();
    }

    /**
     * Constructs the {@link LootTemplate} specifying how and where this relic may be obtained.
     *
     * @return the default {@link LootTemplate} for this relic
     */
    @ApiStatus.OverrideOnly
    default LootTemplate constructDefaultLootTemplate() {
        return LootTemplate.builder().build();
    }

    /**
     * Associates a {@link RelicTemplate} with this instance.
     *
     * @param data the new {@link RelicTemplate} to store for this holder
     */
    default void setRelicTemplate(RelicTemplate data) {
        RelicStorage.RELIC_TEMPLATES.put(this, data);
    }

    /**
     * Retrieves or constructs the stored {@link RelicTemplate} for this instance, using lazy initialization.
     *
     * @return the cached or newly constructed {@link RelicTemplate}
     */
    @ApiStatus.Internal
    default RelicTemplate getDefaultRelicTemplate() {
        return RelicStorage.RELIC_TEMPLATES.computeIfAbsent(this, data -> constructDefaultRelicTemplate());
    }

    /**
     * Returns the {@link AbilitiesTemplate} from the default relic template, without context overrides.
     *
     * @return the default {@link AbilitiesTemplate}
     */
    @ApiStatus.Internal
    default AbilitiesTemplate getDefaultAbilitiesTemplate() {
        return getDefaultRelicTemplate().getAbilities();
    }

    /**
     * Returns the {@link LootTemplate} from the default relic template, without context overrides.
     *
     * @return the default {@link LootTemplate}
     */
    @ApiStatus.Internal
    default LootTemplate getDefaultLootTemplate() {
        return getDefaultRelicTemplate().getLoot();
    }

    /**
     * Retrieves a specific {@link AbilityTemplate} by its ID from the default abilities template.
     *
     * @param ability the ID of the ability
     * @return the corresponding {@link AbilityTemplate}, or {@code null} if not found
     */
    @ApiStatus.Internal
    default AbilityTemplate getDefaultAbilityTemplate(String ability) {
        return getDefaultAbilitiesTemplate().getAbilities().get(ability);
    }

    /**
     * Retrieves the {@link ResearchTemplate} for a given ability from the default template.
     *
     * @param ability the ID of the ability
     * @return the corresponding {@link ResearchTemplate}, or {@code null} if not found
     */
    @ApiStatus.Internal
    default ResearchTemplate getDefaultResearchTemplate(String ability) {
        return getDefaultAbilityTemplate(ability).getResearchTemplate();
    }

    /**
     * Retrieves the {@link StatTemplate} for a specific stat of a given ability from the default template.
     *
     * @param ability the ID of the ability
     * @param stat    the ID of the stat
     * @return the corresponding {@link StatTemplate}, or {@code null} if not found
     */
    @ApiStatus.Internal
    default StatTemplate getDefaultStatTemplate(String ability, String stat) {
        return getDefaultAbilityTemplate(ability).getStats().get(stat);
    }

    /**
     * Returns the {@link LevelingTemplate} from the default relic template, without context overrides.
     *
     * @return the default {@link LevelingTemplate}
     */
    @ApiStatus.Internal
    default LevelingTemplate getDefaultLevelingTemplate() {
        return getDefaultRelicTemplate().getLeveling();
    }

    /**
     * Returns the {@link RelicStatisticTemplate} from the default relic template, without context overrides.
     *
     * @return the default {@link RelicStatisticTemplate}
     */
    @ApiStatus.Internal
    default RelicStatisticTemplate getDefaultStatisticTemplate() {
        return getDefaultRelicTemplate().getStatistic();
    }

    /**
     * Retrieves a {@link MetricTemplate} by its ID from the default statistic template.
     *
     * @param metric the ID of the metric
     * @return the corresponding {@link MetricTemplate}, or {@code null} if not found
     */
    @ApiStatus.Internal
    default MetricTemplate getDefaultMetricTemplate(String metric) {
        return getDefaultStatisticTemplate().getMetrics().get(metric);
    }

    /**
     * Returns the {@link RelicTemplate} associated with the given entity and item context.
     * <p>
     * By default, this returns the static default template. Override to implement dynamic context-based behavior (e.g., scaling with player data or item NBT).
     * </p>
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @return the contextual {@link RelicTemplate}
     */
    default RelicTemplate getRelicTemplate(LivingEntity entity, ItemStack stack) {
        return getDefaultRelicTemplate();
    }

    /**
     * Returns the {@link AbilitiesTemplate} from the contextual relic template.
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @return the contextual {@link AbilitiesTemplate}
     */
    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default AbilitiesTemplate getAbilitiesTemplate(LivingEntity entity, ItemStack stack) {
        return getRelicTemplate(entity, stack).getAbilities();
    }

    /**
     * Retrieves a specific {@link AbilityTemplate} by its ID from the contextual template.
     *
     * @param entity  the holder of the item
     * @param stack   the item stack instance
     * @param ability the ID of the ability
     * @return the corresponding {@link AbilityTemplate}, or {@code null} if not present
     */
    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default AbilityTemplate getAbilityTemplate(LivingEntity entity, ItemStack stack, String ability) {
        return getRelicTemplate(entity, stack).getAbilities().getAbilities().get(ability);
    }

    /**
     * Retrieves the {@link ResearchTemplate} for a given ability from the contextual template.
     *
     * @param entity  the holder of the item
     * @param stack   the item stack instance
     * @param ability the ID of the ability
     * @return the contextual {@link ResearchTemplate}, or {@code null} if not found
     */
    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default ResearchTemplate getResearchTemplate(LivingEntity entity, ItemStack stack, String ability) {
        return getRelicTemplate(entity, stack).getAbilities().getAbilities().get(ability).getResearchTemplate();
    }

    /**
     * Retrieves the {@link StatTemplate} for a specific stat of a given ability from the contextual template.
     *
     * @param entity  the holder of the item
     * @param stack   the item stack instance
     * @param ability the ID of the ability
     * @param stat    the ID of the stat
     * @return the contextual {@link StatTemplate}, or {@code null} if not found
     */
    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default StatTemplate getStatTemplate(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return getRelicTemplate(entity, stack).getAbilities().getAbilities().get(ability).getStats().get(stat);
    }

    /**
     * Returns the {@link LevelingTemplate} from the contextual relic template.
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @return the contextual {@link LevelingTemplate}
     */
    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default LevelingTemplate getLevelingTemplate(LivingEntity entity, ItemStack stack) {
        return getRelicTemplate(entity, stack).getLeveling();
    }

    /**
     * Returns the {@link RelicStatisticTemplate} from the contextual relic template.
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @return the contextual {@link RelicStatisticTemplate}
     */
    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default RelicStatisticTemplate getRelicStatisticTemplate(LivingEntity entity, ItemStack stack) {
        return this.getRelicTemplate(entity, stack).getStatistic();
    }

    /**
     * Retrieves a {@link MetricTemplate} by its ID from the contextual statistic template.
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @param metric the ID of the metric
     * @return the contextual {@link MetricTemplate}, or {@code null} if not found
     */
    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default MetricTemplate getRelicMetricTemplate(LivingEntity entity, ItemStack stack, String metric) {
        return this.getRelicStatisticTemplate(entity, stack).getMetrics().get(metric);
    }

    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default AbilityStatisticTemplate getAbilityStatisticTemplate(LivingEntity entity, ItemStack stack, String ability) {
        return this.getAbilityTemplate(entity, stack, ability).getStatistic();
    }

    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default MetricTemplate getAbilityMetricTemplate(LivingEntity entity, ItemStack stack, String ability, String metric) {
        return this.getAbilityStatisticTemplate(entity, stack, ability).getMetrics().get(metric);
    }

    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default ExperienceSourcesTemplate getExperienceSourcesTemplate(LivingEntity entity, ItemStack stack, String ability) {
        return this.getAbilityTemplate(entity, stack, ability).getExperienceSources();
    }

    @ApiStatus.Obsolete
    @ApiStatus.NonExtendable
    default ExperienceSourceTemplate getExperienceSourceTemplate(LivingEntity entity, ItemStack stack, String ability, String metric) {
        return this.getExperienceSourcesTemplate(entity, stack, ability).getSources().get(metric);
    }
}