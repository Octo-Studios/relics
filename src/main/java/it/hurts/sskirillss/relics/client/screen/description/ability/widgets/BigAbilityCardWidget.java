package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.BigRelicCardWidget;
import it.hurts.sskirillss.relics.client.screen.description.research.particles.SmokeParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public class BigAbilityCardWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    private AbilityDescriptionScreen screen;

    public BigAbilityCardWidget(int x, int y, AbilityDescriptionScreen screen) {
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
        var ability = screen.getSelectedAbility();

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);
        var template = abilityData.getTemplate();

        if (template == null)
            return;

        var isUnlocked = abilityData.isUnlocked();
        var canBeUpgraded = abilityData.canBeUpgraded();

        poseStack.pushPose();

        var color = (float) (1.05F + (Math.sin((player.tickCount + (ability.length() * 10)) * 0.2F) * 0.1F));

        if (isUnlocked)
            GUIRenderer.begin(DescriptionTextures.getAbilityCardTexture(stack, ability), poseStack)
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

        GUIRenderer.begin(canBeUpgraded ? isUnlocked ? DescriptionTextures.BIG_CARD_FRAME_UNLOCKED_ACTIVE : DescriptionTextures.BIG_CARD_FRAME_UNLOCKED_INACTIVE : isUnlocked ? DescriptionTextures.BIG_CARD_FRAME_LOCKED_ACTIVE : DescriptionTextures.BIG_CARD_FRAME_LOCKED_INACTIVE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY())
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

                    if (index == modes.indexOf(abilityData.getMode()))
                        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_selection.png"), poseStack)
                                .anchor(SpriteAnchor.TOP_LEFT)
                                .pos(x - 1, this.getY() + 14)
                                .end();
                }
            }
        }

        var xOff = 0;

        if (isUnlocked && canBeUpgraded) {
            for (int i = 0; i < 5; i++) {
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_HOLE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 5, getY() + 72)
                        .end();

                xOff += 8;
            }

            xOff = 0;

            var quality = abilityData.calculateQuality();
            var isAliquot = quality % 2 == 1;

            for (int i = 0; i < Math.floor(quality / 2D); i++) {
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 5, getY() + 72)
                        .end();

                xOff += 8;
            }

            if (isAliquot)
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 5, getY() + 72)
                        .patternSize(4, 7)
                        .texSize(8, 7)
                        .end();
        }

        if (canBeUpgraded) {
            poseStack.pushPose();

            var pointsComponent = Component.literal(isUnlocked ? String.valueOf(abilityData.getLevel()) : "?").withStyle(ChatFormatting.BOLD);

            poseStack.scale(0.75F, 0.75F, 1F);

            guiGraphics.drawString(minecraft.font, pointsComponent, (int) (((getX() + 26.5F) * 1.33F) - (minecraft.font.width(pointsComponent) / 2F)), (int) ((getY() + 4.5F) * 1.33F), isUnlocked ? 0xFFE278 : 0xB7AED9, false);

            poseStack.popPose();
        }

        if (isUnlocked && canBeUpgraded && this.isHovered()) {
            if (modes.isEmpty())
                GUIRenderer.begin(DescriptionTextures.BIG_CARD_FRAME_OUTLINE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(this.getX() - 1, this.getY() - 1)
                        .end();
            else
                GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/big_card_frame_outline_modes.png"), poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(this.getX() - 4, this.getY() - 1)
                        .end();
        }

        poseStack.popPose();
    }

    private ResourceLocation pickClosestBackground(ItemStack itemStack, List<ResourceLocation> backgroundTextures) {
        var cardColor = BigRelicCardWidget.getTextureColor(DescriptionTextures.getAbilityCardTexture(itemStack, this.screen.getSelectedAbility()));

        return backgroundTextures.stream()
                .min(Comparator.comparingDouble(texture -> BigRelicCardWidget.colorDistance(cardColor, BigRelicCardWidget.getTextureColor(texture))))
                .orElse(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var player = minecraft.player;
        var stack = screen.getStack();
        var ability = screen.getSelectedAbility();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);
        var template = abilityData.getTemplate();

        if (!abilityData.isUnlocked() || !abilityData.canBeUpgraded() || template == null)
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 150;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.literal("").append(Component.translatable("relics.description.researching.ability.info.level").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + abilityData.getLevel() + "/" + template.getInitialMaxLevel()),
                Component.literal("").append(Component.translatable("relics.description.researching.ability.info.quality").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + MathUtils.round(abilityData.calculateQuality() / 2F, 1) + "/" + abilityData.getMaxQuality() / 2),
                Component.literal(" ")
        );

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("relics.description.researching.ability.info.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("relics.description.researching.general.extra_info"));

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry) / 2);

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 2, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        poseStack.pushPose();

        poseStack.translate(0F, 0F, 400);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, (this.getX() - renderWidth / 2) + 16, this.getY() + this.getHeight() - 2);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        int yOff = 0;

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, ((this.getX() - renderWidth / 2) + 14 + 12) * 2, ((this.getY() + yOff + this.getHeight() - 2 + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;
        }

        poseStack.popPose();
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}
