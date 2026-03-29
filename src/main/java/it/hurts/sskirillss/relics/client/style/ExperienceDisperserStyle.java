package it.hurts.sskirillss.relics.client.style;

import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ExperienceDisperserStyle extends RelicStyle {
    @Override
    public OctoColor getTopTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff714f40);
    }

    @Override
    public OctoColor getBottomTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff4a2c1e);
    }

    @Override
    public OctoColor getTopTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf0190e06);
    }

    @Override
    public OctoColor getBottomTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf00e0805);
    }

    @Override
    public List<OctoColor> getItemNameColors(LivingEntity entity, ItemStack stack) {
        return Arrays.asList(new OctoColor(0xFFdbdbdb), new OctoColor(0xFFb9b2b2));
    }
}