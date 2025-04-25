package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base.AbstractAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketRelicTweak;
import it.hurts.sskirillss.relics.utils.EntityUtils;
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

public class RerollAbilityActionWidget extends AbstractAbilityActionWidget {
    public RerollAbilityActionWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, PacketRelicTweak.Operation.REROLL, screen);
    }

    @Override
    public boolean isLocked() {
        return !(getScreen().getStack().getItem() instanceof IRelicItem relic) || !relic.mayPlayerReroll(minecraft.player, getScreen().getStack(), getAbility());
    }

    @Override
    public void playDownSound(SoundManager handler) {
        var player = minecraft.player;
        var stack = getScreen().getStack();

        if (isLocked() || !(stack.getItem() instanceof IRelicItem relic)
                || (relic.getAbilityQuality(player, stack, getAbility()) == relic.getAbilityMaxQuality(player, stack, getAbility()) && !Screen.hasShiftDown()))
            return;

        handler.play(SimpleSoundInstance.forUI(SoundRegistry.TABLE_REROLL.get(), 1F));
    }

    @Override
    public void onPress() {
        var player = minecraft.player;
        var stack = getScreen().getStack();

        if (isLocked() || !(getScreen().getStack().getItem() instanceof IRelicItem relic))
            return;

        boolean hasWarning = relic.getAbilityQuality(player, stack, getAbility()) == relic.getAbilityMaxQuality(player, stack, getAbility());

        if (hasWarning && !Screen.hasShiftDown())
            return;

        NetworkHandler.sendToServer(new PacketRelicTweak(getScreen().getContainer(), getScreen().getSlot(), getAbility(), PacketRelicTweak.Operation.REROLL, !hasWarning && Screen.hasShiftDown()));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var player = minecraft.player;
        var stack = getScreen().getStack();

        if (!(getScreen().getStack().getItem() instanceof IRelicItem relic))
            return;

        boolean isWarning = relic.getAbilityQuality(player, stack, getAbility()) == relic.getAbilityMaxQuality(player, stack, getAbility());
        boolean isQuick = Screen.hasShiftDown() && relic.mayPlayerReroll(player, getScreen().getStack(), getAbility());

        float color = (isWarning && Screen.hasShiftDown()) || isQuick ? (float) (1.05F + (Math.sin((player.tickCount + (getAbility().length() * 10)) * 0.5F) * 0.1F)) : 1F;

        RenderSystem.setShaderColor(color, color, color, 1F);

        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/ability/reroll_button" + (isLocked() ? "_inactive" : "_active" + (isWarning ? "_warning" : isQuick ? "_quick" : "")) + ".png"), getX(), getY(), 0, 0, width, height, width, height);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        if (isHovered)
            guiGraphics.blit(DescriptionTextures.ACTION_BUTTON_OUTLINE, getX(), getY(), 0, 0, width, height, width, height);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var player = minecraft.player;
        var stack = getScreen().getStack();

        if (!(stack.getItem() instanceof IRelicItem relic) || !relic.isAbilityUnlocked(player, stack, getAbility()))
            return;

        AbilityTemplate data = relic.getAbilityTemplate(player, stack, getAbility());

        if (data.getStats().isEmpty())
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 120;
        int renderWidth = 0;

        int requiredExperience = relic.getRerollPlayerExperienceCost(player, stack, getAbility());
        long experience = EntityUtils.getPlayerTotalExperience(minecraft.player);

        MutableComponent negativeStatus = Component.translatable("tooltip.relics.relic.status.negative");
        MutableComponent positiveStatus = Component.translatable("tooltip.relics.relic.status.positive");
        MutableComponent unknownStatus = Component.translatable("tooltip.relics.relic.status.unknown");

        boolean isQuick = relic.mayPlayerReroll(minecraft.player, getScreen().getStack(), getAbility()) && relic.getAbilityQuality(player, stack, getAbility()) != relic.getAbilityMaxQuality(player, stack, getAbility());
        boolean hasExperience = requiredExperience <= experience;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.translatable("tooltip.relics.relic.reroll.description").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE),
                Component.literal(" "),
                Component.translatable("tooltip.relics.relic.reroll.cost", isQuick && Screen.hasShiftDown() ? Component.literal("XXX").withStyle(ChatFormatting.OBFUSCATED) : requiredExperience,
                        isQuick && Screen.hasShiftDown() ? Component.literal("XXX").withStyle(ChatFormatting.OBFUSCATED) : hasExperience ? EntityUtils.calculateExperienceLevelLoss(minecraft.player, requiredExperience) : EntityUtils.getLevelFromTotalExperience(requiredExperience),
                        hasExperience ? isQuick && Screen.hasShiftDown() ? unknownStatus : positiveStatus : negativeStatus)
        );

        if (relic.getAbilityQuality(player, stack, getAbility()) == relic.getAbilityMaxQuality(player, stack, getAbility())) {
            entries.add(Component.literal(" "));
            entries.add(Component.literal("▶ ").append(Component.translatable("tooltip.relics.relic.reroll.warning")));
        } else if (relic.mayPlayerReroll(minecraft.player, getScreen().getStack(), getAbility())) {
            entries.add(Component.literal(" "));
            entries.add(Component.literal("▶ ").append(Component.translatable("tooltip.relics.relic.reroll.quick")));
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