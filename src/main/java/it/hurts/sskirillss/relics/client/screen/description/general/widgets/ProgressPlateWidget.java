package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class ProgressPlateWidget extends AbstractPlateWidget {
    public ProgressPlateWidget(int x, int y, DescriptionScreen provider) {
        super(x, y, provider, "progress");
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var player = this.minecraft.player;

        if (player == null)
            return;

        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();

        var poseStack = guiGraphics.pose();

        var time = player.tickCount + pPartialTick;

        if (!relic.isRelicFlawless(player, stack))
            return;

        poseStack.pushPose();

        poseStack.translate(10.5F, 10F, -1);
        poseStack.scale(16F, 16F, 16F);

        var beams = 8;

        for (int i = 0; i < beams; i++) {
            var angle = (float) (i * 2F * Math.PI / beams);

            poseStack.pushPose();

            poseStack.mulPose(Axis.ZP.rotation(angle));
            poseStack.mulPose(Axis.ZP.rotation(time * 0.025F));

            var length = 0.85F + ((i % 2 == 0 ? Math.sin(time * 0.25F) : Math.cos(time * 0.25F)) * 0.1F);

            RenderUtils.renderFlatBeam(guiGraphics, pPartialTick, (float) length, 0.45F, 0xFFFFFF00, 0x00FF0000);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//        ItemStack stack = getProvider().getStack();
//
//        if (!(stack.getItem() instanceof IRelicItem relic))
//            return;
//
//        PoseStack poseStack = guiGraphics.pose();
//
//        List<FormattedCharSequence> tooltip = Lists.newArrayList();
//
//        int maxWidth = 150;
//        int renderWidth = 0;
//
//        List<MutableComponent> entries = Lists.newArrayList(
//                Component.literal("").append(Component.translatable("tooltip.relics.researching.general.leveling_point.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + relic.getRelicLevelingPoints(minecraft.player, stack)),
//                Component.literal(" ")
//        );
//
//        if (Screen.hasShiftDown())
//            entries.add(Component.translatable("tooltip.relics.researching.general.leveling_point.extra_info").withStyle(ChatFormatting.ITALIC));
//        else
//            entries.add(Component.translatable("tooltip.relics.researching.general.extra_info"));
//
//        for (MutableComponent entry : entries) {
//            int entryWidth = (minecraft.font.width(entry) / 2);
//
//            if (entryWidth > renderWidth)
//                renderWidth = Math.min(entryWidth + 2, maxWidth);
//
//            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
//        }
//
//        poseStack.pushPose();
//
//        poseStack.translate(0F, 0F, 100);
//
//        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, mouseX - 9 - (renderWidth / 2), mouseY);
//
//        poseStack.scale(0.5F, 0.5F, 0.5F);
//
//        int yOff = 0;
//
//        for (FormattedCharSequence entry : tooltip) {
//            guiGraphics.drawString(minecraft.font, entry, ((mouseX - renderWidth / 2) + 1) * 2, ((mouseY + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);
//
//            yOff += 5;
//        }
//
//        poseStack.popPose();
    }

    @Override
    public void onTick() {

    }

    @Override
    public String getValue(ItemStack stack) {
        return stack.getItem() instanceof IRelicItem relic ? (MathUtils.round(relic.calculateRelicProgress(minecraft.player, stack) * 100, 1) + "%").replace(".0", "") : "";
    }
}