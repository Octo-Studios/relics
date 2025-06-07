package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.ITabbedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionTab;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Locale;

public class TabWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    @Getter
    private IRelicScreenProvider source;

    @Getter
    private IRelicScreenProvider target;

    @Getter
    private DescriptionTab tab;

    public double slideProgress = 0D;
    public double animationProgress = 0D;

    public Tween hoverTween;
    private boolean hasHovered = false;

    public TabWidget(int x, int y, IRelicScreenProvider source, DescriptionTab tab, IRelicScreenProvider target) {
        super(x, y, 16, 20);

        this.source = source;
        this.target = target;
        this.tab = tab;
    }

    @Override
    public void onPress() {
        if (isLocked())
            return;

        minecraft.setScreen((Screen) target);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();
        var player = this.minecraft.player;

        GUIScissors.begin(this.getX() - 1, this.getY() - 3, this.getWidth() + 2, this.getHeight() + 3);

        poseStack.pushPose();

        poseStack.translate(getX() + (this.getWidth() / 2F), getY() + this.getHeight() + 1 - this.slideProgress, 0);

        if (isLocked()) {
            poseStack.translate(0, 10, 0);

            GUIRenderer.begin(DescriptionTextures.TAB, poseStack)
                    .anchor(SpriteAnchor.BOTTOM_CENTER)
                    .end();
        } else {
            var texWidth = 16;
            var patternHeight = 21;

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/tabs/" + tab.name().toLowerCase(Locale.ROOT) + "_tab" + ".png"), poseStack)
                    .texOff(0, (int) (Math.floor(this.getAnimationSegments() * animationProgress) * patternHeight))
                    .patternSize(texWidth, patternHeight)
                    .anchor(SpriteAnchor.BOTTOM_CENTER)
                    .end();

            if (this.isHovered())
                GUIRenderer.begin(DescriptionTextures.TAB_OUTLINE, poseStack)
                        .anchor(SpriteAnchor.TOP_CENTER)
                        .pos(0, -(this.getHeight() + 2))
                        .end();
        }

        poseStack.popPose();

        GUIScissors.end();
    }

    @Override
    public boolean isLocked() {
        return minecraft.screen instanceof ITabbedDescriptionScreen screen && screen.getTab() == tab;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked())
            super.playDownSound(handler);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 100;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.translatable("tooltip.relics.researching.tab." + tab.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.BOLD)
        );

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry) + 4) / 2;

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        poseStack.pushPose();

        poseStack.translate(0F, 0F, 100);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, mouseX - 9 - (renderWidth / 2), mouseY);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        int yOff = 0;

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, ((mouseX - renderWidth / 2) + 1) * 2, ((mouseY + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;
        }

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        var hovered = this.isHovered();
        var animationDuration = (this.getAnimationSegments() * 0.5F) / 20F;

        if (hovered && !hasHovered) {
            hasHovered = true;

            if (hoverTween != null)
                hoverTween.kill();

            hoverTween = Tween.create().setParallel(true);

            hoverTween.tweenProperty(this, "slideProgress", 1, 0.075).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.QUART);
            hoverTween.tweenProperty(this, "animationProgress", 1, animationDuration * (1 - animationProgress)).setTransitionType(TransitionType.LINEAR);
        } else if (!hovered && hasHovered) {
            hasHovered = false;

            if (hoverTween != null)
                hoverTween.kill();

            hoverTween = Tween.create().setParallel(true);

            hoverTween.tweenProperty(this, "slideProgress", 0, 0.15).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.QUART);
            hoverTween.tweenProperty(this, "animationProgress", 0, animationDuration * animationProgress).setTransitionType(TransitionType.LINEAR);
        }
    }

    private int getAnimationSegments() {
        Minecraft.getInstance().getTextureManager().getTexture(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/tabs/" + tab.name().toLowerCase(Locale.ROOT) + "_tab" + ".png")).bind();

        var texHeight = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        var patternHeight = 21;

        return (texHeight / patternHeight) - 1;
    }
}