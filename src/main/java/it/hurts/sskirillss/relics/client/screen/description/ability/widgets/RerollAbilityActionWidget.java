package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base.AbstractAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
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

public class RerollAbilityActionWidget extends AbstractAbilityActionWidget {
    public RerollAbilityActionWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, PacketAbilityTweak.Operation.REROLL, screen);
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
                || (relic.calculateAbilityQuality(player, stack, getAbility()) == relic.getAbilityMaxQuality(player, stack, getAbility()) && !Screen.hasShiftDown()))
            return;

        handler.play(SimpleSoundInstance.forUI(RelicsSounds.TABLE_REROLL.get(), 1F));
    }

    @Override
    public List<MutableComponent> buildDescription() {
        var description = super.buildDescription();

        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();

        var key = RelicsHotkeys.RESEARCH_RELIC.getKey().getValue();

        var hasShiftDown = key != GLFW.GLFW_KEY_UNKNOWN && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key);

        var newLine = Component.literal(" ");

        var currentExperience = EntityUtils.getPlayerTotalExperience(player);
        var requiredExperience = relic.getRerollPlayerExperienceCost(player, stack, getAbility());
        var hasExperience = requiredExperience <= currentExperience;

        var quality = relic.calculateRelicQuality(player, stack);
        var maxQuality = relic.getRelicMaxQuality(player, stack);

        var isMaxQuality = quality >= maxQuality;

        description.add(Component.translatable("relics.description.ability.reroll.title")
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.UNDERLINE));

        description.add(newLine);

        description.add(Component.translatable("relics.description.general.cost.title")
                .append(Component.literal(":"))
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.UNDERLINE));

        var relativeLevelCost = hasExperience ? EntityUtils.calculateExperienceLevelLoss(player, requiredExperience) : EntityUtils.getLevelFromTotalExperience(requiredExperience);

        description.add(Component.literal("   ● ")
                .append(Component.translatable("relics.description.ability.reroll.cost.entry_1", !isMaxQuality && hasShiftDown ? (requiredExperience + "+") : requiredExperience, !isMaxQuality && hasShiftDown ? (relativeLevelCost + "+") : relativeLevelCost)
                        .withColor(hasExperience ? DescriptionUtils.POSITIVE_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true))));

        description.add(newLine);

        if (isMaxQuality)
            description.add(Component.translatable("relics.description.ability.reroll.max_quality", RelicsHotkeys.RESEARCH_RELIC.getKey().getDisplayName().getString())
                    .withColor(DescriptionUtils.NEGATIVE_COLOR(true)));
        else
            description.add(Component.translatable("relics.description.ability.reroll.auto", RelicsHotkeys.RESEARCH_RELIC.getKey().getDisplayName().getString())
                    .withColor(DescriptionUtils.NEUTRAL_COLOR(true)));

        description.add(newLine);

        if (hasShiftDown)
            description.add(Component.translatable("relics.description.ability.reroll.description")
                    .withStyle(ChatFormatting.ITALIC));
        else
            description.add(Component.translatable("relics.general.hold_shift", RelicsHotkeys.RESEARCH_RELIC.getKey().getDisplayName().getString()));

        return description;
    }

    @Override
    public void onPress() {
        var player = minecraft.player;
        var stack = getScreen().getStack();

        if (isLocked() || !(getScreen().getStack().getItem() instanceof IRelicItem relic))
            return;

        boolean hasWarning = relic.calculateAbilityQuality(player, stack, getAbility()) == relic.getAbilityMaxQuality(player, stack, getAbility());

        if (hasWarning && !Screen.hasShiftDown())
            return;

        NetworkHandler.sendToServer(new PacketAbilityTweak(getScreen().getContainer(), getScreen().getSlot(), getAbility(), PacketAbilityTweak.Operation.REROLL, !hasWarning && Screen.hasShiftDown()));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var player = minecraft.player;
        var stack = getScreen().getStack();

        if (!(getScreen().getStack().getItem() instanceof IRelicItem relic))
            return;

        var poseStack = guiGraphics.pose();

        var isWarning = relic.calculateAbilityQuality(player, stack, getAbility()) == relic.getAbilityMaxQuality(player, stack, getAbility());
        var isQuick = Screen.hasShiftDown() && relic.mayPlayerReroll(player, getScreen().getStack(), getAbility());

        var color = (isWarning && Screen.hasShiftDown()) || isQuick ? (float) (1.05F + (Math.sin((player.tickCount + (getAbility().length() * 10)) * 0.5F) * 0.1F)) : 1F;

        RenderSystem.setShaderColor(color, color, color, 1F);

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/reroll_button_" + (isLocked() ? "inactive" : "active" + (isWarning ? "_warning" : isQuick ? "_quick" : "")) + ".png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(this.getX(), this.getY())
                .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        if (this.isHovered())
            GUIRenderer.begin(DescriptionTextures.ACTION_BUTTON_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() - 1, this.getY() - 1)
                    .end();
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