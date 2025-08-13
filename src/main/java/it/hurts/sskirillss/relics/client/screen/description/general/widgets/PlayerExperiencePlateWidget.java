package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractPlateWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
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
    public PlayerExperiencePlateWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, screen, "player_experience");
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var barWidth = 52;
        var barHeight = 2;

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        GUIRenderer.begin(DescriptionTextures.PLATE_PLAYER_EXPERIENCE_BACKGROUND, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(1, height - 3)
                .end();

        GUIRenderer.begin(DescriptionTextures.PLATE_PLAYER_EXPERIENCE_FILLER, poseStack)
                .pos(1, height - 3)
                .texSize(barWidth, barHeight)
                .anchor(SpriteAnchor.TOP_LEFT)
                .patternSize((int) (barWidth * minecraft.player.experienceProgress), barHeight)
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