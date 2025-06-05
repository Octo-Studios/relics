package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.octostudios.octolib.client.animator.Animator;
import it.hurts.octostudios.octolib.client.animator.Easing;
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
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;

public class TabWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    @Getter
    private IRelicScreenProvider source;

    @Getter
    private IRelicScreenProvider target;

    @Getter
    private DescriptionTab tab;

    @Getter
    @Setter
    private double xOffset, yOffset;

    public Animator hoverAnimator = new Animator(Easing.EASE_OUT_QUINT, 0, 100, 1.75, this::setYOffset);

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
        var player = minecraft.player;

        poseStack.pushPose();

        guiGraphics.pose().translate(this.getXOffset(), this.getYOffset(), 0);

        if (isLocked()) {
            GUIScissors.begin(getX(), getY(), width, getHeight());

            GUIRenderer.begin(DescriptionTextures.TAB, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX(), getY() + 11)
                    .end();

            GUIScissors.end();
        } else {
            GUIRenderer.begin(DescriptionTextures.TAB, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX(), getY())
                    .end();

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/tabs/" + tab.name().toLowerCase(Locale.ROOT) + "_shadow" + ".png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX(), getY())
                    .end();

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/tabs/" + tab.name().toLowerCase(Locale.ROOT) + "_icon" + ".png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX(), getY())
                    .end();

            if (isHovered())
                GUIRenderer.begin(DescriptionTextures.TAB_OUTLINE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() - 1, getY() - 1)
                        .end();
        }

        poseStack.popPose();
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
//        FIXME
//        if (!(this.hoverAnimator.isRunning() || this.hoverAnimator.isFinished())) {
//            this.hoverAnimator.start();
//        }

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
        if (!this.isHovered() && (this.hoverAnimator.isRunning() || this.hoverAnimator.isFinished())) {
            this.setXOffset(0);
            this.setYOffset(0);

            this.hoverAnimator.reset();
        }
    }
}