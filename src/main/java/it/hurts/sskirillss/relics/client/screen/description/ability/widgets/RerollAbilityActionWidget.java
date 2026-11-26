package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.leveling.C2SRerollAbility;
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

import java.util.ArrayList;
import java.util.List;

public class RerollAbilityActionWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    private final AbilityDescriptionScreen screen;

    public RerollAbilityActionWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, 14, 14);

        this.screen = screen;
    }

    @Override
    public boolean isLocked() {
        if (DescriptionUtils.getEditableRelicSlot(minecraft.player, screen.getContainer(), screen.getSlot()) == null
                || !(screen.getStack().getItem() instanceof IRelicItem relic))
            return true;

        return !relic.getRelicData(minecraft.player, screen.getStack()).getAbilitiesData().getAbilityData(screen.getSelectedAbility()).mayPlayerReroll();
    }

    @Override
    public void playDownSound(SoundManager handler) {
        var player = minecraft.player;
        var stack = screen.getStack();

        if (isLocked() || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(screen.getSelectedAbility());

        if (abilityData.isMaxQuality() && !Screen.hasShiftDown())
            return;

        handler.play(SimpleSoundInstance.forUI(RelicsSounds.TABLE_REROLL.get(), 1F));
    }

    public List<MutableComponent> buildDescription() {
        var description = new ArrayList<MutableComponent>();

        var player = this.minecraft.player;
        var stack = screen.getStack();
        var relic = (IRelicItem) stack.getItem();

        var newLine = Component.literal(" ");

        var currentExperience = EntityUtils.getPlayerTotalExperience(player);
        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(screen.getSelectedAbility());
        var requiredExperience = abilityData.getRerollPlayerExperienceCost();
        var hasExperience = requiredExperience <= currentExperience;

        var isMaxQuality = abilityData.isMaxQuality();
        var quick = !isMaxQuality && Screen.hasShiftDown();

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
                .append(Component.translatable("relics.description.ability.reroll.cost.entry_1", quick ? (requiredExperience + "+") : requiredExperience, quick ? (relativeLevelCost + "+") : relativeLevelCost)
                        .withColor(hasExperience ? DescriptionUtils.POSITIVE_COLOR(true) : DescriptionUtils.NEGATIVE_COLOR(true))));

        description.add(newLine);

        if (isMaxQuality)
            description.add(Component.translatable("relics.description.ability.reroll.max_quality", "Shift")
                    .withColor(DescriptionUtils.NEGATIVE_COLOR(true)));
        else
            description.add(Component.translatable("relics.description.ability.reroll.auto", "Shift")
                    .withColor(DescriptionUtils.NEUTRAL_COLOR(true)));

        description.add(newLine);

        if (RelicsHotkeys.isHeld(RelicsHotkeys.RESEARCH_RELIC))
            description.add(Component.translatable("relics.description.ability.reroll.description")
                    .withStyle(ChatFormatting.ITALIC));
        else
            description.add(Component.translatable("relics.general.hold_shift", RelicsHotkeys.RESEARCH_RELIC.getTranslatedKeyMessage()));

        return description;
    }

    @Override
    public void onPress() {
        var player = minecraft.player;
        var stack = screen.getStack();

        if (isLocked() || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var ability = screen.getSelectedAbility();
        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);
        var hasWarning = abilityData.isMaxQuality();

        if (hasWarning && !Screen.hasShiftDown())
            return;

        NetworkHandler.sendToServer(new C2SRerollAbility(screen.getContainer(), screen.getSlot(), stack.getItem(), ability, hasWarning, Screen.hasShiftDown()));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var player = minecraft.player;
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var poseStack = guiGraphics.pose();

        var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(screen.getSelectedAbility());
        var isWarning = abilityData.isMaxQuality();
        var locked = isLocked();
        var isQuick = Screen.hasShiftDown() && !locked;

        var color = isQuick ? (float) (1.05F + (Math.sin((player.tickCount + (screen.getSelectedAbility().length() * 10)) * 0.5F) * 0.1F)) : 1F;

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/reroll_button_" + (locked ? "inactive" : "active" + (isWarning ? "_warning" : isQuick ? "_quick" : "")) + ".png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .color(color, color, color, 1F)
                .pos(this.getX(), this.getY())
                .end();

        if (this.isHovered())
            GUIRenderer.begin(DescriptionTextures.ACTION_BUTTON_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() - 1, this.getY() - 1)
                    .end();
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        DescriptionUtils.drawActionTooltip(guiGraphics, minecraft.font, buildDescription(), getX() + width + 1, mouseY);
    }
}
