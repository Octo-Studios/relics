package it.hurts.sskirillss.relics.client.astral;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class AstralTooltip {
    private static final Minecraft MC = Minecraft.getInstance();

    public static String format(int amount) {
        if (amount >= 1_000_000_000)
            return String.format("%.1fB", amount / 1.0E9D);

        if (amount >= 1_000_000)
            return String.format("%.1fM", amount / 1.0E6D);

        if (amount >= 10_000)
            return String.format("%.0fK", amount / 1.0E3D);

        if (amount >= 1_000)
            return String.format("%.1fK", amount / 1.0E3D);

        return String.valueOf(amount);
    }

    public static void render(GuiGraphics graphics) {
        var strength = AstralSpace.getLabelStrength();
        var stack = AstralSpace.getLabelStack();

        if (!AstralSpace.isActive() || MC.player == null || MC.screen != null || MC.options.hideGui || stack.isEmpty() || strength <= 0.02F)
            return;

        var ease = strength * strength * (3F - 2F * strength);
        var scale = 0.5F + 0.25F * ease;
        var x = AstralSpace.getLabelX();
        var y = AstralSpace.getLabelY();
        var width = Screen.getTooltipFromItem(MC, stack).stream().mapToInt(MC.font::width).max().orElse(0);
        var gap = AstralSpace.getLabelRadius() / scale + 6F;
        var right = x + (gap + 12 + width) * scale < graphics.guiWidth() - 4;
        var pose = graphics.pose();

        pose.pushPose();
        pose.translate(x + (right ? gap : -(gap + 24 + width)) * scale, y, 0F);
        pose.scale(scale, scale, 1F);

        graphics.renderTooltip(MC.font, stack, 0, 0);

        pose.popPose();
    }
}
