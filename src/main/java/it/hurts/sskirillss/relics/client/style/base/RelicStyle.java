package it.hurts.sskirillss.relics.client.style.base;

import it.hurts.octostudios.octolib.util.OctoColor;
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
}