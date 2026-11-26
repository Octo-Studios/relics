package it.hurts.sskirillss.relics.client.screen.description.misc;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    private static final ResourceLocation TOOLTIP = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/tooltip.png");

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

    @OnlyIn(Dist.CLIENT)
    public static void drawActionTooltip(GuiGraphics graphics, Font font, List<MutableComponent> entries, int x, int mouseY) {
        var lines = new ArrayList<FormattedCharSequence>();
        var maxWidth = 150;
        var width = 0;

        for (var entry : entries) {
            width = Math.max(width, Math.min((font.width(entry) + 4) / 2, maxWidth));

            lines.addAll(font.split(entry, maxWidth * 2));
        }

        var height = lines.size() * 5;
        var y = mouseY - height / 2 - 9;

        drawTooltipBackground(graphics, width, height, x, y);

        var pose = graphics.pose();

        pose.pushPose();
        pose.scale(0.5F, 0.5F, 0.5F);

        for (var line : lines) {
            graphics.drawString(font, line, (x + 10) * 2, (y + 9) * 2, TEXT_COLOR, false);

            y += 5;
        }

        pose.popPose();
    }

    public static AbstractContainerMenu getContainerMenu(Player player) {
        return player.containerMenu.containerId == player.inventoryMenu.containerId ? player.inventoryMenu : player.containerMenu;
    }

    public static ItemStack gatherRelicStack(Player player, int slot) {
        var source = getRelicSlot(getContainerMenu(player), slot);

        return source == null ? ItemStack.EMPTY : source.getItem();
    }

    @Nullable
    public static Slot getActionSlot(Player player, int container, int slot) {
        var menu = getContainerMenu(player);

        if (menu.containerId != container || player.isSpectator() || !menu.stillValid(player))
            return null;

        var source = getRelicSlot(menu, slot);

        return source != null && source.isActive() && !source.isFake() ? source : null;
    }

    @Nullable
    public static Slot getRemovableRelicSlot(Player player, int container, int slot) {
        var source = getActionSlot(player, container, slot);

        if (source == null || !source.mayPickup(player))
            return null;

        return source.getItem().getCount() == 1 || source.allowModification(player) ? source : null;
    }

    @Nullable
    public static Slot getEditableRelicSlot(Player player, int container, int slot) {
        var source = getActionSlot(player, container, slot);

        return source != null && source.allowModification(player) ? source : null;
    }

    @Nullable
    private static Slot getRelicSlot(AbstractContainerMenu menu, int index) {
        if (index < 0 || index >= menu.slots.size())
            return null;

        var slot = menu.getSlot(index);

        return slot.getItem().getItem() instanceof IRelicItem ? slot : null;
    }
}
