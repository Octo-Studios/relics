package it.hurts.sskirillss.relics.badges.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@AllArgsConstructor
public abstract sealed class AbstractBadge permits AbilityBadge, RelicBadge {
    @Getter
    private final String id;

    public abstract ResourceLocation getIconTexture(LivingEntity entity, ItemStack stack);

    public abstract ResourceLocation getOutlineTexture(LivingEntity entity, ItemStack stack);
}