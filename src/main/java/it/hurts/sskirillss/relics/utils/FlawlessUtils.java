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

    public static ResourceLocation getTexture(LivingEntity entity, ItemStack stack, ResourceLocation location) {
        var isFlawless = ((IRelicItem) stack.getItem()).isRelicFlawless(entity, stack);

        return isFlawless ? ResourceLocation.parse(location.toString().replaceFirst("\\.png$", "_flawless.png")) : location;
    }
}