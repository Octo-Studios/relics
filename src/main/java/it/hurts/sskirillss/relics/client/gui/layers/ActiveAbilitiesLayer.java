package it.hurts.sskirillss.relics.client.gui.layers;

import it.hurts.sskirillss.relics.client.handlers.ActiveAbilitiesClientHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

public class ActiveAbilitiesLayer implements LayeredDraw.Layer {
    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ActiveAbilitiesClientHandler.render(guiGraphics, deltaTracker.getGameTimeDeltaPartialTick(true));
    }
}
