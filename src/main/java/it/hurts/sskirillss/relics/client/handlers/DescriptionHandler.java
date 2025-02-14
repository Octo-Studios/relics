package it.hurts.sskirillss.relics.client.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.HotkeyRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT)
public class DescriptionHandler {
    private static final int REQUIRED_TIME = 20;

    private static int ticksCountOld;
    private static int ticksCount;

    private static Slot slot;

    private static int width = 200;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player))
            return;

        ticksCountOld = ticksCount;

        var MC = Minecraft.getInstance();
        var key = HotkeyRegistry.RESEARCH_RELIC.getKey().getValue();

        if (key == GLFW.GLFW_KEY_UNKNOWN)
            return;

        var isResearching = InputConstants.isKeyDown(MC.getWindow().getWindow(), key);

        if (ticksCount > 0 && !isResearching)
            ticksCount--;

        if (!(MC.screen instanceof AbstractContainerScreen<? extends AbstractContainerMenu> screen))
            return;

        var window = MC.getWindow();

        var mouseX = MC.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
        var mouseY = MC.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();

        var menu = player.containerMenu;

        var oldId = slot == null ? -1 : slot.getContainerSlot();
        var id = -1;

        for (int i = 0; i < menu.slots.size(); i++) {
            var entry = menu.slots.get(i);

            if (isHovering(screen.getGuiLeft(), screen.getGuiTop(), entry.x, entry.y, mouseX, mouseY)) {
                slot = entry;
                id = i;

                break;
            }
        }

        if (slot == null || id == -1) {
            ticksCount = 0;
            ticksCountOld = 0;

            return;
        }

        var stack = slot.getItem();

        if (slot.getContainerSlot() != oldId) {
            ticksCount = 0;
            ticksCountOld = 0;
        }

        if (!(stack.getItem() instanceof IRelicItem relic) || !isResearching)
            return;

        ticksCount++;

        if (ticksCountOld >= REQUIRED_TIME) {
            DescriptionUtils.openCachedScreen(relic, player, id, screen);

            ticksCount = 0;
            ticksCountOld = 0;
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
        var string = new StringBuilder(style);
        var percentage = Mth.lerp(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true), ticksCountOld, ticksCount) / REQUIRED_TIME;
        var offset = (int) Math.min(string.length(), Math.floor(string.length() * percentage));

        var component = Component.literal("");
        var start = string.substring(0, offset);

        var startColor = 0x180133;
        var endColor = 0x2c0863;

        for (int i = 0; i < offset; i++) {
            var fraction = (float) i / (offset - 1);

            var r = (int) ((1 - fraction) * ((startColor >> 16) & 0xFF) + fraction * ((endColor >> 16) & 0xFF));
            var g = (int) ((1 - fraction) * ((startColor >> 8) & 0xFF) + fraction * ((endColor >> 8) & 0xFF));
            var b = (int) ((1 - fraction) * (startColor & 0xFF) + fraction * (endColor & 0xFF));

            var color = (r << 16) | (g << 8) | b;

            component.append(Component.literal(String.valueOf(start.charAt(i))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }

        component.append(Component.literal(string.substring(offset)).setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));

        return component;
    }

//    @SubscribeEvent
//    public static void onTooltipDisplay(TooltipDisplayEvent event) {
//        if (!(event.getStack().getItem() instanceof IRelicItem))
//            return;
//
//        width = event.getWidth();
//    }
}