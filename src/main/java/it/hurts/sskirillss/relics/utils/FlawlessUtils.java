package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class FlawlessUtils {
    public static Color getColor(LivingEntity entity, ItemStack stack, Color color) {
        return color;
    }

    public static ResourceLocation getTexture(LivingEntity entity, ItemStack stack, ResourceLocation location) {
        var isFlawless = ((IRelicItem) stack.getItem()).isRelicFlawless(entity, stack);

        return isFlawless ? ResourceLocation.parse(location.toString().replaceFirst("\\.png$", "_flawless.png")) : location;
    }
}