package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

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

        if (!relic.isRelicMaxRank(player, stack))
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
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    public void onTick() {

    }

    @Override
    public String getValue(ItemStack stack) {
        return String.valueOf(stack.getItem() instanceof IRelicItem relic ? relic.getRelicRank(minecraft.player, stack) : 0);
    }
}