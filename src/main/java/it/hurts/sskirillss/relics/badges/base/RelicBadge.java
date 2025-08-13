package it.hurts.sskirillss.relics.badges.base;

import it.hurts.sskirillss.relics.Relics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract non-sealed class RelicBadge extends AbstractBadge {
    public RelicBadge(String id) {
        super(id);
    }

    public MutableComponent getTitle(LivingEntity entity, ItemStack stack) {
        return Component.translatable("relics.description.researching.badge.relic." + getId() + ".title");
    }

    public List<MutableComponent> getDescription(LivingEntity entity, ItemStack stack) {
        return Arrays.asList(Component.translatable("relics.description.researching.badge.relic." + getId() + ".description"));
    }

    public List<MutableComponent> getHint(LivingEntity entity, ItemStack stack) {
        return new ArrayList<>();
    }

    public boolean isVisible(LivingEntity entity, ItemStack stack) {
        return false;
    }

    @Override
    public final ResourceLocation getIconTexture(LivingEntity entity, ItemStack stack) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/badges/relic/" + getId() + ".png");
    }

    @Override
    public final ResourceLocation getOutlineTexture(LivingEntity entity, ItemStack stack) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/badges/relic/" + getId() + "_outline.png");
    }
}