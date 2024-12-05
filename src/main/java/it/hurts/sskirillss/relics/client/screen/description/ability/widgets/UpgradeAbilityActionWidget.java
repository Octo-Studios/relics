package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base.AbstractAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketRelicTweak;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class UpgradeAbilityActionWidget extends AbstractAbilityActionWidget {
    public UpgradeAbilityActionWidget(int x, int y, AbilityDescriptionScreen screen, String ability) {
        super(x, y, PacketRelicTweak.Operation.UPGRADE, screen, ability);
    }

    @Override
    public boolean isLocked() {
        return !(getScreen().getStack().getItem() instanceof IRelicItem relic) || !relic.mayPlayerUpgrade(minecraft.player, getScreen().getStack(), getAbility());
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (getScreen().getStack().getItem() instanceof IRelicItem relic && !isLocked()) {
            int level = relic.getAbilityLevel(getScreen().getStack(), getAbility());
            int maxLevel = relic.getAbilityData(getAbility()).getMaxLevel();

            handler.play(SimpleSoundInstance.forUI(SoundRegistry.TABLE_UPGRADE.get(), Screen.hasShiftDown() && relic.mayPlayerUpgrade(minecraft.player, getScreen().getStack(), getAbility()) ? 2F : 1F + ((float) level / maxLevel)));
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (!(getScreen().getStack().getItem() instanceof IRelicItem relic))
            return;

        boolean isQuick = Screen.hasShiftDown() && relic.mayPlayerUpgrade(minecraft.player, getScreen().getStack(), getAbility());

        float color = isQuick ? (float) (1.05F + (Math.sin((minecraft.player.tickCount + (getAbility().length() * 10)) * 0.5F) * 0.1F)) : 1F;

        RenderSystem.setShaderColor(color, color, color, 1F);

        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/ability/upgrade_button_" + (isLocked() ? "inactive" : "active") + (isQuick ? "_quick" : "") + ".png"), getX(), getY(), 0, 0, width, height, width, height);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        if (isHovered)
            guiGraphics.blit(DescriptionTextures.ACTION_BUTTON_OUTLINE, getX(), getY(), 0, 0, width, height, width, height);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!(getScreen().getStack().getItem() instanceof IRelicItem relic) || !relic.isAbilityUnlocked(getScreen().getStack(), getAbility()))
            return;

        AbilityData data = relic.getAbilityData(getAbility());

        if (data.getStats().isEmpty())
            return;

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        PoseStack poseStack = guiGraphics.pose();

        int maxWidth = 100;
        int renderWidth = 0;

        int requiredPoints = data.getRequiredPoints();
        int requiredLevel = relic.getUpgradeRequiredLevel(getScreen().getStack(), getAbility());

        int points = relic.getRelicLevelingPoints(getScreen().getStack());
        int level = minecraft.player.experienceLevel;

        MutableComponent negativeStatus = Component.translatable("tooltip.relics.relic.status.negative");
        MutableComponent positiveStatus = Component.translatable("tooltip.relics.relic.status.positive");

        List<MutableComponent> entries = Lists.newArrayList(Component.translatable("tooltip.relics.relic.upgrade.description").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE));

        boolean isMaxLevel = relic.isAbilityMaxLevel(getScreen().getStack(), getAbility());
        boolean isQuick = Screen.hasShiftDown() && relic.mayPlayerUpgrade(minecraft.player, getScreen().getStack(), getAbility());

        if (!isMaxLevel) {
            entries.add(Component.literal(" "));
            entries.add(Component.translatable("tooltip.relics.relic.upgrade.cost", isQuick ? Component.literal("XXX").withStyle(ChatFormatting.OBFUSCATED) : requiredPoints,
                    (requiredPoints > points ? negativeStatus : positiveStatus), isQuick ? Component.literal("XXX").withStyle(ChatFormatting.OBFUSCATED) : requiredLevel,
                    (requiredLevel > level ? negativeStatus : positiveStatus)));
        }

        if (!isLocked()) {
            entries.add(Component.literal(" "));
            entries.add(Component.literal("▶ ").append(Component.translatable("tooltip.relics.relic.upgrade.quick")));
        }

        if (isMaxLevel) {
            entries.add(Component.literal(" "));
            entries.add(Component.literal("▶ ").append(Component.translatable("tooltip.relics.relic.upgrade.locked")));
        }

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry) + 4) / 2;

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        int height = Math.round(tooltip.size() * 5F);

        int renderX = getX() + width + 1;
        int renderY = mouseY - (height / 2) - 9;

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, height, renderX, renderY);

        int yOff = 0;

        poseStack.scale(0.5F, 0.5F, 0.5F);

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, (renderX + 10) * 2, (renderY + 9 + yOff) * 2, DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;
        }

        poseStack.scale(1F, 1F, 1F);
    }
}