package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class FlawlessUtils {
    public static Color getColor(LivingEntity entity, ItemStack stack, Color color) {
        if (!(stack.getItem() instanceof IRelicItem relic) || !relic.isRelicFlawless(entity, stack))
            return color;

        var hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        var goldenHue = 0.125f;

        var saturation = Math.min(1f, hsb[1] * 0.8f + 0.2f);
        var brightness = Math.min(1f, hsb[2] * 0.9f + 0.1f);

        return Color.getHSBColor(goldenHue, saturation, brightness);
    }

    public static ResourceLocation getTexture(LivingEntity entity, ItemStack stack, ResourceLocation location) {
        var isFlawless = ((IRelicItem) stack.getItem()).isRelicFlawless(entity, stack);

        return isFlawless ? ResourceLocation.parse(location.toString().replaceFirst("\\.png$", "_flawless.png")) : location;
    }
}