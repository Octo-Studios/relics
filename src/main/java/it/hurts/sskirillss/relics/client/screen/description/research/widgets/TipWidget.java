package it.hurts.sskirillss.relics.client.screen.description.research.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.description.research.AbilityResearchScreen;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;

import java.awt.*;
import java.util.List;

public class TipWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private final AbilityResearchScreen screen;

    public TipWidget(int x, int y, AbilityResearchScreen screen) {
        super(x, y, 12, 14);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || !(screen.stack.getItem() instanceof IRelicItem))
            return;

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        float color = (float) (1.05F + (Math.sin((player.tickCount + pPartialTick) * 0.5F) * 0.1F));

        GUIRenderer.begin(DescriptionTextures.TIP_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .color(color, color, color, 1F)
                .pos(getX() + 2, getY() + 1)
                .end();

        if (isHovered())
            GUIRenderer.begin(DescriptionTextures.TIP_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX(), getY() - 1)
                    .end();

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        if (!isHovered() || !(screen.stack.getItem() instanceof IRelicItem) || minecraft.player == null)
            return;

        RandomSource random = minecraft.player.getRandom();

        if (minecraft.player.tickCount % 5 == 0) {
            ParticleStorage.addParticle(screen, new ExperienceParticleData(new Color(255, 200 + random.nextInt(50), random.nextInt(50)),
                    getX() + 34 + random.nextInt(16), getY() - 6 + random.nextInt(16), 1F + (random.nextFloat() * 0.25F), 50 + random.nextInt(50)));
        }
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(screen.stack.getItem() instanceof IRelicItem relic))
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 180;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.translatable("tooltip.relics.researching.research.tip")
        );

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry) / 2);

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 2, maxWidth);

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

    @Override
    public void playDownSound(SoundManager handler) {

    }
}