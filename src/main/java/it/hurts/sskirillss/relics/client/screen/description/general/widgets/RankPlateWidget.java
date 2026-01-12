package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RankPlateWidget extends AbstractPlateWidget {
    public RankPlateWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, screen, "rank");
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

        if (!relic.getRelicData(player, stack).isMaxRank())
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

            RenderUtils.renderFlatBeam(guiGraphics, pPartialTick, (float) length, 0.45F, 0xFF00FFFF, 0x000000FF);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public List<MutableComponent> getHoverTooltip() {
        var entries = new ArrayList<MutableComponent>();

        var stack = this.getScreen().getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return entries;

        entries.add(Component.literal("").append(Component.translatable("relics.description.researching.general.relic_rank.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + relic.getRelicData(minecraft.player, stack).getLevelingData().getRank()));

        entries.add(Component.literal(" "));

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("relics.description.researching.general.relic_rank.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("relics.description.researching.general.extra_info"));

        return entries;
    }

    @Override
    public String getValue(ItemStack stack) {
        return String.valueOf(stack.getItem() instanceof IRelicItem relic ? relic.getRelicData(minecraft.player, stack).getLevelingData().getRank() : 0);
    }
}