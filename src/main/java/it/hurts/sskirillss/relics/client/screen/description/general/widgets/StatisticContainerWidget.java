package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.DescriptionContainerWidget;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class StatisticContainerWidget extends DescriptionContainerWidget {
    public StatisticContainerWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, screen);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();

        if (stack == null || !(stack.getItem() instanceof IRelicItem relic) || player == null)
            return;

        var poseStack = guiGraphics.pose();

        GUIScissors.begin(getX(), getY(), getWidth(), getHeight());

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        var lineOffset = 10;
        var lines = this.getContent();

        var scroll = getScrollbar();

        if (scroll != null) {
            var offset = scroll.getScrollPosition(partialTick);

            poseStack.translate(0, -(offset * ((lines.size() - DescriptionContainerWidget.MAX_LINES) * (lineOffset))), 0);
        }

        var yOff = 0;

        for (FormattedCharSequence line : lines) {

            guiGraphics.drawString(minecraft.font, line, (this.getX() + 7) * 2, (this.getY() * 2) + yOff, DescriptionUtils.TEXT_COLOR, false);

            yOff += lineOffset;
        }

        poseStack.popPose();

        GUIScissors.end();
    }

    public abstract List<FormattedCharSequence> getContent();

    @Override
    public int getContentHeight() {
        return getContent().size() * 10;
    }
}