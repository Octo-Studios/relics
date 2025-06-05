package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.ScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class RelicDescriptionWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private final RelicDescriptionScreen screen;

    public RelicDescriptionWidget(int x, int y, RelicDescriptionScreen screen) {
        super(x, y, 179, 44);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;
        var stack = this.screen.getStack();

        if (stack == null || !(stack.getItem() instanceof IRelicItem relic) || player == null)
            return;

        var poseStack = guiGraphics.pose();

        GUIScissors.begin(getX(), getY(), getWidth(), getHeight());

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        var maxLines = 9;
        var lineOffset = 10;
        var lines = RelicDescriptionScreen.justifyStyledText(Component.translatable("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".description"), 320, minecraft.font);

        var scroll = screen.children().stream()
                .filter(ScrollbarWidget.class::isInstance)
                .map(ScrollbarWidget.class::cast)
                .findFirst()
                .orElse(null);

        if (scroll != null) {
            var offset = scroll.getScrollPosition(partialTick);

            poseStack.translate(0, -(offset * ((lines.size() - maxLines) * (lineOffset))), 0);
        }

        var yOff = 0;

        for (FormattedCharSequence line : lines) {

            guiGraphics.drawString(minecraft.font, line, (this.getX() + 7) * 2, (this.getY() * 2) + yOff, DescriptionUtils.TEXT_COLOR, false);

            yOff += lineOffset;
        }

        poseStack.popPose();

        GUIScissors.end();
    }

    @Override
    public void onTick() {

    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}