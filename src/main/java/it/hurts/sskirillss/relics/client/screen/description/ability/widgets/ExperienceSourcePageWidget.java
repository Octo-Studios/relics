package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.client.screen.description.experience.ExperienceDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;

public class ExperienceSourcePageWidget extends AbstractDescriptionWidget {
    @Getter
    private ExperienceDescriptionScreen screen;

    @Getter
    private int step;

    public ExperienceSourcePageWidget(int x, int y, ExperienceDescriptionScreen screen, int step) {
        super(x, y, 12, 17);

        this.screen = screen;
        this.step = step;
    }

    @Override
    public void onPress() {
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var sources = relic.getLevelingSourcesData().getSources().keySet().stream()
                .filter(entry -> relic.isLevelingSourceEnabled(stack, entry))
                .toList();

        var maxEntries = 5;
        var size = sources.size();

        var totalPages = (int) Math.ceil(size / (double) maxEntries);

        if (totalPages <= 0)
            return;

        var newPage = screen.getPage() + step;

        if (newPage >= totalPages)
            newPage = 0;

        if (newPage < 0)
            newPage = totalPages - 1;

        screen.setPage(newPage);
        screen.rebuildWidgets();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        GUIRenderer.begin(step > 0 ? DescriptionTextures.PAGE_ARROW_DOWN : DescriptionTextures.PAGE_ARROW_UP, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY())
                .end();

        poseStack.popPose();
    }
}