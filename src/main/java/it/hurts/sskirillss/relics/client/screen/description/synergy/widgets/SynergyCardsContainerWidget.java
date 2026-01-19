package it.hurts.sskirillss.relics.client.screen.description.synergy.widgets;

import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;

public class SynergyCardsContainerWidget extends AbstractDescriptionWidget {
    private final SynergyDescriptionScreen screen;

    public SynergyCardsContainerWidget(SynergyDescriptionScreen screen) {
        super(screen.x + 77, screen.y + 150, 209, 70);

        this.screen = screen;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (var listener : screen.children()) {
            if (listener instanceof SynergyPageScrollbarWidget scrollbar && scrollbar.handleExternalScroll(scrollY))
                return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}
