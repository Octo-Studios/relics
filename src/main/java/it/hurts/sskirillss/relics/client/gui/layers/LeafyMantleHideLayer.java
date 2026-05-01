package it.hurts.sskirillss.relics.client.gui.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.back.LeafyMantleItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import it.hurts.sskirillss.relics.utils.data.SpriteMirror;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class LeafyMantleHideLayer implements LayeredDraw.Layer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/leafy_mantle_hide.png");
    private static final ResourceLocation VIGNETTE = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var MC = Minecraft.getInstance();
        var player = MC.player;

        if (player == null)
            return;

        var stack = EntityUtils.findEquippedCurio(player, RelicsItems.LEAFY_MANTLE.get());

        if (!(stack.getItem() instanceof LeafyMantleItem relic) || !relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("camouflage").getRankModifierData("disappearance").isEnabled())
            return;

        var progress = relic.getCurrentProgress(stack);

        if (progress <= 0)
            return;

        var maxProgress = relic.getMaxProgress();
        var partialTick = RenderUtils.getPartialTick(false);

        var progressRatio = (float) TransitionType.QUAD.apply(EaseType.EASE_IN_OUT, Math.min(1F, (progress + (partialTick * (relic.isHiding(stack) ? 1 : -1))) / maxProgress));

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        var window = MC.getWindow();
        var width = window.getGuiScaledWidth();
        var height = window.getGuiScaledHeight();

        var texWidth = 89;
        var texHeight = 62;

        var alpha = progressRatio * 0.75F;

        var offsetX = texWidth * (1 - progressRatio);
        var offsetY = texHeight * (1 - progressRatio);

        var color = new Color(BiomeColors.getAverageGrassColor(player.level(), player.blockPosition())).brighter();

        RenderSystem.enableBlend();

        GUIRenderer.begin(VIGNETTE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .patternSize(width, height)
                .texSize(width, height)
                .color(color)
                .alpha(alpha * 0.25F)
                .end();

        GUIRenderer.begin(TEXTURE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(-offsetX, -offsetY)
                .color(color)
                .alpha(alpha)
                .scale(2)
                .end();

        GUIRenderer.begin(TEXTURE, poseStack)
                .pos(width + offsetX, -offsetY)
                .mirror(SpriteMirror.HORIZONTAL)
                .anchor(SpriteAnchor.TOP_RIGHT)
                .color(color)
                .alpha(alpha)
                .scale(2)
                .end();

        GUIRenderer.begin(TEXTURE, poseStack)
                .pos(-offsetX, height + offsetY)
                .anchor(SpriteAnchor.BOTTOM_LEFT)
                .mirror(SpriteMirror.VERTICAL)
                .color(color)
                .alpha(alpha)
                .scale(2)
                .end();

        GUIRenderer.begin(TEXTURE, poseStack)
                .pos(width + offsetX, height + offsetY)
                .mirror(SpriteMirror.HORIZONTAL, SpriteMirror.VERTICAL)
                .anchor(SpriteAnchor.BOTTOM_RIGHT)
                .color(color)
                .alpha(alpha)
                .scale(2)
                .end();

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}
