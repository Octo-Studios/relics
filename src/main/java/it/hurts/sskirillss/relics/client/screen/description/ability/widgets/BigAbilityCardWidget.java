package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.misc.PointsRefundEffect;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.BigRelicCardWidget;
import it.hurts.sskirillss.relics.client.screen.utils.misc.AnimatedValue;
import it.hurts.sskirillss.relics.client.screen.utils.misc.DeltaCounter;
import it.hurts.sskirillss.relics.client.screen.utils.misc.FloatingTexts;
import it.hurts.sskirillss.relics.client.screen.utils.misc.GrayscaleTextures;
import it.hurts.sskirillss.relics.client.screen.utils.misc.LevelUpEffect;
import it.hurts.sskirillss.relics.client.screen.utils.misc.RerollAnimation;
import it.hurts.sskirillss.relics.client.screen.utils.misc.UIParticles;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class BigAbilityCardWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    public static final int STAR_COUNT = 5;

    private static final int STAR_TINT = UIParticles.GOLD;
    private static final int LEVEL_UP_COLOR = 0xFFE278;
    private static final int LEVEL_DOWN_COLOR = RerollAnimation.TINT_DOWN;

    private static final int MAX_PARTICLES = 200;
    private static final int LEVEL_UP_PARTICLES = 40;
    private static final int LEVEL_UP_PARTICLES_PER_LEVEL = 8;
    private static final int REROLL_PARTICLES_MAXED = 220;
    private static final int REROLL_PARTICLES_UP = 60;
    private static final int REROLL_PARTICLES_UP_PER_STAR = 20;
    private static final int REROLL_PARTICLES_UP_MAX = 180;
    private static final int REROLL_PARTICLES_DOWN = 30;
    private static final int REROLL_PARTICLES_DOWN_PER_STAR = 6;
    private static final int REROLL_PARTICLES_SAME = 45;
    private static final int STAR_BURST_PARTICLES = 10;
    private static final int STAR_BURST_PARTICLES_PER_STATE = 4;
    private static final int STAR_CRUMBLE_PARTICLES = 8;
    private static final int RESET_CRUMBLE_PARTICLES = 14;

    private static final int BASE_ROLLBACK_DURATION = 10;
    private static final int MIN_ROLLBACK_DURATION = 5;
    private static final int MAX_ROLLBACK_DURATION = 30;
    private static final int ROLLBACK_PER_LEVEL = 3;


    private static final float COUNTER_OFFSET_Y = 8F;
    private static final float RESET_CRUMBLE_OFFSET_Y = 10F;
    private static final float REROLL_TEXT_OFFSET_Y = 66F;
    private static final float REROLL_TEXT_SPREAD = 18F;

    private static final float STAR_OFFSET_X = 5F;
    private static final float STAR_OFFSET_Y = 72F;
    private static final float STAR_STEP = 8F;
    private static final float STAR_CENTER_X = 4F;
    private static final float STAR_CENTER_Y = 3.5F;

    private static final float PUNCH_DECAY = 0.7F;
    private static final float PUNCH_THRESHOLD = 0.0005F;
    private static final float RESET_PUNCH = -0.07F;
    private static final float REROLL_PUNCH_UP = 0.04F;
    private static final float REROLL_PUNCH_UP_PER_STAR = 0.01F;
    private static final float REROLL_PUNCH_UP_MAX = 0.09F;
    private static final float REROLL_PUNCH_DOWN = -0.03F;
    private static final float REROLL_PUNCH_SAME = -0.012F;

    private static final float FLASH_STRENGTH = 0.9F;

    private static final int MODE_DOT_WIDTH = 4;
    private static final int MODE_FIELD_WIDTH = 30;
    private static final int MODE_DOT_X = 11;
    private static final int MODE_DOT_Y = 15;
    private static final int MODE_SPARKS = 6;
    private static final float MODE_FLASH_STRENGTH = 0.8F;

    private AbilityDescriptionScreen screen;

    private final LevelUpEffect levelUpEffect = new LevelUpEffect();
    private final LevelUpEffect rerollEffect = new LevelUpEffect();
    private final RerollAnimation rerollAnimation = new RerollAnimation();
    private final FloatingTexts floatingTexts = new FloatingTexts();
    private final AnimatedValue shownLevel = new AnimatedValue();
    private final DeltaCounter levelCounter = new DeltaCounter(floatingTexts);
    private final PointsRefundEffect pointsRefund = new PointsRefundEffect();

    private String lastAbility = null;
    private int lastLevel = -1;

    private float punch = 0F;
    private float punchO = 0F;

    public BigAbilityCardWidget(int x, int y, AbilityDescriptionScreen screen) {
        super(x, y, 50, 87);

        this.screen = screen;
    }

    @Override
    public void onTick() {
        var stack = screen.getStack();

        if (minecraft.player == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var ability = screen.getSelectedAbility();
        var abilityData = relic.getRelicData(minecraft.player, stack).getAbilitiesData().getAbilityData(ability);

        if (abilityData == null)
            return;

        var random = minecraft.player.getRandom();

        updateLevel(random, stack, ability, abilityData);
        updateMode(random, ability, abilityData);
        pointsRefund.tick(screen, random);

        levelUpEffect.tick();
        rerollEffect.tick();

        if (rerollAnimation.observe(abilityData, ability))
            onReroll(random);

        rerollAnimation.tick();

        punchO = punch;
        punch = Math.abs(punch) < PUNCH_THRESHOLD ? 0F : punch * PUNCH_DECAY;

        floatingTexts.tick();
    }

    public void applyPunch(PoseStack poseStack, float partialTick) {
        var scale = 1F + Mth.lerp(partialTick, punchO, punch);

        poseStack.translate(getX() + width / 2F, getY() + height / 2F, 0F);
        poseStack.scale(scale, scale, 1F);
        poseStack.translate(-(getX() + width / 2F), -(getY() + height / 2F), 0F);
    }

    public static int getStarState(int quality, int index) {
        var full = quality / 2;

        if (index < full)
            return 2;

        return index == full && quality % 2 == 1 ? 1 : 0;
    }

    private void updateLevel(RandomSource random, ItemStack stack, String ability, AbilityData abilityData) {
        var level = abilityData.getLevel();
        var known = ability.equals(lastAbility) && lastLevel >= 0;

        if (!known) {
            shownLevel.snap(level);
            levelCounter.reset();
        }

        if (known && level > lastLevel) {
            levelUpEffect.start(screen, random, getX(), getY(), width, height, BigRelicCardWidget.getTextureColor(DescriptionTextures.getAbilityCardTexture(stack, ability)), Math.min(LEVEL_UP_PARTICLES + level * LEVEL_UP_PARTICLES_PER_LEVEL, MAX_PARTICLES));

            shownLevel.set(level, Mth.clamp(Math.round(Math.abs(level - shownLevel.getValue()) * ROLLBACK_PER_LEVEL), MIN_ROLLBACK_DURATION, MAX_ROLLBACK_DURATION));
        }

        if (known && level < lastLevel)
            onReset(random, abilityData.getTemplate().getRequiredPoints(), lastLevel, level);

        lastAbility = ability;
        lastLevel = level;

        levelCounter.update(shownLevel.getInt(level), shownLevel.isMoving(), LEVEL_UP_COLOR, LEVEL_DOWN_COLOR, getX() + width / 2F, getY() + COUNTER_OFFSET_Y);
    }

    private void updateMode(RandomSource random, String ability, AbilityData abilityData) {
        var template = abilityData.getTemplate();

        if (template == null || template.getModes().isEmpty() || !abilityData.isUnlocked())
            return;

        var modes = template.getModes();
        var index = modes.indexOf(abilityData.getMode());

        if (index >= 0 && screen.getModeSlide().update(ability, index))
            LevelUpEffect.burstAt(screen, random, getModeDotX(modes.size(), index) + MODE_DOT_WIDTH / 2F, getY() + MODE_DOT_Y + MODE_DOT_WIDTH / 2F, STAR_TINT, MODE_SPARKS);
    }

    private void onReset(RandomSource random, int requiredPoints, int oldLevel, int newLevel) {
        var centerX = getX() + width / 2F;

        var speed = PointsRefundEffect.getSpeed((oldLevel - newLevel) * requiredPoints);

        punch = RESET_PUNCH;
        punchO = punch;

        shownLevel.set(newLevel, Math.round(BASE_ROLLBACK_DURATION * speed));

        LevelUpEffect.crumbleAt(screen, random, centerX, getY() + RESET_CRUMBLE_OFFSET_Y, LEVEL_DOWN_COLOR, RESET_CRUMBLE_PARTICLES);

        pointsRefund.start(screen, random, centerX, getY() + height / 2F, oldLevel - newLevel, requiredPoints);
    }

    private void onReroll(RandomSource random) {
        var oldQuality = rerollAnimation.getOldQuality();
        var newQuality = rerollAnimation.getNewQuality();
        var delta = newQuality - oldQuality;
        var outcome = rerollAnimation.getOutcome();
        var tint = rerollAnimation.tint();

        for (var i = 0; i < STAR_COUNT; i++) {
            var before = getStarState(oldQuality, i);
            var after = getStarState(newQuality, i);

            var starX = getX() + i * STAR_STEP + STAR_OFFSET_X + STAR_CENTER_X;
            var starY = getY() + STAR_OFFSET_Y + STAR_CENTER_Y;

            if (after > before)
                LevelUpEffect.burstAt(screen, random, starX, starY, STAR_TINT, STAR_BURST_PARTICLES + after * STAR_BURST_PARTICLES_PER_STATE);
            else if (after < before)
                LevelUpEffect.crumbleAt(screen, random, starX, starY, STAR_TINT, STAR_CRUMBLE_PARTICLES);
        }

        var count = rerollAnimation.isMaxed() ? REROLL_PARTICLES_MAXED
                : outcome > 0 ? Math.min(REROLL_PARTICLES_UP + delta * REROLL_PARTICLES_UP_PER_STAR, REROLL_PARTICLES_UP_MAX)
                : outcome < 0 ? REROLL_PARTICLES_DOWN + Math.abs(delta) * REROLL_PARTICLES_DOWN_PER_STAR
                : REROLL_PARTICLES_SAME;

        rerollEffect.start(screen, random, getX(), getY(), width, height, tint, count);

        punch = outcome > 0 ? Math.min(REROLL_PUNCH_UP + delta * REROLL_PUNCH_UP_PER_STAR, REROLL_PUNCH_UP_MAX) : outcome < 0 ? REROLL_PUNCH_DOWN : REROLL_PUNCH_SAME;
        punchO = punch;

        if (delta != 0)
            floatingTexts.add(String.format(Locale.ROOT, "%+.1f", delta / 2F).replace(".0", ""), tint, getX() + width / 2F + (random.nextFloat() - 0.5F) * REROLL_TEXT_SPREAD, getY() + REROLL_TEXT_OFFSET_Y, -1F);
    }

    private float getModeDotX(int amount, float index) {
        var margin = (MODE_FIELD_WIDTH - amount * MODE_DOT_WIDTH) / (amount + 1);

        return getX() + MODE_DOT_X + margin * (index + 1) + MODE_DOT_WIDTH * index;
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

        var canBeLeveledUp = abilityData.getTemplate().getInitialMaxLevel() > 0;
        var hasStats = !abilityData.getTemplate().getStats().isEmpty();

        poseStack.pushPose();

        applyPunch(poseStack, pPartialTick);

        var color = (float) (1.05F + (Math.sin((player.tickCount + (ability.length() * 10)) * 0.2F) * 0.1F)) + (levelUpEffect.flash(pPartialTick) + rerollEffect.flash(pPartialTick)) * FLASH_STRENGTH;

        if (isUnlocked)
            GUIRenderer.begin(abilityData.isEnabled() ? DescriptionTextures.getAbilityCardTexture(stack, ability) : GrayscaleTextures.get(DescriptionTextures.getAbilityCardTexture(stack, ability)), poseStack)
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
                .pos(this.getX(), this.getY())
                .end();

        if (!canBeLeveledUp)
            GUIRenderer.begin(isUnlocked ? ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/big_card_frame_level_slug_active.png") : ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/big_card_frame_level_slug_inactive.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX(), this.getY())
                    .end();

        var modes = template.getModes();

        if (isUnlocked && !modes.isEmpty()) {
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_list.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX() + 8, this.getY() + 11)
                    .end();

            var amount = modes.size();
            var slide = screen.getModeSlide();

            for (int index = 0; index < amount; index++) {
                var brightness = 1F + (index == modes.indexOf(abilityData.getMode()) ? slide.flash(pPartialTick) * MODE_FLASH_STRENGTH : 0F);

                GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_point.png"), poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .color(brightness, brightness, brightness, 1F)
                        .pos(getModeDotX(amount, index), this.getY() + MODE_DOT_Y)
                        .end();
            }

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/ability_mode_selection.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getModeDotX(amount, slide.position(pPartialTick)) - 1, this.getY() + MODE_DOT_Y - 1)
                    .end();
        }

        if (isUnlocked && hasStats) {
            var quality = abilityData.calculateQuality();

            for (int i = 0; i < STAR_COUNT; i++) {
                var starX = getX() + i * STAR_STEP + STAR_OFFSET_X;

                GUIRenderer.begin(DescriptionTextures.BIG_STAR_HOLE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(starX, getY() + STAR_OFFSET_Y)
                        .end();

                var state = getStarState(quality, i);

                if (state == 0)
                    continue;

                var brightness = 1F + (state > getStarState(rerollAnimation.getOldQuality(), i) ? rerollAnimation.starFlash(pPartialTick) : 0F);

                var renderer = GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .color(brightness, brightness, brightness, 1F)
                        .pos(starX, getY() + STAR_OFFSET_Y);

                if (state == 1)
                    renderer.patternSize(4, 7).texSize(8, 7);

                renderer.end();
            }
        }

        floatingTexts.render(guiGraphics, minecraft.font, pPartialTick);

        if (canBeLeveledUp) {
            poseStack.pushPose();

            var pointsComponent = Component.literal(isUnlocked ? String.valueOf(shownLevel.getInt(abilityData.getLevel())) : "?").withStyle(ChatFormatting.BOLD);

            poseStack.scale(0.75F, 0.75F, 1F);

            guiGraphics.drawString(minecraft.font, pointsComponent, (int) (((getX() + 26.5F) * 1.33F) - (minecraft.font.width(pointsComponent) / 2F)), (int) ((getY() + 4.5F) * 1.33F), isUnlocked ? 0xFFE278 : 0xB7AED9, false);

            poseStack.popPose();
        }

        if (isUnlocked && canBeLeveledUp && this.isHovered()) {
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
