package it.hurts.sskirillss.relics.client.style;

import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class SphereOfSelfSacrificeStyle extends RelicStyle {
    @Override
    public OctoColor getTopTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff22101d);
    }

    @Override
    public OctoColor getBottomTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff1b1030);
    }

    @Override
    public OctoColor getTopTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf00d0a14);
    }

    @Override
    public OctoColor getBottomTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf010060d);
    }

    @Override
    public List<OctoColor> getItemNameColors(LivingEntity entity, ItemStack stack) {
        return Arrays.asList(new OctoColor(0xFFae1f3e), new OctoColor(0xFFa129cd));
    }
}