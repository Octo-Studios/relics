package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class OptionsWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    @Getter
    private final DescriptionScreen screen;

    public OptionsWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, 14, 14);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = minecraft.player;

        if (player == null)
            return;

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        var time = player.tickCount + partialTick;
        var color = (float) (1.05F + (Math.sin((time + 10F) * 0.2F) * 0.1F));

        if (isHovered())
            color += 0.15F;

        poseStack.translate(this.getX() + Math.cos(time * 0.075F) * 0.5F, this.getY() - Math.sin(time * 0.075F) * 0.5F, 100);

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/options.png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .color(color, color, color, 1F)
                .end();

        if (this.isHovered())
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/options_outline.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(-1, -1)
                    .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.popPose();
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        var maxWidth = 50;
        var renderWidth = 0;
        var entry = Component.literal("WIP").withStyle(ChatFormatting.BOLD);
        int entryWidth = minecraft.font.width(entry) / 2;

        if (entryWidth > renderWidth)
            renderWidth = Math.min(entryWidth + 2, maxWidth);

        tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));

        poseStack.pushPose();
        poseStack.translate(0F, 0F, 100);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, mouseX - 9 - (renderWidth / 2), mouseY);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        var yOff = 0;

        for (FormattedCharSequence line : tooltip) {
            guiGraphics.drawString(minecraft.font, line, ((mouseX - renderWidth / 2) + 1) * 2, ((mouseY + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);
            yOff += 5;
        }

        poseStack.popPose();
    }
}
