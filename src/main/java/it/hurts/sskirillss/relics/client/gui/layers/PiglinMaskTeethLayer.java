package it.hurts.sskirillss.relics.client.gui.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.head.PiglinMaskItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import it.hurts.sskirillss.relics.utils.data.SpriteMirror;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public class PiglinMaskTeethLayer implements LayeredDraw.Layer {
    private static final ResourceLocation TOOTH = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/layer/piglin_mask/tooth.png");
    private static final ResourceLocation GUM = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/layer/piglin_mask/gum.png");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var MC = Minecraft.getInstance();

        var player = MC.player;

        if (player == null)
            return;

        var stacks = 0;
        var duration = 0;
        var maxDuration = 0;

        for (var stack : EntityUtils.findEquippedCurios(player, RelicsItems.PIGLIN_MASK.get())) {
            var relic = (PiglinMaskItem) stack.getItem();

            if (relic.getStacks(stack) > stacks)
                stacks = relic.getStacks(stack);

            if (relic.getDuration(stack) > duration)
                duration = relic.getDuration(stack);

            if (relic.getMaxDuration(player, stack) > maxDuration)
                maxDuration = relic.getMaxDuration(player, stack);
        }

        if (stacks <= 0)
            return;

        var poseStack = guiGraphics.pose();
        var partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);

        var maxStacks = PiglinMaskItem.getMaxStacks();

        poseStack.pushPose();

        var window = MC.getWindow();

        RenderSystem.enableBlend();

        var maxHeight = window.getGuiScaledHeight();
        var maxWidth = window.getGuiScaledWidth();

        var height = 21;
        var width = 24;

        var yOff = 0;

        var progress = ((float) stacks / maxStacks);
        var alpha = stacks >= maxStacks ? (duration > maxDuration * 0.25F ? 1F : duration / (maxDuration * 0.25F)) : 1F;

        for (int i = 0; i < Math.ceil((double) maxHeight / height); i++) {
            poseStack.pushPose();

            poseStack.translate(-width + (width * progress), 0, 0);

            GUIRenderer.begin(GUM, poseStack)
                    .pos(0, yOff)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .alpha(alpha)
                    .end();

            if (stacks == maxStacks)
                poseStack.translate(Math.sin(player.tickCount + i + partialTicks) * 2, 0, 0);

            GUIRenderer.begin(TOOTH, poseStack)
                    .pos(0, yOff)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .alpha(alpha)
                    .end();

            poseStack.popPose();

            yOff += height;
        }

        yOff = 0;

        for (int i = 0; i < Math.ceil((double) maxHeight / height); i++) {
            poseStack.pushPose();

            poseStack.translate(width - (width * progress), 0, 0);

            GUIRenderer.begin(GUM, poseStack)
                    .pos(maxWidth - width, yOff)
                    .mirror(SpriteMirror.HORIZONTAL)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .alpha(alpha)
                    .end();

            if (stacks == maxStacks)
                poseStack.translate(Math.sin(player.tickCount + i + partialTicks) * 2, 0, 0);

            GUIRenderer.begin(TOOTH, poseStack)
                    .pos(maxWidth - width, yOff)
                    .mirror(SpriteMirror.HORIZONTAL)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .alpha(alpha)
                    .end();

            poseStack.popPose();

            yOff += height;
        }

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}