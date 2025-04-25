package it.hurts.sskirillss.relics.badges.base;

import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract non-sealed class AbilityBadge extends AbstractBadge {
    public AbilityBadge(String id) {
        super(id);
    }

    public MutableComponent getTitle(LivingEntity entity, ItemStack stack, String ability) {
        return Component.translatable("tooltip.relics.researching.badge.ability." + getId() + ".title");
    }

    public List<MutableComponent> getDescription(LivingEntity entity, ItemStack stack, String ability) {
        return Arrays.asList(Component.translatable("tooltip.relics.researching.badge.ability." + getId() + ".description"));
    }

    public List<MutableComponent> getHint(LivingEntity entity, ItemStack stack, String ability) {
        return new ArrayList<>();
    }

    public boolean isVisible(LivingEntity entity, ItemStack stack, String ability) {
        return false;
    }

    @Override
    public final ResourceLocation getIconTexture(LivingEntity entity, ItemStack stack) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/badges/ability/" + getId() + ".png");
    }

    @Override
    public final ResourceLocation getOutlineTexture(LivingEntity entity, ItemStack stack) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/badges/ability/" + getId() + "_outline.png");
    }
}