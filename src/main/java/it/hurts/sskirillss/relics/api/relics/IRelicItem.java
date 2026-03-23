package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.data.RelicData;
import it.hurts.sskirillss.relics.config.data.RelicConfigData;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface IRelicItem extends IRelicTemplateHolder {
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

    default RelicData getRelicData(@Nullable LivingEntity entity, ItemStack stack) {
        return new RelicData(this, entity, stack);
    }

    // TODO: Replace with relative integration
    @Deprecated(forRemoval = true)
    String getConfigRoute();

    // TODO: Probably remove?
    @Nullable
    default RelicConfigData constructDefaultConfigData(@NotNull RelicConfigData config) {
        return config;
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
}
