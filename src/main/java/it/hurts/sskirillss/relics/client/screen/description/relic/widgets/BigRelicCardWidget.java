package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.google.common.collect.Lists;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BigRelicCardWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    private RelicDescriptionScreen screen;

    public BigRelicCardWidget(int x, int y, RelicDescriptionScreen screen) {
        super(x, y, 48, 74);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var player = minecraft.player;
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        GUIRenderer.begin(DescriptionTextures.BIG_CARD_FRAME_UNLOCKED_ACTIVE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY())
                .end();

        int xOff = 0;

        if (relic.hasUnlockedAbility(stack)) {
            for (int i = 0; i < 5; i++) {
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_HOLE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 4, getY() + 63)
                        .end();

                xOff += 8;
            }

            xOff = 0;

            var quality = relic.getRelicQuality(stack);
            var isAliquot = quality % 2 == 1;

            for (int i = 0; i < Math.floor(quality / 2D); i++) {
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 4, getY() + 63)
                        .end();

                xOff += 8;
            }

            if (isAliquot)
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 4, getY() + 63)
                        .patternSize(4, 7)
                        .texSize(8, 7)
                        .end();
        }

        poseStack.pushPose();

        float scale = 1.75F;

        poseStack.translate(getX() + 10 + 8 * scale, getY() + 21 + Math.sin((player.tickCount + pPartialTick) * 0.1F) * 2F + 8 * scale, 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.cos((player.tickCount + pPartialTick) * 0.05F) * 5F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.cos((player.tickCount + pPartialTick) * 0.075F) * 25F));

        poseStack.translate(-8 * scale, -8 * scale, -150 * scale);

        poseStack.scale(scale, scale, scale);

        guiGraphics.renderItem(stack, 0, 0);

        poseStack.popPose();

        poseStack.pushPose();

        poseStack.scale(0.75F, 0.75F, 1F);

        MutableComponent levelComponent = Component.literal(String.valueOf(relic.getRelicLevel(stack))).withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, levelComponent, (int) (((getX() + 25.5F) * 1.33F) - (minecraft.font.width(levelComponent) / 2F)), (int) ((getY() + 4) * 1.33F), 0xFFE278, true);

        poseStack.popPose();

        if (isHovered() && relic.hasUnlockedAbility(stack))
            GUIRenderer.begin(DescriptionTextures.BIG_CARD_FRAME_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() - 1, getY() - 1)
                    .end();

        poseStack.popPose();
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic) || !relic.hasUnlockedAbility(stack))
            return;

        var poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 150;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.info.level").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + relic.getRelicLevel(stack) + "/" + relic.getLevelingData().getMaxLevel()),
                Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.info.quality").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + MathUtils.round(relic.getRelicQuality(stack) / 2F, 1) + "/" + relic.getMaxQuality() / 2),
                Component.literal(" ")
        );

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("tooltip.relics.researching.relic.info.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("tooltip.relics.researching.general.extra_info"));

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry) / 2);

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 2, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        poseStack.pushPose();

        poseStack.translate(0F, 0F, 400);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, mouseX - 9 - (renderWidth / 2), mouseY);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        int yOff = 0;

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, ((mouseX - renderWidth / 2) + 1) * 2, ((mouseY + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;
        }

        poseStack.popPose();
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}