package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicStorage;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingSourceTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingSourcesTemplate;
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
 * - Default relic template construction and storage
 * - Lazy evaluation of template data and internal caching
 * - Access to nested templates such as {@link AbilitiesTemplate}, {@link LevelingTemplate}, {@link LootTemplate}, and related subcomponents
 * - Context-aware overrides based on {@link LivingEntity} and {@link ItemStack} data
 * </ul>
 * </p>
 *
 * <p>
 * Template construction can be decomposed and customized through helper methods such as {@link #constructDefaultAbilitiesTemplate()}, {@link #constructDefaultLevelingTemplate()}, and {@link #constructDefaultLootTemplate()} for modular setup.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> This interface is already implemented and integrated in {@link IRelicItem}, which is the primary entry point for working with relic items. In most cases, you should implement {@link IRelicItem} instead of directly implementing this interface, unless you have a specific reason to separate concerns.
 * </p>
 *
 * @implNote While default implementations are provided, overriding these methods is encouraged to accommodate specific relic designs or gameplay mechanics.
 * @see RelicTemplate
 * @see IRelicItem
 */
@ApiStatus.Internal
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
    default AbilitiesTemplate constructDefaultAbilitiesTemplate() {
        return AbilitiesTemplate.builder().build();
    }

    /**
     * Constructs the {@link LevelingTemplate} that governs how this relic progresses over time.
     *
     * @return the default {@link LevelingTemplate} used for this relic
     */
    default LevelingTemplate constructDefaultLevelingTemplate() {
        return LevelingTemplate.builder().build();
    }

    /**
     * Constructs the {@link LootTemplate} specifying how and where this relic may be obtained.
     *
     * @return the default {@link LootTemplate} for this relic
     */
    default LootTemplate constructDefaultLootTemplate() {
        return LootTemplate.builder().build();
    }

    /**
     * Sets the {@link RelicTemplate} for this object.
     *
     * @param data the new {@link RelicTemplate} to associate with this instance.
     */
    default void setRelicTemplate(RelicTemplate data) {
        RelicStorage.RELIC_TEMPLATES.put(this, data);
    }

    /**
     * Returns the {@link RelicTemplate} associated with this object.
     *
     * @return the {@link RelicTemplate} linked to this instance.
     */
    @ApiStatus.NonExtendable
    default RelicTemplate getDefaultRelicTemplate() {
        return RelicStorage.RELIC_TEMPLATES.computeIfAbsent(this, data -> constructDefaultRelicTemplate());
    }

    /**
     * Returns the {@link AbilitiesTemplate} defined in this relic's raw template.
     * <p>
     * This method bypasses any runtime or context-specific overrides and accesses the base ability configuration associated with the default {@link RelicTemplate}.
     * </p>
     *
     * @return the raw {@link AbilitiesTemplate} of this relic
     */
    @ApiStatus.NonExtendable
    default AbilitiesTemplate getDefaultAbilitiesTemplate() {
        return getDefaultRelicTemplate().getAbilities();
    }

    /**
     * Returns the raw {@link AbilityTemplate} by its ID from this relic's base template.
     *
     * @param ability the ID of the ability
     * @return the corresponding {@link AbilityTemplate}, or {@code null} if not present
     */
    @ApiStatus.NonExtendable
    default AbilityTemplate getDefaultAbilityTemplate(String ability) {
        return getDefaultAbilitiesTemplate().getAbilities().get(ability);
    }

    /**
     * Returns the {@link ResearchTemplate} associated with a specific ability from the raw template.
     *
     * @param ability the ID of the ability
     * @return the corresponding {@link ResearchTemplate}, or {@code null} if not present
     */
    @ApiStatus.NonExtendable
    default ResearchTemplate getDefaultResearchTemplate(String ability) {
        return getDefaultAbilityTemplate(ability).getResearchTemplate();
    }

    /**
     * Returns the {@link StatTemplate} for a specific stat of a specific ability from the raw template.
     *
     * @param ability the ID of the ability
     * @param stat    the ID of the stat
     * @return the matching {@link StatTemplate}, or {@code null} if not found
     */
    @ApiStatus.NonExtendable
    default StatTemplate getDefaultStatTemplate(String ability, String stat) {
        return getDefaultAbilityTemplate(ability).getStats().get(stat);
    }

    /**
     * Returns the {@link LevelingTemplate} associated with this relic's raw template.
     * <p>
     * Contains experience curves, thresholds, and leveling behaviors.
     * </p>
     *
     * @return the raw {@link LevelingTemplate}
     */
    @ApiStatus.NonExtendable
    default LevelingTemplate getDefaultLevelingTemplate() {
        return getDefaultRelicTemplate().getLeveling();
    }

    /**
     * Returns the {@link LevelingSourcesTemplate} from the raw leveling configuration.
     * <p>
     * Encapsulates all XP gain sources defined in the relic template.
     * </p>
     *
     * @return the raw {@link LevelingSourcesTemplate}
     */
    @ApiStatus.NonExtendable
    default LevelingSourcesTemplate getDefaultLevelingSourcesTemplate() {
        return getDefaultLevelingTemplate().getSources();
    }

    /**
     * Returns the {@link LevelingSourceTemplate} for a specific experience source from the raw template.
     *
     * @param source the ID of the experience source
     * @return the corresponding {@link LevelingSourceTemplate}, or {@code null} if not found
     */
    @ApiStatus.NonExtendable
    default LevelingSourceTemplate getDefaultLevelingSourceTemplate(String source) {
        return getDefaultLevelingSourcesTemplate().getSources().get(source);
    }

    /**
     * Returns the {@link RelicTemplate} associated with the given entity and item context.
     * <p>
     * By default, this method returns the raw template. Implementations may override this to provide dynamic behavior based on the context (e.g., NBT, player status, etc.).
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
     * Returns the {@link AbilitiesTemplate} from the contextual {@link RelicTemplate}.
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @return the contextual {@link AbilitiesTemplate}
     */
    default AbilitiesTemplate getAbilitiesTemplate(LivingEntity entity, ItemStack stack) {
        return getRelicTemplate(entity, stack).getAbilities();
    }

    /**
     * Returns the {@link AbilityTemplate} for the given ability ID from the contextual template.
     *
     * @param entity  the holder of the item
     * @param stack   the item stack instance
     * @param ability the ID of the ability
     * @return the corresponding {@link AbilityTemplate}, or {@code null} if not present
     */
    default AbilityTemplate getAbilityTemplate(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilitiesTemplate(entity, stack).getAbilities().get(ability);
    }

    /**
     * Returns the {@link ResearchTemplate} for the given ability from the contextual template.
     *
     * @param entity  the holder of the item
     * @param stack   the item stack instance
     * @param ability the ID of the ability
     * @return the contextual {@link ResearchTemplate}, or {@code null} if not found
     */
    default ResearchTemplate getResearchTemplate(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityTemplate(entity, stack, ability).getResearchTemplate();
    }

    /**
     * Returns the {@link StatTemplate} for a given ability and stat ID from the contextual template.
     *
     * @param entity  the holder of the item
     * @param stack   the item stack instance
     * @param ability the ID of the ability
     * @param stat    the ID of the stat
     * @return the contextual {@link StatTemplate}, or {@code null} if not found
     */
    default StatTemplate getStatTemplate(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return getAbilityTemplate(entity, stack, ability).getStats().get(stat);
    }

    /**
     * Returns the {@link LevelingTemplate} from the contextual {@link RelicTemplate}.
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @return the contextual {@link LevelingTemplate}
     */
    default LevelingTemplate getLevelingTemplate(LivingEntity entity, ItemStack stack) {
        return getRelicTemplate(entity, stack).getLeveling();
    }

    /**
     * Returns the {@link LevelingSourcesTemplate} from the contextual leveling configuration.
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @return the contextual {@link LevelingSourcesTemplate}
     */
    default LevelingSourcesTemplate getLevelingSourcesTemplate(LivingEntity entity, ItemStack stack) {
        return getLevelingTemplate(entity, stack).getSources();
    }

    /**
     * Returns the {@link LevelingSourceTemplate} for a specific source from the contextual configuration.
     *
     * @param entity the holder of the item
     * @param stack  the item stack instance
     * @param source the ID of the leveling source
     * @return the contextual {@link LevelingSourceTemplate}, or {@code null} if not found
     */
    default LevelingSourceTemplate getLevelingSourceTemplate(LivingEntity entity, ItemStack stack, String source) {
        return getLevelingSourcesTemplate(entity, stack).getSources().get(source);
    }
}