package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base.AbstractAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.HotkeyRegistry;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class UpgradeAbilityActionWidget extends AbstractAbilityActionWidget {
    public UpgradeAbilityActionWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, PacketAbilityTweak.Operation.UPGRADE, screen);
    }

    @Override
    public boolean isLocked() {
        return !(getScreen().getStack().getItem() instanceof IRelicItem relic) || !relic.mayPlayerUpgrade(minecraft.player, getScreen().getStack(), getAbility());
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (getScreen().getStack().getItem() instanceof IRelicItem relic && !isLocked()) {
            int level = relic.getAbilityLevel(minecraft.player, getScreen().getStack(), getAbility());
            int maxLevel = relic.getAbilityTemplate(minecraft.player, getScreen().getStack(), getAbility()).getMaxLevel();

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

        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/upgrade_button_" + (isLocked() ? "inactive" : "active") + (isQuick ? "_quick" : "") + ".png"), getX(), getY(), 0, 0, width, height, width, height);

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        if (isHovered)
            guiGraphics.blit(DescriptionTextures.ACTION_BUTTON_OUTLINE, getX(), getY(), 0, 0, width, height, width, height);
    }

    @Override
    public List<MutableComponent> buildDescription() {
        var description = super.buildDescription();

        var screen = this.getScreen();

        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();
        var ability = screen.getSelectedAbility();

        var key = HotkeyRegistry.RESEARCH_RELIC.getKey().getValue();

        var hasShiftDown = key != GLFW.GLFW_KEY_UNKNOWN && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key);

        var newLine = Component.literal(" ");

        var currentExperience = EntityUtils.getPlayerTotalExperience(player);
        var requiredExperience = relic.getUpgradePlayerExperienceCost(player, stack, getAbility());
        var hasExperience = requiredExperience <= currentExperience;

        var currentLevelingPoints = relic.getRelicLevelingPoints(player, stack);
        var requiredLevelingPoints = relic.getRelicTemplate(player, stack).getAbilities().getAbilities().get(ability).getRequiredPoints();
        var hasLevelingPoints = requiredLevelingPoints <= currentLevelingPoints;

        var level = relic.getAbilityLevel(player, stack, ability);
        var maxLevel = relic.getRelicTemplate(player, stack).getAbilities().getAbilities().get(ability).getMaxLevel();
        var isMaxLevel = level >= maxLevel;

        description.add(Component.translatable("relics.description.ability.levelup.title")
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.UNDERLINE));

        description.add(newLine);

        if (isMaxLevel)
            description.add(Component.translatable("relics.description.ability.levelup.max_level")
                    .withColor(DescriptionUtils.NEGATIVE_COLOR(true)));
        else {
            description.add(Component.translatable("relics.description.general.cost.title")
                    .withStyle(ChatFormatting.BOLD)
                    .append(Component.literal(" ")));

            var relativeLevelCost = hasExperience ? EntityUtils.calculateExperienceLevelLoss(player, requiredExperience) : EntityUtils.getLevelFromTotalExperience(requiredExperience);

            description.add(Component.literal("   ● ")
                    .append(Component.translatable("relics.description.ability.levelup.cost.entry_1", hasShiftDown ? requiredExperience + "+" : requiredExperience, hasShiftDown ? relativeLevelCost + "+" : relativeLevelCost)
                            .withColor(hasExperience ? DescriptionUtils.POSITIVE_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true))));
            description.add(Component.literal("   ● ")
                    .append(Component.translatable("relics.description.ability.levelup.cost.entry_2", hasShiftDown ? requiredLevelingPoints + "+" : requiredLevelingPoints)
                            .withColor(hasLevelingPoints ? DescriptionUtils.POSITIVE_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true))));

            description.add(newLine);

            description.add(Component.translatable("relics.description.ability.levelup.auto", HotkeyRegistry.RESEARCH_RELIC.getKey().getDisplayName().getString())
                    .withColor(DescriptionUtils.NEUTRAL_COLOR(true)));
        }

        description.add(newLine);

        if (hasShiftDown)
            description.add(Component.translatable("relics.description.ability.levelup.description")
                    .withStyle(ChatFormatting.ITALIC));
        else
            description.add(Component.translatable("relics.general.hold_shift", HotkeyRegistry.RESEARCH_RELIC.getKey().getDisplayName().getString()));

        return description;
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltip = new ArrayList<>();

        var poseStack = guiGraphics.pose();

        int maxWidth = 150;
        int renderWidth = 0;

        for (var entry : this.buildDescription()) {
            var entryWidth = (minecraft.font.width(entry) + 4) / 2;

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        var height = Math.round(tooltip.size() * 5F);

        var renderX = getX() + width + 1;
        var renderY = mouseY - (height / 2) - 9;

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, height, renderX, renderY);

        var yOff = 0;

        poseStack.scale(0.5F, 0.5F, 0.5F);

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, (renderX + 10) * 2, (renderY + 9 + yOff) * 2, DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;
        }

        poseStack.scale(1F, 1F, 1F);
    }
}