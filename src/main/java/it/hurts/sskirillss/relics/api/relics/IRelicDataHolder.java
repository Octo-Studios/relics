package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityExtenderComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatComponent;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Optional;

@ApiStatus.Internal
public interface IRelicDataHolder {
    default RelicComponent getRelicData(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.DATA, RelicComponent.EMPTY);
    }

    default void setRelicData(ItemStack stack, RelicComponent data) {
        stack.set(DataComponentRegistry.DATA, data);
    }

    default LevelingComponent getLevelingData(ItemStack stack) {
        return getRelicData(stack).getLeveling();
    }

    default void setLevelingData(ItemStack stack, LevelingComponent data) {
        setRelicData(stack, getRelicData(stack).toBuilder().leveling(data).build());
    }

    default AbilitiesComponent getAbilitiesComponent(ItemStack stack) {
        return getRelicData(stack).getAbilities();
    }

    default void setAbilitiesComponent(ItemStack stack, AbilitiesComponent data) {
        setRelicData(stack, getRelicData(stack).toBuilder().abilities(data).build());
    }

    @Nullable
    default AbilityComponent getAbilityComponent(LivingEntity entity, ItemStack stack, String ability) {
        if (!(stack.getItem() instanceof IRelicTemplateHolder templateHolder))
            return null;

        var abilitiesComponent = getAbilitiesComponent(stack);
        var abilityComponent = abilitiesComponent.getAbilities().get(ability);
        var abilityTemplate = templateHolder.getAbilityTemplate(entity, stack, ability);

        if (abilityComponent != null)
            return abilityComponent;
        else if (abilityTemplate != null) {
            abilityComponent = AbilityComponent.EMPTY;

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

    @Nullable
    default StatComponent getStatComponent(LivingEntity entity, ItemStack stack, String ability, String stat) {
        var abilityComponent = getAbilityComponent(entity, stack, ability);
        var statComponent = getStatComponent(entity, stack, ability, stat);
        var statData = getStatTemplate(entity, stack, ability, stat);

        if (statComponent != null)
            return statComponent;
        else if (statData != null) {
            statComponent = StatComponent.EMPTY;

            setAbilityComponent(stack, ability, abilityComponent.toBuilder()
                    .stat(stat, statComponent)
                    .build());

            return statComponent;
        } else
            return null;
    }

    default void setStatComponent(LivingEntity entity, ItemStack stack, String ability, String stat, StatComponent component) {
        setAbilityComponent(stack, ability, getAbilityComponent(entity, stack, ability).toBuilder()
                .stat(stat, component)
                .build());
    }

    default int getStatInitialQuality(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return getStatComponent(entity, stack, ability, stat).getInitialQuality();
    }

    default void setStatInitialQuality(LivingEntity entity, ItemStack stack, String ability, String stat, int quality) {
        setStatComponent(entity, stack, ability, stat, getStatComponent(entity, stack, ability, stat).toBuilder()
                .initialQuality(Math.clamp(quality, 0, getStatMaxQuality(entity, stack, ability, stat)))
                .build());
    }

    default void addStatInitialQuality(LivingEntity entity, ItemStack stack, String ability, String stat, int quality) {
        setStatOverrideValue(entity, stack, ability, stat, getStatInitialQuality(entity, stack, ability, stat) + quality);
    }

    default Optional<Double> getStatOverrideValue(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return getStatComponent(entity, stack, ability, stat).getOverrideValue();
    }

    default void setStatOverrideValue(LivingEntity entity, ItemStack stack, String ability, String stat, double value) {
        setStatComponent(entity, stack, ability, stat, getStatComponent(entity, stack, ability, stat).toBuilder()
                .overrideValue(Optional.of(value))
                .build());
    }

    default void addStatOverrideValue(LivingEntity entity, ItemStack stack, String ability, String stat, double value) {
        setStatOverrideValue(entity, stack, ability, stat, getStatOverrideValue(entity, stack, ability, stat).orElse(0D) + value);
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
}