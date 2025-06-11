package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class RelicStatisticContainerWidget extends DescriptionContainerWidget {
    public RelicStatisticContainerWidget(int x, int y, RelicDescriptionScreen screen) {
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

    public List<FormattedCharSequence> getContent() {
        var sequences = new ArrayList<FormattedCharSequence>();
        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();
        var player = this.minecraft.player;
        var font = this.minecraft.font;
        var path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        var maxWidth = 320;
        var dot = ". ";
        var dotWidth = font.width(dot);

        for (var metric : relic.getStatisticTemplate(player, stack).getMetrics().values()) {
            var prefix = Component.translatable("tooltip.relics." + path + ".statistic." + metric.getId()).append(Component.literal(" "));
            var suffix = Component.literal(metric.getFormatValue().apply(relic.getMetricComponent(player, stack, metric.getId()).getValue())).withStyle(ChatFormatting.BOLD);

            var availableWidth = maxWidth - font.width(prefix) - font.width(suffix);
            var repeatCount = availableWidth / dotWidth;
            var line = prefix.append(dot.repeat(repeatCount)).append(suffix);

            sequences.addAll(font.split(line, maxWidth));
        }

        return sequences;
    }

    @Override
    public int getContentHeight() {
        return getContent().size() * 10;
    }
}