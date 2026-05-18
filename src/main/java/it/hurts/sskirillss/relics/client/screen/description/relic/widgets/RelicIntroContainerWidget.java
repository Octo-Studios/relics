package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.client.screen.base.IScrollableWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.RelicIntroScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.ScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.misc.TextJustificator;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RelicIntroContainerWidget extends AbstractDescriptionWidget implements IScrollableWidget {
    private static final int TEXT_WIDTH = 380;
    private static final int X_PADDING = 7;

    private final RelicDescriptionScreen screen;

    public RelicIntroContainerWidget(RelicDescriptionScreen screen) {
        super(screen.x + 77, screen.y + 160, 196, 55);

        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var poseStack = guiGraphics.pose();
        var lines = this.getLines();
        var shiftY = 0D;
        var scrollbar = this.getScrollbar();

        if (scrollbar != null) {
            var maxScrollPx = Math.max(0, this.getContentHeight() - this.getContainerHeight()) * 2D;

            shiftY = scrollbar.getScrollPosition(RenderUtils.getPartialTick(false)) * maxScrollPx;
        }

        GUIScissors.begin(this.getX(), this.getY(), this.getWidth(), this.getHeight());

        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate(0, -shiftY, 0);

        TextJustificator.renderJustifiedDescriptionWithStatBoxes(guiGraphics, (this.getX() + X_PADDING) * 2, this.getY() * 2, TEXT_WIDTH, this.minecraft.font, lines);

        poseStack.popPose();

        GUIScissors.end();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        var scrollbar = this.getScrollbar();

        return scrollbar == null ? super.mouseScrolled(mouseX, mouseY, scrollX, scrollY) : scrollbar.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }

    @Override
    public int getContentHeight() {
        return (int) Math.ceil(this.getLines().size() * (this.minecraft.font.lineHeight + 1) / 2D);
    }

    @Override
    public int getContainerHeight() {
        return this.getHeight();
    }

    @Nullable
    @Override
    public ScrollbarWidget getScrollbar() {
        return this.screen.children().stream()
                .filter(RelicIntroScrollbarWidget.class::isInstance)
                .map(ScrollbarWidget.class::cast)
                .findFirst()
                .orElse(null);
    }

    private List<TextJustificator.LayoutLine> getLines() {
        return TextJustificator.layoutJustifiedLines(this.minecraft.font, this.getRawLines(), List.of(), TEXT_WIDTH);
    }

    private List<TextJustificator.LineEntry> getRawLines() {
        var lines = new ArrayList<TextJustificator.LineEntry>();

        lines.add(new TextJustificator.LineEntry(Component.translatable("relics.description.relic.intro.title").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE).withColor(DescriptionUtils.NEUTRAL_COLOR(true)), false));
        lines.add(new TextJustificator.LineEntry(Component.literal(""), false));
        lines.add(new TextJustificator.LineEntry(Component.translatable("relics.description.relic.intro.customization"), true));
        lines.add(new TextJustificator.LineEntry(Component.literal(""), false));
        lines.add(new TextJustificator.LineEntry(Component.translatable("relics.description.relic.intro.top_scroll.title").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE).withColor(DescriptionUtils.NEGATIVE_COLOR(true)), true));
        lines.add(new TextJustificator.LineEntry(Component.translatable("relics.description.relic.intro.top_scroll.description"), true));
        lines.add(new TextJustificator.LineEntry(Component.literal(""), false));
        lines.add(new TextJustificator.LineEntry(Component.translatable("relics.description.relic.intro.bottom_scroll.title").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE).withColor(DescriptionUtils.NEGATIVE_COLOR(true)), true));
        lines.add(new TextJustificator.LineEntry(Component.translatable("relics.description.relic.intro.bottom_scroll.description"), true));
        lines.add(new TextJustificator.LineEntry(Component.literal(""), false));
        lines.add(new TextJustificator.LineEntry(Component.translatable("relics.description.relic.intro.buttons"), true));
        lines.add(new TextJustificator.LineEntry(Component.literal(""), false));
        lines.add(new TextJustificator.LineEntry(Component.translatable("relics.description.relic.intro.good_luck").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE).withColor(DescriptionUtils.POSITIVE_COLOR(true)), true));

        return lines;
    }
}
