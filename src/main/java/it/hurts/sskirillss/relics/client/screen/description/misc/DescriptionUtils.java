package it.hurts.sskirillss.relics.client.screen.description.misc;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionCategories;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.experience.ExperienceDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.*;

public class DescriptionUtils {
    public static final int TEXT_COLOR = 0x732f20;

    public static int POSITIVE_COLOR(boolean flicker) {
        var color = 0x228B22;

        return flicker ? DescriptionUtils.CUSTOM_COLOR(color) : color;
    }

    public static int NEUTRAL_COLOR(boolean flicker) {
        var color = 0xFF8C00;

        return flicker ? DescriptionUtils.CUSTOM_COLOR(color) : color;
    }

    public static int NEGATIVE_COLOR(boolean flicker) {
        var color = 0xB22222;

        return flicker ? DescriptionUtils.CUSTOM_COLOR(color) : color;
    }

    public static int CUSTOM_COLOR(int color) {
        return DescriptionUtils.CUSTOM_COLOR(color, 0.001D, 0.75D);
    }

    public static int CUSTOM_COLOR(int color, double animationSpeed, double oscillationFrequency) {
        float oscillation = 0.1F * (float) Math.sin(2 * Math.PI * oscillationFrequency * System.currentTimeMillis() * animationSpeed);
        float brightnessLevel = (float) oscillationFrequency + oscillation;

        float[] hsbValues = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);

        hsbValues[2] = Mth.clamp(brightnessLevel, 0F, 1F);

        return Color.HSBtoRGB(hsbValues[0], hsbValues[1], hsbValues[2]);
    }

    private static final ResourceLocation TOOLTIP = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/tooltip.png");

    @OnlyIn(Dist.CLIENT)
    public static void drawTooltipBackground(GuiGraphics guiGraphics, int width, int height, int x, int y) {
        int texWidth = 12;
        int texHeight = 15;

        int xStep = 0;

        for (int i = 0; i < 2; i++) {
            guiGraphics.blit(TOOLTIP, x + xStep, y, 9, 7, 0, 0, 9, 7, texWidth, texHeight);
            guiGraphics.blit(TOOLTIP, x + xStep, y + 7, 9, height + 4, 0, 7, 9, 1, texWidth, texHeight);
            guiGraphics.blit(TOOLTIP, x + xStep, y + height + 9, 9, 7, 0, 8, 9, 7, texWidth, texHeight);

            xStep += width + 9;
        }

        guiGraphics.blit(TOOLTIP, x + 7, y + 5, 1, height + 6, 9, 0, 1, 1, texWidth, texHeight);
        guiGraphics.blit(TOOLTIP, x + width + 10, y + 5, 1, height + 6, 10, 0, 1, 1, texWidth, texHeight);

        guiGraphics.blit(TOOLTIP, x + 8, y + 5, width + 2, 3, 11, 0, 1, 3, texWidth, texHeight);
        guiGraphics.blit(TOOLTIP, x + 8, y + 8, width + 2, height + 1, 11, 3, 1, 1, texWidth, texHeight);
        guiGraphics.blit(TOOLTIP, x + 8, y + height + 9, width + 2, 3, 11, 4, 1, 3, texWidth, texHeight);
    }

    public static ItemStack gatherRelicStack(Player player, int slot) {
        if (!player.containerMenu.isValidSlotIndex(slot))
            return ItemStack.EMPTY;

        var stack = player.containerMenu.getSlot(slot).getItem();

        if (!(stack.getItem() instanceof IRelicItem))
            return ItemStack.EMPTY;

        return stack;
    }
}