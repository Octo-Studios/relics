package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.octostudios.octolib.client.particle.UIParticle;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.particle.PixelUIParticle;
import it.hurts.sskirillss.relics.init.HotkeyRegistry;
import it.hurts.sskirillss.relics.init.SoundRegistry;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketRelicTweak;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RankupRelicActionWidget extends AbstractRelicActionWidget {
    public RankupRelicActionWidget(int x, int y, RelicDescriptionScreen screen) {
        super(x, y, PacketRelicTweak.Operation.RANKUP, screen);
    }

    @Override
    public boolean isLocked() {
        return !(getScreen().getStack().getItem() instanceof IRelicItem relic) || !relic.mayPlayerRankup(minecraft.player, getScreen().getStack());
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked())
            handler.play(SimpleSoundInstance.forUI(SoundRegistry.TABLE_REROLL.get(), 1F));
    }

    @Override
    public void onTick() {
        super.onTick();
    }

    @Override
    public List<MutableComponent> buildDescription() {
        var description = super.buildDescription();

        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();

        var rank = relic.getRelicRank(player, stack);
        var maxRank = relic.getRelicTemplate(player, stack).getLeveling().getMaxRank();
        var isMaxRank = rank >= maxRank;

        var level = relic.getRelicLevel(player, stack);
        var maxLevel = relic.getRelicTemplate(player, stack).getLeveling().getMaxLevel();
        var isMaxLevel = level >= maxLevel;

        var key = HotkeyRegistry.RESEARCH_RELIC.getKey().getValue();

        var hasShiftDown = key != GLFW.GLFW_KEY_UNKNOWN && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key);

        var newLine = Component.literal(" ");

        description.add(Component.translatable("relics.description.relic.rankup.title")
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.UNDERLINE));

        description.add(newLine);

        if (!isMaxRank) {
            description.add(Component.translatable("relics.description.general.cost.title")
                    .append(Component.literal(":"))
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.UNDERLINE));

            description.add(Component.literal("   ● ")
                    .append(Component.translatable("relics.description.relic.rankup.cost.entry_1", maxLevel)
                            .withColor(isMaxLevel ? DescriptionUtils.POSITIVE_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true))));

            if (isMaxLevel) {
                description.add(newLine);

                description.add(Component.translatable("relics.description.relic.rankup.warning", HotkeyRegistry.RESEARCH_RELIC.getKey().getDisplayName().getString())
                        .withColor(hasShiftDown ? DescriptionUtils.NEUTRAL_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true)));
            }
        } else
            description.add(Component.empty()
                    .append(Component.translatable("relics.description.relic.rankup.max_rank", maxLevel)
                            .withColor(DescriptionUtils.NEGATIVE_COLOR(true))));

        description.add(newLine);

        if (hasShiftDown)
            description.add(Component.translatable("relics.description.relic.rankup.description")
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