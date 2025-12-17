package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class FlawlessUtils {
    public static int getColor(boolean flawless, int color) {
        var c = new Color(color, (color >>> 24) != 0);

        var res = getColor(flawless, c);

        return (c.getAlpha() << 24) | (res.getRed() << 16) | (res.getGreen() << 8) | res.getBlue();
    }

    public static int getColor(LivingEntity entity, ItemStack stack, int color) {
        return getColor(stack.getItem() instanceof IRelicItem relic && relic.isRelicFlawless(entity, stack), color);
    }

    public static Color getColor(boolean flawless, Color color) {
        if (!flawless)
            return color;

        var hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        var baseHue = 0.125f;
        var minHue = 0.10f;
        var maxHue = 0.16f;

        var blended = baseHue + (hsb[0] - baseHue) * 0.25f;
        var offset = (hsb[0] - baseHue) * 0.10f;
        var goldenHue = blended + offset;

        if (hsb[0] > 0.25f && hsb[0] < 0.45f)
            goldenHue -= 0.01f;

        goldenHue = Math.max(minHue, Math.min(maxHue, goldenHue));

        var saturation = Math.min(1f, Math.max(0f, 0.55f + 0.35f * hsb[1]));
        var brightness = Math.min(1f, Math.max(0f, 0.72f + 0.28f * hsb[2]));

        return Color.getHSBColor(goldenHue, saturation, brightness);
    }

    public static Color getColor(LivingEntity entity, ItemStack stack, Color color) {
        return getColor(stack.getItem() instanceof IRelicItem relic && relic.isRelicFlawless(entity, stack), color);
    }

    public static ResourceLocation getTexture(boolean flawless, ResourceLocation location) {
        return flawless ? ResourceLocation.parse(location.toString().replaceFirst("\\.png$", "_flawless.png")) : location;
    }

    public static ResourceLocation getTexture(LivingEntity entity, ItemStack stack, ResourceLocation location) {
        return getTexture(stack.getItem() instanceof IRelicItem relic && relic.isRelicFlawless(entity, stack), location);
    }
}