package it.hurts.sskirillss.relics.client.style;

import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class RiderFluteStyle extends RelicStyle {
    @Override
    public OctoColor getTopTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff3c1230);
    }

    @Override
    public OctoColor getBottomTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff544d20);
    }

    @Override
    public OctoColor getTopTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf0190312);
    }

    @Override
    public OctoColor getBottomTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf0130c01);
    }

    @Override
    public List<OctoColor> getItemNameColors(LivingEntity entity, ItemStack stack) {
        return Arrays.asList(new OctoColor(0xFFb345cb), new OctoColor(0xFFe4eb64));
    }
}