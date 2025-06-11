package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import it.hurts.sskirillss.relics.client.screen.base.IScrollableWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.ScrollbarWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import lombok.Getter;
import net.minecraft.client.sounds.SoundManager;

import javax.annotation.Nullable;

public abstract class DescriptionContainerWidget extends AbstractDescriptionWidget implements IScrollableWidget {
    @Getter
    private final DescriptionScreen screen;

    public static final int MAX_LINES = 9;

    public DescriptionContainerWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, 170, 44);

        this.screen = screen;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        var scrollbar = this.getScrollbar();

        return scrollbar == null ? super.mouseScrolled(mouseX, mouseY, scrollX, scrollY) : scrollbar.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }

    @Override
    public int getContainerHeight() {
        return this.getHeight();
    }

    @Nullable
    @Override
    public ScrollbarWidget getScrollbar() {
        return this.getScreen().children().stream()
                .filter(ScrollbarWidget.class::isInstance)
                .map(ScrollbarWidget.class::cast)
                .findFirst()
                .orElse(null);
    }
}