package it.hurts.sskirillss.relics.client.screen.description.general.widgets.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.badges.base.AbstractBadge;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;

public abstract class AbstractBadgeWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    @Getter
    private IRelicScreenProvider provider;

    public AbstractBadgeWidget(int x, int y, IRelicScreenProvider provider, AbstractBadge badge) {
        super(x, y, 16, 16);

        this.provider = provider;
    }

    public abstract AbstractBadge getBadge();

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (minecraft.player == null)
            return;

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        float color = (float) (1.05F + (Math.sin((minecraft.player.tickCount + (getBadge().getId().length() * 10)) * 0.2F) * 0.1F));

        RenderSystem.setShaderColor(color, color, color, 1F);

        poseStack.translate(getX(), getY(), 0);

        if (isHovered) {
            poseStack.translate(width / 2F, height / 2F, 0);

            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.cos((minecraft.player.tickCount + pPartialTick) * 0.35F) * 7.5F));

            poseStack.translate(-(width / 2F), -(height / 2F), 0);
        }

        guiGraphics.blit(getBadge().getIconTexture(minecraft.player, provider.getStack()), 0, 0, 0, 0, width, height, width, height);

        if (isHovered)
            guiGraphics.blit(getBadge().getOutlineTexture(minecraft.player, provider.getStack()), 0, 0, 0, 0, width, height, width, height);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.popPose();
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}