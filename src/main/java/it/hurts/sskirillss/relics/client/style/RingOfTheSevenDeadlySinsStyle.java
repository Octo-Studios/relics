package it.hurts.sskirillss.relics.client.style;

import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class RingOfTheSevenDeadlySinsStyle extends RelicStyle {
    @Override
    public OctoColor getTopTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff451f1c);
    }

    @Override
    public OctoColor getBottomTooltipBorderColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xff451f1c);
    }

    @Override
    public OctoColor getTopTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf0120000);
    }

    @Override
    public OctoColor getBottomTooltipBackgroundColor(LivingEntity entity, ItemStack stack) {
        return new OctoColor(0xf0210311);
    }

    @Override
    public List<OctoColor> getItemNameColors(LivingEntity entity, ItemStack stack) {
        return Arrays.asList(new OctoColor(0xFFb61033), new OctoColor(0xFF771757));
    }
}