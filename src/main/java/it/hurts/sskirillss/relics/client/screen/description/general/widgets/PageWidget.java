package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public class PageWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    @Getter
    private IRelicScreenProvider source;

    @Getter
    private IRelicScreenProvider target;

    @Getter
    private DescriptionPage page;

    public PageWidget(int x, int y, IRelicScreenProvider source, DescriptionPage page, IRelicScreenProvider target) {
        super(x, y, 17, 19);

        this.source = source;
        this.target = target;
        this.page = page;
    }

    @Override
    public void onPress() {
        minecraft.setScreen((Screen) target);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var poseStack = guiGraphics.pose();
        var player = minecraft.player;

        poseStack.pushPose();

        if (isLocked()) {
            GUIScissors.begin(getX(), getY(), width, 19);

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

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/general/tabs/" + page.name().toLowerCase(Locale.ROOT) + ".png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() + 2, getY() + 5)
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
        return minecraft.screen instanceof IPagedDescriptionScreen screen && screen.getPage() == page;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked())
            super.playDownSound(handler);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }
}