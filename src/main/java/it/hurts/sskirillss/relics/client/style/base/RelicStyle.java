package it.hurts.sskirillss.relics.client.style.base;

import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RelicStyle {
    @Nullable
    public OctoColor getTopTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return null;
    }

    @Nullable
    public OctoColor getBottomTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return null;
    }

    @Nullable
    public OctoColor getTopTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return null;
    }

    @Nullable
    public OctoColor getBottomTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return null;
    }

    public List<OctoColor> getItemNameColors(LivingEntity entity, ItemStack stack) {
        return new ArrayList<>();
    }

    @Nullable
    public ResourceLocation getTooltipFrameTexture(LivingEntity entity, ItemStack stack) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/tooltip/frame/" + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "/frame.png");
    }

    @Nullable
    public ResourceLocation getTooltipStarTexture(LivingEntity entity, ItemStack stack) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/tooltip/frame/" + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "/star.png");
    }

    @Nullable
    public OctoColor getFlawlessStartColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xFFFFFF00);
    }

    @Nullable
    public OctoColor getFlawlessEndColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0x00FF0000);
    }
}