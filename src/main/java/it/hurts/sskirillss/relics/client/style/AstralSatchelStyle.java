package it.hurts.sskirillss.relics.client.style;

import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class AstralSatchelStyle extends RelicStyle {
    @Override
    public OctoColor getTopTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff9a7bff);
    }

    @Override
    public OctoColor getBottomTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff4a3bc4);
    }

    @Override
    public OctoColor getTopTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf0150a2e);
    }

    @Override
    public OctoColor getBottomTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf0090418);
    }

    @Override
    public List<OctoColor> getItemNameColors(LivingEntity entity, ItemStack stack) {
        return Arrays.asList(new OctoColor(0xFFc9b3ff), new OctoColor(0xFF5a7bff));
    }
}
