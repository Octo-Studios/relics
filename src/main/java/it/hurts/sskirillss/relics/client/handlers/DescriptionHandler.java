package it.hurts.sskirillss.relics.client.handlers;

import com.mojang.blaze3d.platform.Window;
import it.hurts.sskirillss.relics.api.events.common.TooltipDisplayEvent;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.experience.ExperienceDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionCache;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class DescriptionHandler {
    private static final int REQUIRED_TIME = 10;

    private static int ticksCount;

    private static int width;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide())
            return;

        boolean hasShiftDown = Screen.hasShiftDown();

        if (ticksCount > 0 && !hasShiftDown)
            ticksCount--;

        Minecraft MC = Minecraft.getInstance();

        if (!(MC.screen instanceof AbstractContainerScreen<? extends AbstractContainerMenu> screen))
            return;

        Window window = MC.getWindow();

        double mouseX = MC.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
        double mouseY = MC.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();

        AbstractContainerMenu menu = player.containerMenu;

        Slot slot = null;
        int id = 0;

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot entry = menu.slots.get(i);

            if (isHovering(screen.getGuiLeft(), screen.getGuiTop(), entry.x, entry.y, mouseX, mouseY)) {
                slot = entry;
                id = i;

                break;
            }
        }

        if (slot == null)
            return;

        ItemStack stack = slot.getItem();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        if (hasShiftDown) {
            ticksCount++;

            if (ticksCount >= REQUIRED_TIME) {
                Screen descriptionScreen;

                switch (DescriptionCache.getEntry(relic).getSelectedPage()) {
                    case ABILITY -> descriptionScreen = new AbilityDescriptionScreen(player, player.containerMenu.containerId, id, screen);
                    case EXPERIENCE -> descriptionScreen = new ExperienceDescriptionScreen(player, player.containerMenu.containerId, id, screen);
                    default -> descriptionScreen = new RelicDescriptionScreen(player, player.containerMenu.containerId, id, screen);
                }

                Minecraft.getInstance().setScreen(descriptionScreen);

                ticksCount = 0;
            }
        }
    }

    protected static boolean isHovering(int leftPos, int topPos, int slotX, int slotY, double mouseX, double mouseY) {
        mouseX -= leftPos;
        mouseY -= topPos;

        return mouseX >= (slotX - 1) && mouseX < (slotX + 16 + 1) && mouseY >= (slotY - 1) && mouseY < (slotY + 16 + 1);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof IRelicItem))
            return;

        var filler = "|";

        event.getToolTip().add(drawProgressBar(filler.repeat(width / Minecraft.getInstance().font.width(filler))));
    }

    public static MutableComponent drawProgressBar(String style) {
        StringBuilder string = new StringBuilder(style);
        float percentage = (float) ticksCount / REQUIRED_TIME;
        int offset = (int) Math.min(string.length(), Math.floor(string.length() * percentage));

        MutableComponent component = Component.literal("");
        String start = string.substring(0, offset);

        int startColor = 0x180133;
        int endColor = 0x2c0863;

        for (int i = 0; i < offset; i++) {
            float fraction = (float) i / (offset - 1);

            int r = (int) ((1 - fraction) * ((startColor >> 16) & 0xFF) + fraction * ((endColor >> 16) & 0xFF));
            int g = (int) ((1 - fraction) * ((startColor >> 8) & 0xFF) + fraction * ((endColor >> 8) & 0xFF));
            int b = (int) ((1 - fraction) * (startColor & 0xFF) + fraction * (endColor & 0xFF));

            int color = (r << 16) | (g << 8) | b;

            component.append(Component.literal(String.valueOf(start.charAt(i))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }

        component.append(Component.literal(string.substring(offset)).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));

        return component;
    }

    @SubscribeEvent
    public static void onTooltipDisplay(TooltipDisplayEvent event) {
        if (!(event.getStack().getItem() instanceof IRelicItem))
            return;

        width = event.getWidth();
    }
}