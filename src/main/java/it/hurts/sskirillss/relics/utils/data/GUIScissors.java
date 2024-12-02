package it.hurts.sskirillss.relics.utils.data;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

public class GUIScissors {
    public static void begin(int x, int y, int width, int height) {
        var window = Minecraft.getInstance().getWindow();

        float scaledWidth = window.getGuiScaledWidth();
        float scaledHeight = window.getGuiScaledHeight();

        int realWidth = window.getWidth();
        int realHeight = window.getHeight();

        int scissorX = (int) (realWidth * (x / scaledWidth));
        int scissorY = (int) (realHeight * (1 - (y + height) / scaledHeight)) - 2;
        int scissorWidth = (int) (realWidth * (width / scaledWidth));
        int scissorHeight = (int) (realHeight * (height / scaledHeight));

        RenderSystem.enableScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    public static void end() {
        RenderSystem.disableScissor();
    }
}