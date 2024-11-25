package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.RandomSource;

import java.awt.*;
import java.util.List;

public class RelicExperienceWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private static final int FILLER_WIDTH = 125;

    private final IRelicScreenProvider screen;

    public RelicExperienceWidget(int x, int y, IRelicScreenProvider screen) {
        super(x, y, 139, 15);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null || !(screen.getStack().getItem() instanceof IRelicItem relic))
            return;

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();

        float color = (float) (1.025F + (Math.sin(player.tickCount * 0.5F) * 0.05F));

        GUIRenderer.begin(DescriptionTextures.RELIC_EXPERIENCE_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY() - 10)
                .end();

        RenderSystem.enableBlend();

        GUIRenderer.begin(DescriptionTextures.RELIC_EXPERIENCE_FILLER, poseStack)
                .patternSize(calculateFillerWidth(relic), 11)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX() + 3, getY() + 2)
                .color(color, color, color, 1F)
                .texSize(FILLER_WIDTH, 11)
                .end();

        RenderSystem.disableBlend();

        if (isHovered())
            GUIRenderer.begin(DescriptionTextures.RELIC_EXPERIENCE_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() - 1, getY() - 6)
                    .end();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        MutableComponent percentage = Component.literal(relic.isRelicMaxLevel(screen.getStack()) ? "MAX" : MathUtils.round(calculateFillerPercentage(relic), 1) + "%").withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, percentage, (getX() + 67) * 2 - (minecraft.font.width(percentage) / 2), (getY() + 6) * 2, DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        if (!(screen.getStack().getItem() instanceof IRelicItem relic) || minecraft.player == null)
            return;

        RandomSource random = minecraft.player.getRandom();

        int fillerWidth = calculateFillerWidth(relic);

        if (minecraft.player.tickCount % 5 == 0) {
            for (float i = 0; i < fillerWidth / 40F; i++) {
                ParticleStorage.addParticle((Screen) screen, new ExperienceParticleData(new Color(200, 255, 0),
                        getX() + 5 + random.nextInt(fillerWidth), getY() + random.nextInt(2), 1F + (random.nextFloat() * 0.25F), 50 + random.nextInt(50)));
            }
        }
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(screen.getStack().getItem() instanceof IRelicItem relic))
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 150;
        int renderWidth = 0;

        int level = relic.getRelicLevel(screen.getStack());

        List<MutableComponent> entries = Lists.newArrayList(
                Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.experience.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE))
                        .append(" " + (relic.isRelicMaxLevel(screen.getStack()) ? "MAX" : relic.getRelicExperience(screen.getStack()) + "/" + relic.getTotalRelicExperienceBetweenLevels(level, level + 1))),
                Component.literal(" ")
        );

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("tooltip.relics.researching.relic.experience.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("tooltip.relics.researching.general.extra_info"));

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

    private float calculateFillerPercentage(IRelicItem relic) {
        int level = relic.getRelicLevel(screen.getStack());

        return relic.getRelicExperience(screen.getStack()) / (relic.getTotalRelicExperienceBetweenLevels(level, level + 1) / 100F);
    }

    private int calculateFillerWidth(IRelicItem relic) {
        return relic.isRelicMaxLevel(screen.getStack()) ? FILLER_WIDTH : (int) Math.ceil(calculateFillerPercentage(relic) / 100F * FILLER_WIDTH);
    }
}