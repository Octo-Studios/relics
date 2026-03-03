package it.hurts.sskirillss.relics.client.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import it.hurts.sskirillss.relics.client.screen.description.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.HotkeyRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class DescriptionHandler {
    private static final int REQUIRED_TIME = 20;

    private static int ticksCount;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        if (!player.level().isClientSide())
            return;

        Minecraft MC = Minecraft.getInstance();

        int key = HotkeyRegistry.RESEARCH_RELIC.getKey().getValue();

        if (key == GLFW.GLFW_KEY_UNKNOWN)
            return;

        boolean isResearching = InputConstants.isKeyDown(MC.getWindow().getWindow(), key);;

        if (ticksCount > 0 && !isResearching)
            ticksCount--;

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

        if (!(stack.getItem() instanceof IRelicItem))
            return;

        if (isResearching) {
            ticksCount++;

            if (ticksCount >= REQUIRED_TIME) {
                RelicDescriptionScreen descriptionScreen = new RelicDescriptionScreen(player, player.containerMenu.containerId, id, Minecraft.getInstance().screen);

                descriptionScreen.stack = DescriptionUtils.gatherRelicStack(player, id);

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

        event.getToolTip().add(drawProgressBar("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||"));
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
}