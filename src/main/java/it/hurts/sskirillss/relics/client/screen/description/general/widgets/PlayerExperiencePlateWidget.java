package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.utils.misc.ProgressBarEffect;
import it.hurts.sskirillss.relics.client.screen.utils.misc.ProgressPath;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlayerExperiencePlateWidget extends AbstractPlateWidget {
    private static final int BAR_WIDTH = 52;
    private static final int BAR_HEIGHT = 2;
    private static final int ANIMATION_DURATION = 16;
    private static final float FLASH_STRENGTH = 0.8F;

    private final ProgressBarEffect progressEffect = new ProgressBarEffect(ProgressPath.horizontalBar(BAR_WIDTH, BAR_HEIGHT), ANIMATION_DURATION, ProgressBarEffect.Palette.GREEN).animateDecrease(true);

    private int lastLevel = -1;
    private Float pendingProgress = null;

    public PlayerExperiencePlateWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, screen, "player_experience");
    }

    @Override
    public void onTick() {
        super.onTick();

        if (minecraft.player == null)
            return;

        var level = minecraft.player.experienceLevel;
        var progress = minecraft.player.experienceProgress;

        if (lastLevel < 0)
            progressEffect.snap(progress);
        else if (level != lastLevel) {
            pendingProgress = progress;

            progressEffect.update(level > lastLevel ? 1F : 0F);
        } else if (pendingProgress != null)
            pendingProgress = progress;
        else
            progressEffect.update(progress);

        lastLevel = level;

        if (pendingProgress != null && !progressEffect.isMoving()) {
            progressEffect.snap(0F);
            progressEffect.update(pendingProgress);

            pendingProgress = null;
        }

        progressEffect.tick(getScreen(), minecraft.player.getRandom(), getX() + 1, getY() + height - 3, 1F, 1F);
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var brightness = 1F + (progressEffect.isRising() ? progressEffect.flash(pPartialTick) * FLASH_STRENGTH : 0F);

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        GUIRenderer.begin(DescriptionTextures.PLATE_PLAYER_EXPERIENCE_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(1, height - 3)
                .end();

        GUIRenderer.begin(DescriptionTextures.PLATE_PLAYER_EXPERIENCE_FILLER, poseStack)
                .pos(1, height - 3)
                .texSize(BAR_WIDTH, BAR_HEIGHT)
                .anchor(SpriteAnchor.TOP_LEFT)
                .color(brightness, brightness, brightness, 1F)
                .patternSize((int) Math.ceil(BAR_WIDTH * progressEffect.progressOr(minecraft.player.experienceProgress, pPartialTick)), BAR_HEIGHT)
                .end();

        poseStack.popPose();
    }

    @Override
    public List<MutableComponent> getHoverTooltip() {
        var experience = EntityUtils.getPlayerTotalExperience(minecraft.player);

        var entries = new ArrayList<MutableComponent>();

        entries.add(Component.literal("").append(Component.translatable("relics.description.researching.general.player_experience.title_1").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" ").append(Component.translatable("relics.description.researching.general.player_experience.title_2", experience, EntityUtils.getLevelFromTotalExperience(experience))));

        entries.add(Component.literal(" "));

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("relics.description.researching.general.player_experience.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("relics.description.researching.general.extra_info"));

        return entries;
    }

    @Override
    public String getValue(ItemStack stack) {
        return String.valueOf(minecraft.player.experienceLevel);
    }
}