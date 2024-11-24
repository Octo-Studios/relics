package it.hurts.sskirillss.relics.client.screen.description.misc;

import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DescriptionUtils {
    public static final int TEXT_COLOR = 0x662F13;
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

        ItemStack stack = player.containerMenu.getSlot(slot).getItem();

        if (!(stack.getItem() instanceof IRelicItem))
            return ItemStack.EMPTY;

        return stack;
    }
}