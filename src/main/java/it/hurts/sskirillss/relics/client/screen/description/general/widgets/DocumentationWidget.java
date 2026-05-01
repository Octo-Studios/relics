package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IDocsEntry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class DocumentationWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    @Getter
    private final DescriptionScreen screen;

    public DocumentationWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, 14, 14);

        this.screen = screen;
    }

    @Override
    public void onPress() {
        var stack = this.screen.getStack();

        if (!(stack.getItem() instanceof IDocsEntry entry))
            return;

        var uri = entry.getURI(Minecraft.getInstance().player, stack);

        if (uri == null)
            return;

        Util.getPlatform().openUri(uri);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = minecraft.player;

        if (player == null)
            return;

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        var color = (float) (1.05F + (Math.sin((player.tickCount + partialTick + 20F) * 0.2F) * 0.1F));

        poseStack.translate(this.getX() + Math.sin((player.tickCount + partialTick) * 0.075F), this.getY() + Math.cos((player.tickCount + partialTick) * 0.075F) * 0.5F, 100);

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/documentation.png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .color(color, color, color, 1F)
                .end();

        if (this.isHovered())
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/documentation_outline.png"), poseStack)
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

        var maxWidth = 170;
        var renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.translatable("relics.description.researching.general.documentation.title").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                Component.literal(" "),
                Component.translatable("relics.description.researching.general.documentation.description")
        );

        for (var entry : entries) {
            int entryWidth = (minecraft.font.width(entry) / 2);

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 2, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        poseStack.pushPose();
        poseStack.translate(0F, 0F, 100);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, mouseX - 9 - (renderWidth / 2), mouseY);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        var yOff = 0;

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, ((mouseX - renderWidth / 2) + 1) * 2, ((mouseY + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);
            yOff += 5;
        }

        poseStack.popPose();
    }
}