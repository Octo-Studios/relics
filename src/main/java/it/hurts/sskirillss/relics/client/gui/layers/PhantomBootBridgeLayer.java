package it.hurts.sskirillss.relics.client.gui.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.feet.PhantomBootItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public class PhantomBootBridgeLayer implements LayeredDraw.Layer {
    private static final ResourceLocation VIGNETTE = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var MC = Minecraft.getInstance();
        var player = MC.player;

        if (player == null)
            return;

        var stack = EntityUtils.findEquippedCurio(player, RelicsItems.PHANTOM_BOOT.get());

        if (!(stack.getItem() instanceof PhantomBootItem relic))
            return;

        var progress = relic.getTime(stack);

        if (progress <= 0)
            return;

        var maxProgress = relic.getMaxTime(player, stack);

        var partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        var progressRatio = Math.min(1F, (progress + (partialTick * (relic.isToggled(stack) ? 1 : -1))) / maxProgress);

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        var window = MC.getWindow();

        var width = window.getGuiScaledWidth();
        var height = window.getGuiScaledHeight();

        var alpha = progressRatio * 0.5F;

        RenderSystem.enableBlend();

        GUIRenderer.begin(VIGNETTE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .patternSize(width, height)
                .texSize(width, height)
                .color(0xb632bf)
                .alpha(alpha)
                .end();

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}