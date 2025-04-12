package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.components.*;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

@ApiStatus.Internal
public interface IRelicDataHolder {
    default DataComponent getRelicData(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.DATA, DataComponent.EMPTY);
    }

    default void setRelicData(ItemStack stack, DataComponent data) {
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
}