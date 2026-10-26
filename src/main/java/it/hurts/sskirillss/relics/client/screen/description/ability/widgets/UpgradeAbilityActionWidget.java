package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base.AbstractAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.RelicsHotkeys;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.network.packets.leveling.PacketAbilityTweak;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Setter;
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
    private static final int SHAKE_SWINGS = 8;
    private static final float SHAKE_SCALE = 1.2F;
    private static final float SHAKE_ROTATION = 0.3F;
    private static final float SHAKE_DECAY = 0.75F;
    private static final double SHAKE_SEGMENT = 0.12D;

    @Setter
    private float shakeScale = 1F;
    @Setter
    private float shakeRotation = 0F;

    private Tween shakeTween;

    public UpgradeAbilityActionWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, PacketAbilityTweak.Operation.UPGRADE, screen);
    }

    @Override
    public boolean isLocked() {
        if (!(getScreen().getStack().getItem() instanceof IRelicItem relic))
            return true;

        return !relic.getRelicData(minecraft.player, getScreen().getStack()).getAbilitiesData().getAbilityData(getAbility()).mayPlayerUpgrade();
    }

    public void shake() {
        if (shakeTween != null)
            shakeTween.kill();

        shakeTween = Tween.create().setParallel(true);

        shakeTween.tweenMethod(this::setShakeScale, shakeScale, SHAKE_SCALE, 0.08D)
                .setEaseType(EaseType.EASE_OUT)
                .setTransitionType(TransitionType.QUAD);
        shakeTween.tweenMethod(this::setShakeScale, SHAKE_SCALE, 1F, 0.12D)
                .setDelay(0.04D)
                .setEaseType(EaseType.EASE_IN)
                .setTransitionType(TransitionType.QUAD);

        var amplitude = minecraft.player != null && minecraft.player.getRandom().nextBoolean() ? SHAKE_ROTATION : -SHAKE_ROTATION;
        var delay = 0D;
        var lastTarget = shakeRotation;

        for (var i = 0; i < SHAKE_SWINGS; i++) {
            var nextTarget = i % 2 == 0 ? amplitude : -amplitude;

            shakeTween.tweenMethod(this::setShakeRotation, lastTarget, nextTarget, SHAKE_SEGMENT)
                    .setDelay(delay)
                    .setEaseType(i == 0 ? EaseType.EASE_OUT : EaseType.EASE_IN_OUT)
                    .setTransitionType(TransitionType.QUAD);

            lastTarget = nextTarget;
            delay += SHAKE_SEGMENT;
            amplitude *= SHAKE_DECAY;
        }

        shakeTween.tweenMethod(this::setShakeRotation, lastTarget, 0F, SHAKE_SEGMENT)
                .setDelay(delay)
                .setEaseType(EaseType.EASE_IN);

        shakeTween.start();
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (getScreen().getStack().getItem() instanceof IRelicItem relic && !isLocked()) {
            var abilityData = relic.getRelicData(minecraft.player, getScreen().getStack()).getAbilitiesData().getAbilityData(getAbility());
            var template = abilityData.getTemplate();

            if (template == null)
                return;

            int level = abilityData.getLevel();
            int maxLevel = template.getInitialMaxLevel();

            handler.play(SimpleSoundInstance.forUI(RelicsSounds.TABLE_UPGRADE.get(), Screen.hasShiftDown() && abilityData.mayPlayerUpgrade() ? 2F : 1F + ((float) level / maxLevel)));
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (!(getScreen().getStack().getItem() instanceof IRelicItem relic))
            return;

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        poseStack.translate(getX() + width / 2F, getY() + height / 2F, 0F);
        poseStack.scale(shakeScale, shakeScale, 1F);
        poseStack.mulPose(Axis.ZP.rotation(shakeRotation));
        poseStack.translate(-(getX() + width / 2F), -(getY() + height / 2F), 0F);

        var isQuick = Screen.hasShiftDown() && relic.getRelicData(minecraft.player, getScreen().getStack()).getAbilitiesData().getAbilityData(getAbility()).mayPlayerUpgrade();

        var color = isQuick ? (float) (1.05F + (Math.sin((minecraft.player.tickCount + (getAbility().length() * 10)) * 0.5F) * 0.1F)) : 1F;

        RenderSystem.setShaderColor(color, color, color, 1F);

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/upgrade_button_" + (isLocked() ? "inactive" : "active") + (isQuick ? "_quick" : "") + ".png"), poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(this.getX(), this.getY())
                .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        if (this.isHovered())
            GUIRenderer.begin(DescriptionTextures.ACTION_BUTTON_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() - 1, this.getY() - 1)
                    .end();

        poseStack.popPose();
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
        var requiredExperience = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(getAbility()).getUpgradePlayerExperienceCost();
        var hasExperience = requiredExperience <= currentExperience;

        var currentLevelingPoints = relic.getRelicData(player, stack).getLevelingData().getPoints();
        var requiredLevelingPoints = relic.getRelicData(player, stack).getAbilitiesData().getAbilities().get(ability).getTemplate().getRequiredPoints();
        var hasLevelingPoints = requiredLevelingPoints <= currentLevelingPoints;

        var level = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability).getLevel();
        var maxLevel = relic.getRelicData(player, stack).getAbilitiesData().getAbilities().get(ability).getTemplate().getInitialMaxLevel();
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

            description.add(Component.translatable("relics.description.ability.levelup.auto", RelicsHotkeys.RESEARCH_RELIC.getKey().getDisplayName().getString())
                    .withColor(DescriptionUtils.NEUTRAL_COLOR(true)));
        }

        description.add(newLine);

        if (hasShiftDown)
            description.add(Component.translatable("relics.description.ability.levelup.description")
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
