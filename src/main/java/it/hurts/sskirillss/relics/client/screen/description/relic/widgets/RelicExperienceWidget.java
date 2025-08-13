package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.octostudios.octolib.client.particle.UIParticle;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.particle.PixelUIParticle;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector2f;

import java.util.List;

public class RelicExperienceWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private static final int FILLER_WIDTH = 125;

    private final DescriptionScreen screen;

    public RelicExperienceWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, 139, 15);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var player = Minecraft.getInstance().player;

        if (player == null || !(screen.getStack().getItem() instanceof IRelicItem relic))
            return;

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        var color = (float) (1.025F + (Math.sin(player.tickCount * 0.5F) * 0.05F));

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

        var percentage = Component.literal(relic.isRelicMaxLevel(minecraft.player, screen.getStack()) ? "MAX" : MathUtils.round(calculateFillerPercentage(relic), 1) + "%").withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, percentage, (getX() + 67) * 2 - (minecraft.font.width(percentage) / 2), (getY() + 6) * 2, DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        var player = minecraft.player;

        if (!(screen.getStack().getItem() instanceof IRelicItem relic) || player == null)
            return;

        var random = player.getRandom();

        int fillerWidth = this.calculateFillerWidth(relic);

        if (player.tickCount % 5 == 0) {
            for (float i = 0; i < fillerWidth / 30F; i++) {
                var particle = new PixelUIParticle(0.4f, random.nextInt(20, 40), this.getX() + 5 + random.nextInt(fillerWidth), this.getY() + random.nextInt(2), UIParticle.Layer.SCREEN, 10);

                float size = (random.nextFloat() * 0.5F) + 0.75F;

                particle.setColors(new OctoColor((random.nextFloat() * 0.25F) + 0.75F, random.nextFloat() * 0.25F, 1F, 1F), OctoColor.WHITE, new OctoColor(1F, random.nextFloat() * 0.25F, (random.nextFloat() * 0.5F) + 0.25F, 0F));
                particle.setDirection(MathUtils.randomFloat(random) * 0.75F, random.nextFloat() * -0.5F);
                particle.setRollVelocity(MathUtils.randomFloat(random) * 15);
                particle.getTransform().setSize(new Vector2f(size, size));
                particle.setGravityDirection(0, -1);
                particle.setScreen(this.screen);
                particle.setFriction(0.025F);
                particle.setGravity(0.02F);

                particle.instantiate();
            }
        }
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(screen.getStack().getItem() instanceof IRelicItem relic))
            return;

        var poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 150;
        int renderWidth = 0;

        var level = relic.getRelicLevel(minecraft.player, screen.getStack());

        var experience = String.valueOf(MathUtils.round(relic.getRelicExperience(minecraft.player, screen.getStack()), 1));

        List<MutableComponent> entries = Lists.newArrayList(
                Component.literal("").append(Component.translatable("relics.description.researching.relic.experience.title").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE))
                        .append(" " + (relic.isRelicMaxLevel(minecraft.player, screen.getStack()) ? "MAX" : (experience.endsWith(".0") ? experience.replace(".0", "") : experience) + "/" + relic.getTotalRelicExperienceBetweenLevels(minecraft.player, screen.getStack(), level, level + 1))),
                Component.literal(" ")
        );

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("relics.description.researching.relic.experience.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("relics.description.researching.general.extra_info"));

        for (MutableComponent entry : entries) {
            var entryWidth = (minecraft.font.width(entry) / 2);

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

    @Override
    public void playDownSound(SoundManager handler) {

    }

    private float calculateFillerPercentage(IRelicItem relic) {
        var level = relic.getRelicLevel(minecraft.player, screen.getStack());

        return (float) (relic.getRelicExperience(minecraft.player, screen.getStack()) / (relic.getTotalRelicExperienceBetweenLevels(minecraft.player, screen.getStack(), level, level + 1) / 100D));
    }

    private int calculateFillerWidth(IRelicItem relic) {
        return relic.isRelicMaxLevel(minecraft.player, screen.getStack()) ? FILLER_WIDTH : (int) Math.ceil(calculateFillerPercentage(relic) / 100F * FILLER_WIDTH);
    }
}