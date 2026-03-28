package it.hurts.sskirillss.relics.client.screen.description.synergy.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.description.DescriptionSubcategories;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.synergy.SynergyDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class SynergyCardWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private final SynergyDescriptionScreen screen;
    @Getter
    private final String synergy;

    @Getter
    @Setter
    private float clickXSqueeze = 1F;
    @Getter
    @Setter
    private float clickYSqueeze = 1F;
    @Getter
    @Setter
    private float clickZRotation = 0F;

    public Tween hoverTween;
    private boolean hasHovered = false;

    @Getter
    @Setter
    private float hoverXSqueeze = 1F;
    @Getter
    @Setter
    private float hoverYSqueeze = 1F;

    public SynergyCardWidget(int x, int y, SynergyDescriptionScreen screen, String synergy) {
        super(x, y, 38, 51);

        this.screen = screen;
        this.synergy = synergy;
    }

    @Override
    public void onPress() {
        var player = minecraft.player;
        var stack = screen.getStack();
        var random = player.getRandom();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(synergy);
        var isUnlocked = synergyData.isUnlocked();

        SoundManager soundManager = minecraft.getSoundManager();

        if (isUnlocked) {
            if (!screen.getSelectedSynergy().equals(synergy)) {
                screen.setSelectedSynergy(synergy);

                var subcategories = DescriptionSubcategories.getSubcategories().values().stream()
                        .filter(subcategory -> subcategory.shouldAppear(this.screen, player, stack))
                        .toList();

                if (!subcategories.contains(screen.getSubcategory()))
                    screen.setSubcategory(DescriptionSubcategories.getSubcategory("synergy_description"));

                screen.rebuildWidgets();

                for (var entry : screen.renderables) {
                    if (!(entry instanceof SynergyCardWidget card) || !card.synergy.equals(synergy))
                        continue;
                }
            }
        } else {
            var overshootFactor = 0.035F;
            var overshoot = 1F + overshootFactor;

            var tween = Tween.create().setParallel(true);

            tween.tweenMethod(this::setClickYSqueeze, this.getClickYSqueeze(), overshoot, 0.08D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.QUAD);
            tween.tweenMethod(this::setClickXSqueeze, this.getClickXSqueeze(), overshoot, 0.08D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.QUAD);

            tween.tweenMethod(this::setClickYSqueeze, overshoot, 1F, 0.12D)
                    .setDelay(0.04D)
                    .setEaseType(EaseType.EASE_IN)
                    .setTransitionType(TransitionType.QUAD);
            tween.tweenMethod(this::setClickXSqueeze, overshoot, 1F, 0.12D)
                    .setDelay(0.04D)
                    .setEaseType(EaseType.EASE_IN)
                    .setTransitionType(TransitionType.QUAD);

            var initialRotation = this.getClickZRotation();
            var rotationBase = 0.025F;
            var amplitude = random.nextBoolean() ? rotationBase : -rotationBase;
            var decay = 0.75F;
            var segmentDuration = 0.25D;
            var delay = 0D;
            var lastTarget = initialRotation;

            for (var i = 0; i < 10; i++) {
                var nextTarget = (i % 2 == 0 ? amplitude : -amplitude);

                tween.tweenMethod(this::setClickZRotation, lastTarget, nextTarget, segmentDuration)
                        .setDelay(delay)
                        .setEaseType(i == 0 ? EaseType.EASE_OUT : EaseType.EASE_IN_OUT)
                        .setTransitionType(TransitionType.QUAD);

                lastTarget = nextTarget;
                delay += segmentDuration;
                amplitude *= decay;
            }

            tween.tweenMethod(this::setClickZRotation, lastTarget, 0f, segmentDuration)
                    .setDelay(delay)
                    .setEaseType(EaseType.EASE_IN);

            tween.start();

            soundManager.play(SimpleSoundInstance.forUI(SoundEvents.CHAIN_BREAK, 1F));
        }
    }

    public void rebuildActionButtons() {
        this.screen.children().removeIf(entry -> entry instanceof SynergyModeWidget);
        this.screen.initModeButtons();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;

        if (player == null || !(this.screen.stack.getItem() instanceof IRelicItem relic))
            return;

        var stack = this.screen.getStack();
        var poseStack = guiGraphics.pose();

        var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(this.synergy);

        var canBeUpgraded = synergyData.canBeUpgraded();
        var canUse = synergyData.isUnlocked();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.enableBlend();

        poseStack.pushPose();

        poseStack.translate((this.getX() + (this.width / 2F)), (this.getY() + (this.height / 2F)), 0);

        poseStack.scale(this.getHoverXSqueeze(), this.getHoverYSqueeze(), 1F);
        poseStack.scale(this.getClickXSqueeze(), this.getClickYSqueeze(), 1F);

        poseStack.mulPose(Axis.ZP.rotation(this.getClickZRotation()));

        var color = (float) (1.05F + (Math.sin((player.tickCount + (synergy.length() * 10)) * 0.2F) * 0.1F));

        GUIRenderer.begin(DescriptionTextures.getSynergyCardTexture(stack, synergy), poseStack)
                .color(color, color, color, 1F)
                .texSize(22, 31)
                .pos(0, 1)
                .end();

        if (!canUse)
            GUIRenderer.begin(DescriptionTextures.ABILITY_SMALL_CARD_LOCK_BACKGROUND, poseStack)
                    .pos(0, 1)
                    .end();

        GUIRenderer.begin(canUse ? DescriptionTextures.SYNERGY_SMALL_CARD_FRAME_ACTIVE : DescriptionTextures.SYNERGY_SMALL_CARD_FRAME_INACTIVE, poseStack)
                .end();

        if (isHovered())
            GUIRenderer.begin(DescriptionTextures.SYNERGY_SMALL_CARD_FRAME_SELECTION, poseStack)
                    .end();

        if (!canUse) {
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/synergy/small_card_frame_progress_slug_inactive.png"), poseStack)
                    .end();

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/synergy/chains_inactive.png"), poseStack)
                    .pos(0, 0.5F)
                    .end();

            GUIRenderer.begin(DescriptionTextures.LOCK_INACTIVE, poseStack)
                    .pos(0, -2)
                    .end();

            var textScale = 0.5F;

            poseStack.pushPose();

            poseStack.scale(textScale, textScale, textScale);

            var requiredConditionsComponent = Component.literal(String.valueOf(relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(synergy).getTemplate().getRelicConditions().size())).withStyle(ChatFormatting.BOLD);

            guiGraphics.drawString(minecraft.font, requiredConditionsComponent, (-(width / 2) + 19) * 2 - minecraft.font.width(requiredConditionsComponent) / 2, (-(height / 2) + 24) * 2, 0xB7AED9, true);

            poseStack.popPose();
        }

        var progress = synergyData.getProgress();

        if (canUse)
            drawProgressBar(guiGraphics, (-this.width / 2F) + 2, (-this.height / 2F) + 3F, (float) progress);

        if (canBeUpgraded) {
            var title = Component.literal(canUse ? ((int) (progress * 100)) + "%" : "?").withStyle(ChatFormatting.BOLD);

            var textScale = 0.5F;

            poseStack.scale(textScale, textScale, textScale);

            guiGraphics.drawString(minecraft.font, title, -((width + 1) / 2) - (minecraft.font.width(title) / 2) + 35, (-(height / 2) - 20), canUse ? 0xFFE278 : 0xB7AED9, true);
        }

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public static void drawProgressBar(GuiGraphics gui, float x, float y, float progress) {
        progress = 1F;

        var atlasWidth = 38;
        var atlasHeight = 51;

        var frameU = 2;
        var frameV = 3;
        var frameWidth = 34;
        var frameHeight = 46;

        var borderThickness = 3;
        var cornerSize = 7;

        var topStartU = 7;
        var topEndU = 15;
        var rightEdgeTopV = 6;

        var segTop = topEndU - topStartU + 1;
        var segCornerTopLeft = cornerSize;
        var segLeftEdge = frameHeight - cornerSize - borderThickness;

        var segBottom = frameWidth - cornerSize;
        var segCornerBotRight = cornerSize;
        var segRightEdge = frameHeight - cornerSize - rightEdgeTopV;

        var totalLength =
                segTop
                        + segCornerTopLeft
                        + segLeftEdge
                        + segBottom
                        + segCornerBotRight
                        + segRightEdge;

        var remaining = (int) (Mth.clamp(progress, 0f, 1f) * totalLength);

        if (remaining <= 0)
            return;

        var renderer = GUIRenderer
                .begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/synergy/small_card_filler.png"), gui.pose())
                .texSize(atlasWidth, atlasHeight)
                .anchor(SpriteAnchor.TOP_LEFT);

        var drawLen = 0;
        var moveLen = 0;

        drawLen = Math.min(remaining, segTop);

        var u0 = topEndU - drawLen + 1;

        renderer.pos(x + u0, y)
                .patternSize(drawLen, borderThickness)
                .texOff(frameU + u0, frameV)
                .end();

        remaining -= drawLen;

        if (remaining > 0) {
            drawLen = Math.min(remaining, segCornerTopLeft);

            var sliceW = drawLen;
            var sliceU = cornerSize - sliceW;

            renderer.pos(x + sliceU, y)
                    .patternSize(sliceW, cornerSize)
                    .texOff(frameU + sliceU, frameV)
                    .end();

            remaining -= drawLen;
        }

        if (remaining > 0) {
            moveLen = Math.min(remaining, segLeftEdge);

            renderer.pos(x, y + cornerSize)
                    .patternSize(borderThickness, moveLen)
                    .texOff(frameU, frameV + cornerSize)
                    .end();

            remaining -= moveLen;
        }

        if (remaining > 0) {
            drawLen = Math.min(remaining, segBottom);

            renderer.pos(x, y + frameHeight - borderThickness)
                    .patternSize(drawLen, borderThickness)
                    .texOff(frameU, frameV + frameHeight - borderThickness)
                    .end();

            remaining -= drawLen;
        }

        if (remaining > 0) {
            drawLen = Math.min(remaining, segCornerBotRight);

            var sliceW = drawLen;
            var u4 = frameWidth - cornerSize;
            var v4 = frameHeight - cornerSize;

            renderer.pos(x + u4, y + v4)
                    .patternSize(sliceW, cornerSize)
                    .texOff(frameU + u4, frameV + v4)
                    .end();

            remaining -= drawLen;
        }

        if (remaining > 0) {
            moveLen = Math.min(remaining, segRightEdge);

            var v5 = rightEdgeTopV + (segRightEdge - moveLen);

            renderer.pos(x + frameWidth - borderThickness, y + v5)
                    .patternSize(borderThickness, moveLen)
                    .texOff(frameU + frameWidth - borderThickness, frameV + v5)
                    .end();
        }
    }

    @Override
    public void onTick() {
        var hovered = this.isHovered();

        var overshoot = 1.075F;

        if (hovered && !hasHovered) {
            hasHovered = true;

            if (hoverTween != null)
                hoverTween.kill();

            hoverTween = Tween.create().setParallel(true);

            hoverTween.tweenMethod(this::setHoverYSqueeze, 1F, overshoot, 0.25D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.QUAD);
            hoverTween.tweenMethod(this::setHoverXSqueeze, 1F, overshoot, 0.25D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.QUAD);

            hoverTween.start();
        } else if (!hovered && hasHovered) {
            hasHovered = false;

            if (hoverTween != null)
                hoverTween.kill();

            hoverTween = Tween.create().setParallel(true);

            hoverTween.tweenMethod(this::setHoverYSqueeze, overshoot, 1F, 0.25D)
                    .setEaseType(EaseType.EASE_IN)
                    .setTransitionType(TransitionType.QUAD);
            hoverTween.tweenMethod(this::setHoverXSqueeze, overshoot, 1F, 0.25D)
                    .setEaseType(EaseType.EASE_IN)
                    .setTransitionType(TransitionType.QUAD);

            hoverTween.start();
        }
    }

    @Override
    public boolean isLocked() {
        return screen.getSelectedSynergy().equals(synergy);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var player = minecraft.player;
        var stack = screen.stack;

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(synergy);
        var data = synergyData.getTemplate();

        if (data == null)
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        var title = Component.translatableWithFallback("relics.description." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".synergy." + synergy, synergy);

        int maxWidth = 110;
        int renderWidth = Math.min((minecraft.font.width(title.withStyle(ChatFormatting.BOLD)) / 2) + 4, maxWidth);

        List<MutableComponent> entries = new ArrayList<>();

        entries.add(Component.literal(" "));

        int level = relic.getRelicData(player, stack).getLevelingData().getLevel();
        // TODO
//        int requiredLevel = data.getRequiredLevel();
//
//        if (level < requiredLevel) {
//            entries.add(Component.literal(" "));
//
//            entries.add(Component.literal("").append(Component.translatable("relics.description.researching.relic.card.low_level", Component.literal(String.valueOf(requiredLevel)).withStyle(ChatFormatting.BOLD))));
//        } else {
//            if (!synergyData.getLockData().isUnlocked()) {
//                entries.add(Component.literal(" "));
//
//                entries.add(Component.literal("").append(Component.translatable("relics.description.researching.relic.card.ready_to_unlock", Component.literal(String.valueOf(synergyData.getLockData().getMaxUnlocks() - synergyData.getLockData().getUnlocks())).withStyle(ChatFormatting.BOLD))));
//            } else {
//                if (!synergyData.getResearchData().isResearched()) {
//                    entries.add(Component.literal(" "));
//
//                    entries.add(Component.literal("").append(Component.translatable("relics.description.researching.relic.card.unresearched")));
//                } else if (synergyData.mayPlayerUpgrade(player)) {
//                    entries.add(Component.literal(" "));
//
//                    entries.add(Component.literal("").append(Component.translatable("relics.description.researching.relic.card.ready_to_upgrade")));
//                }
//            }
//        }

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry)) / 2;

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 4, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        int height = tooltip.size() * 5;

        int y = getHeight() / 2;

        poseStack.translate((getX() + (getWidth() / 2F)), (getY() + (getHeight() / 2F)), 0);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, height, -((renderWidth + 19) / 2), y);

        int yOff = 0;

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        if (!synergyData.isUnlocked()) {
            title = ScreenUtils.stylizeWithReplacement(title, 1F, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(0x9E00B0), synergy.length());

            var random = player.getRandom();

            var shakeX = MathUtils.randomFloat(random) * 0.5F;
            var shakeY = MathUtils.randomFloat(random) * 0.5F;

            poseStack.translate(shakeX, shakeY, 0F);

        } else
            title.withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, title, -(minecraft.font.width(title) / 2), ((y + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();

        for (FormattedCharSequence entry : tooltip) {
            poseStack.pushPose();

            poseStack.scale(0.5F, 0.5F, 0.5F);

            guiGraphics.drawString(minecraft.font, entry, -(minecraft.font.width(entry) / 2), ((y + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;

            poseStack.popPose();
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked() && screen.getStack().getItem() instanceof IRelicItem relic
                && relic.getRelicData(minecraft.player, screen.stack).getAbilitiesData().getSynergyData(synergy).isUnlocked())
            super.playDownSound(handler);
    }
}
