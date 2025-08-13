package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.opengl.GL11;

import java.util.List;

public abstract class BookmarkWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    @Getter
    private DescriptionScreen screen;

    @Setter
    public double slideProgress = 0D;
    @Setter
    public double animationProgress = 0D;

    public Tween hoverTween;
    private boolean hasHovered = false;

    public BookmarkWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, 16, 20);

        this.screen = screen;
    }

    public abstract String getId();

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();
        var player = this.minecraft.player;

        GUIScissors.begin(this.getX() - 1, this.getY() - 3, this.getWidth() + 2, this.getHeight() + 3);

        poseStack.pushPose();

        poseStack.translate(getX() + (this.getWidth() / 2F), getY() + this.getHeight() + 1 - this.slideProgress, 0);

        if (this.isLocked()) {
            poseStack.translate(0, 10, 0);

            GUIRenderer.begin(this instanceof TabWidget ? DescriptionTextures.TAB : DescriptionTextures.PAGE, poseStack)
                    .anchor(SpriteAnchor.BOTTOM_CENTER)
                    .end();
        } else {
            var texWidth = 16;
            var patternHeight = 21;

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/bookmarks/" + this.getId() + ".png"), poseStack)
                    .texOff(0, (int) (Math.floor(this.getAnimationSegments() * animationProgress) * patternHeight))
                    .patternSize(texWidth, patternHeight)
                    .anchor(SpriteAnchor.BOTTOM_CENTER)
                    .end();

            if (this.isHovered())
                GUIRenderer.begin(this instanceof TabWidget ? DescriptionTextures.TAB_OUTLINE : DescriptionTextures.PAGE_OUTLINE, poseStack)
                        .anchor(SpriteAnchor.TOP_CENTER)
                        .pos(0, -(this.getHeight() + 2))
                        .end();
        }

        poseStack.popPose();

        GUIScissors.end();
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!this.isLocked())
            super.playDownSound(handler);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 100;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.translatable("relics.description.researching.bookmarks." + this.getId()).withStyle(ChatFormatting.BOLD)
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

            hoverTween.tweenMethod(this::setSlideProgress, 0D, 1D, 0.075D).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.QUART);
            hoverTween.tweenMethod(this::setAnimationProgress, 0D, 1D, animationDuration * (1 - animationProgress)).setTransitionType(TransitionType.LINEAR);

            hoverTween.start();
        } else if (!hovered && hasHovered) {
            hasHovered = false;

            if (hoverTween != null)
                hoverTween.kill();

            hoverTween = Tween.create().setParallel(true);

            hoverTween.tweenMethod(this::setSlideProgress, 1D, 0D, 0.15D).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.QUART);
            hoverTween.tweenMethod(this::setAnimationProgress, 1D, 0D, animationDuration * animationProgress).setTransitionType(TransitionType.LINEAR);

            hoverTween.start();
        }
    }

    private int getAnimationSegments() {
        Minecraft.getInstance().getTextureManager().getTexture(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/bookmarks/" + this.getId() + ".png")).bind();

        var texHeight = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        var patternHeight = 21;

        return (texHeight / patternHeight) - 1;
    }
}