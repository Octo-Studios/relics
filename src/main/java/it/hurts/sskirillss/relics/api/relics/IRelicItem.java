package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.data.RelicData;
import it.hurts.sskirillss.relics.config.data.RelicConfigData;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Override
    default RelicTemplate getRelicTemplate(LivingEntity entity, ItemStack stack) {
        var base = IRelicTemplateHolder.super.getRelicTemplate(entity, stack);
        var rank = getRelicData(entity, stack).getLevelingData().getRank();

        var abilities = base.getAbilities();

        var updatedAbilitiesMap = abilities.getAbilities().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                            var template = entry.getValue();

                            var updatedMax = IntStream.range(0, rank)
                                    .reduce(template.getInitialMaxLevel(), (level, i) -> level + (int) Math.ceil(level * template.getMaxLevelRankModifier()));

                            return template.toBuilder()
                                    .initialMaxLevel(updatedMax)
                                    .build();
                        }
                ));

        var abilitiesBuilder = abilities.toBuilder();

        updatedAbilitiesMap.forEach((key, template) -> abilitiesBuilder.ability(template.toBuilder()
                .initialMaxLevel(template.getInitialMaxLevel())
                .build()
        ));

        return base.toBuilder()
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
