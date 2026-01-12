package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base.AbstractAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ResetAbilityActionWidget extends AbstractAbilityActionWidget {
    public ResetAbilityActionWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, PacketAbilityTweak.Operation.RESET, screen);
    }

    @Override
    public boolean isLocked() {
        if (!(getScreen().getStack().getItem() instanceof IRelicItem relic))
            return true;

        return !relic.getRelicData(minecraft.player, getScreen().getStack()).getAbilitiesData().getAbilityData(getAbility()).mayPlayerReset(minecraft.player);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked())
            handler.play(SimpleSoundInstance.forUI(RelicsSounds.TABLE_RESET.get(), 1F));
    }

    @Override
    public List<MutableComponent> buildDescription() {
        var description = super.buildDescription();

        var screen = this.getScreen();

        var player = this.minecraft.player;
        var stack = this.getScreen().getStack();
        var relic = (IRelicItem) stack.getItem();
        var ability = screen.getSelectedAbility();

        var key = RelicsHotkeys.RESEARCH_RELIC.getKey().getValue();

        var hasShiftDown = key != GLFW.GLFW_KEY_UNKNOWN && InputConstants.isKeyDown(minecraft.getWindow().getWindow(), key);

        var newLine = Component.literal(" ");

        var currentExperience = EntityUtils.getPlayerTotalExperience(player);
        var requiredExperience = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(getAbility()).getResetPlayerExperienceCost();
        var hasExperience = requiredExperience <= currentExperience;

        var level = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability).getLevel();
        var isMinLevel = level <= 0;

        description.add(Component.translatable("relics.description.ability.reset.title")
                .append(Component.literal(":"))
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.UNDERLINE));

        description.add(newLine);

        if (isMinLevel)
            description.add(Component.translatable("relics.description.ability.reset.min_level")
                    .withColor(DescriptionUtils.NEGATIVE_COLOR(true)));
        else {
            description.add(Component.translatable("relics.description.general.cost.title")
                    .withStyle(ChatFormatting.BOLD)
                    .append(Component.literal(" ")));

            description.add(Component.literal("   ● ")
                    .append(Component.translatable("relics.description.ability.reset.cost.entry_1", requiredExperience, (hasExperience ? EntityUtils.calculateExperienceLevelLoss(player, requiredExperience) : EntityUtils.getLevelFromTotalExperience(requiredExperience)))
                            .withColor(hasExperience ? DescriptionUtils.POSITIVE_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true))));
        }
        description.add(newLine);

        if (hasShiftDown)
            description.add(Component.translatable("relics.description.ability.reset.description")
                    .withStyle(ChatFormatting.ITALIC));
        else
            description.add(Component.translatable("relics.general.hold_shift", RelicsHotkeys.RESEARCH_RELIC.getKey().getDisplayName().getString()));

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
