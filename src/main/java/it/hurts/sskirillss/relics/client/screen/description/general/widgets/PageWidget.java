package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.IPagedDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.GUIScissors;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
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
        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 100;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.translatable("tooltip.relics.researching.tab." + page.name().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.BOLD)
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
}