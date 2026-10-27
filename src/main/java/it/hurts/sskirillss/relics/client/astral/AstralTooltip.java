package it.hurts.sskirillss.relics.client.astral;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class AstralTooltip {
    private static final Minecraft MC = Minecraft.getInstance();

    public static void render(GuiGraphics graphics) {
        var strength = AstralSpace.getLabelStrength();
        var stack = AstralSpace.getLabelStack();

        if (!AstralSpace.isActive() || MC.player == null || MC.options.hideGui || stack.isEmpty() || strength <= 0.02F)
            return;

        var ease = strength * strength * (3F - 2F * strength);
        var scale = 0.5F + 0.25F * ease;
        var x = AstralSpace.getLabelX();
        var y = AstralSpace.getLabelY();
        var width = Screen.getTooltipFromItem(MC, stack).stream().mapToInt(MC.font::width).max().orElse(0);
        var right = x + (12 + width) * scale < graphics.guiWidth() - 4;
        var pose = graphics.pose();

        pose.pushPose();
        pose.translate(x, y, 0F);
        pose.scale(scale, scale, 1F);

        graphics.renderTooltip(MC.font, stack, right ? 0 : -24 - width, 0);

        pose.popPose();
    }
}
