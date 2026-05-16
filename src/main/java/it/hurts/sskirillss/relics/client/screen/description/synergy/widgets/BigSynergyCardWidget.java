package it.hurts.sskirillss.relics.client.screen.description.synergy.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.BigRelicCardWidget;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public class BigSynergyCardWidget extends AbstractDescriptionWidget {
    private SynergyDescriptionScreen screen;

    public BigSynergyCardWidget(int x, int y, SynergyDescriptionScreen screen) {
        super(x, y, 50, 87);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var player = minecraft.player;
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var poseStack = guiGraphics.pose();
        var ability = screen.getSelectedSynergy();

        var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(ability);
        var template = synergyData.getTemplate();

        var canBeUpgraded = synergyData.canBeUpgraded();
        var isUnlocked = synergyData.isUnlocked();

        poseStack.pushPose();

        var color = (float) (1.05F + (Math.sin((player.tickCount + (ability.length() * 10)) * 0.2F) * 0.1F));

        if (isUnlocked)
            GUIRenderer.begin(DescriptionTextures.getSynergyCardTexture(stack, ability), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .color(color, color, color, 1F)
                    .pos(this.getX() + 8, this.getY() + 20)
                    .texSize(34, 49)
                    .end();
        else
            GUIRenderer.begin(this.pickClosestBackground(stack, BigRelicCardWidget.BACKGROUNDS), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() + 8, this.getY() + 20)
                    .end();

        GUIRenderer.begin(isUnlocked ? DescriptionTextures.BIG_CARD_FRAME_ACTIVE : DescriptionTextures.BIG_CARD_FRAME_INACTIVE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY())
                .end();

        if (!true)
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/big_card_frame_level_slug_active.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX(), this.getY())
                    .end();

        var modes = template.getModes();

        if (isUnlocked && !modes.isEmpty()) {
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_list.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() + 8, this.getY() + 11)
                    .end();

            var dotWidth = 4;
            var fieldWidth = 30;

            var amount = modes.size();

            if (amount > 0) {
                var totalWidth = amount * dotWidth;
                var margin = (fieldWidth - totalWidth) / (amount + 1);

                for (int index = 0; index < amount; index++) {
                    var x = this.getX() + 11 + margin * (index + 1) + dotWidth * index;

                    GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_point.png"), poseStack)
                            .anchor(SpriteAnchor.TOP_LEFT)
                            .pos(x, this.getY() + 15)
                            .end();

                    if (index == modes.indexOf(synergyData.getMode()))
                        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_selection.png"), poseStack)
                                .anchor(SpriteAnchor.TOP_LEFT)
                                .pos(x - 1, this.getY() + 14)
                                .end();
                }
            }
        }

        if (canBeUpgraded) {
            poseStack.pushPose();

            var pointsComponent = Component.literal(isUnlocked ? (int) (synergyData.getProgress() * 100) + "%" : "?").withStyle(ChatFormatting.BOLD);

            poseStack.scale(0.75F, 0.75F, 1F);

            guiGraphics.drawString(minecraft.font, pointsComponent, (int) (((getX() + 26.5F) * 1.33F) - (minecraft.font.width(pointsComponent) / 2F)), (int) ((getY() + 4.5F) * 1.33F), isUnlocked ? 0xFFE278 : 0xB7AED9, false);

            poseStack.popPose();
        }

        if (isUnlocked && this.isHovered()) {
            if (modes.isEmpty())
                GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/big_card_frame_outline.png"), poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(this.getX() - 1, this.getY() - 1)
                        .end();
            else
                GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/big_card_frame_outline_with_modes.png"), poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(this.getX() - 4, this.getY() - 1)
                        .end();
        }

        poseStack.popPose();
    }

    private ResourceLocation pickClosestBackground(ItemStack itemStack, List<ResourceLocation> backgroundTextures) {
        var cardColor = BigRelicCardWidget.getTextureColor(DescriptionTextures.getSynergyCardTexture(itemStack, this.screen.getSelectedSynergy()));

        return backgroundTextures.stream()
                .min(Comparator.comparingDouble(texture -> BigRelicCardWidget.colorDistance(cardColor, BigRelicCardWidget.getTextureColor(texture))))
                .orElse(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

//    @Override
//    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//        var player = minecraft.player;
//        var stack = screen.getStack();
//        var ability = screen.getSelectedSynergy();
//
//        if (!(stack.getItem() instanceof IRelicItem relic))
//            return;
//
//        var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(ability);
//        var template = synergyData.getTemplate();
//
//        if (!synergyData.isUnlocked() || template == null)
//            return;
//
//        PoseStack poseStack = guiGraphics.pose();
//
//        List<FormattedCharSequence> tooltip = Lists.newArrayList();
//
//        int maxWidth = 150;
//        int renderWidth = 0;
//
//        // TODO
//        List<MutableComponent> entries = Lists.newArrayList(
////                Component.literal("").append(Component.translatable("relics.description.researching.ability.info.level").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + synergyData.getLevel() + "/" + template.getInitialMaxLevel()),
////                Component.literal("").append(Component.translatable("relics.description.researching.ability.info.quality").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + MathUtils.round(synergyData.calculateQuality() / 2F, 1) + "/" + synergyData.getMaxQuality() / 2),
//                Component.literal(" ")
//        );
//
//        if (Screen.hasShiftDown())
//            entries.add(Component.translatable("relics.description.researching.ability.info.extra_info").withStyle(ChatFormatting.ITALIC));
//        else
//            entries.add(Component.translatable("relics.description.researching.general.extra_info"));
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
//        poseStack.translate(0F, 0F, 400);
//
//        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, (this.getX() - renderWidth / 2) + 16, this.getY() + this.getHeight() - 2);
//
//        poseStack.scale(0.5F, 0.5F, 0.5F);
//
//        int yOff = 0;
//
//        for (FormattedCharSequence entry : tooltip) {
//            guiGraphics.drawString(minecraft.font, entry, ((this.getX() - renderWidth / 2) + 14 + 12) * 2, ((this.getY() + yOff + this.getHeight() - 2 + 9) * 2), DescriptionUtils.TEXT_COLOR, false);
//
//            yOff += 5;
//        }
//
//        poseStack.popPose();
//    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}
