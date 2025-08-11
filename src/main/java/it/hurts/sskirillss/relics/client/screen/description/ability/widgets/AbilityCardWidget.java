package it.hurts.sskirillss.relics.client.screen.description.ability.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.octostudios.octolib.client.particle.ExtendedUIParticle;
import it.hurts.octostudios.octolib.client.particle.UIParticle;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.ability.AbilityDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.ability.widgets.base.AbstractAbilityActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.research.AbilityResearchScreen;
import it.hurts.sskirillss.relics.client.screen.particle.PixelUIParticle;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.description.ability.C2SPacketAbilityUnlock;
import it.hurts.sskirillss.relics.utils.ClientScheduler;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class AbilityCardWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private final AbilityDescriptionScreen screen;
    @Getter
    private final String ability;

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

    public AbilityCardWidget(int x, int y, AbilityDescriptionScreen screen, String ability) {
        super(x, y, 38, 51);

        this.screen = screen;
        this.ability = ability;
    }

    @Override
    public void onPress() {
        var player = minecraft.player;
        var stack = screen.getStack();
        var random = player.getRandom();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var isEnoughLevel = relic.isEnoughLevel(player, stack, ability);
        var isLockUnlocked = relic.isLockUnlocked(player, stack, ability);
        var isAbilityResearched = relic.isAbilityResearched(player, stack, ability);

        SoundManager soundManager = minecraft.getSoundManager();

        if (isEnoughLevel) {
            if (isLockUnlocked) {
                if (isAbilityResearched) {
                    if (!screen.getSelectedAbility().equals(ability)) {
                        screen.setSelectedAbility(ability);

                        screen.rebuildWidgets();

                        for (var entry : screen.renderables) {
                            if (!(entry instanceof AbilityCardWidget card) || !card.ability.equals(ability))
                                continue;
                        }
                    }
                } else
                    minecraft.setScreen(new AbilityResearchScreen(minecraft.player, screen.container, screen.slot, screen, ability));
            } else {
                var unlocks = relic.getLockUnlocks(player, stack, ability) + 1;

                NetworkHandler.sendToServer(new C2SPacketAbilityUnlock(screen.container, screen.slot, ability, unlocks));

                var overshootFactor = 0.035F * unlocks;
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
                var rotationBase = 0.025F * unlocks;
                var amplitude = random.nextBoolean() ? rotationBase : -rotationBase;
                var decay = 0.75F;
                var segmentDuration = 0.2D;
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

                for (int i = 0; i < unlocks * 75; i++) {
                    var center = new Vec2(width / 2F, height / 2F);
                    var margin = new Vec2(center.x + MathUtils.randomFloat(random) * 7F, center.y + MathUtils.randomFloat(random) * 8.5F);

                    var particle = new PixelUIParticle(5F, random.nextInt(20, 40), getX() + margin.x, getY() + margin.y, UIParticle.Layer.SCREEN, 110);

                    var size = (random.nextFloat() * 0.5F) + 0.75F;

                    particle.setColors(new OctoColor(1F, 0.5F + random.nextFloat() * 0.5F, random.nextFloat() * 0.25F, 1F), new OctoColor(1F, 0F, 0F, 1F));
                    particle.setDirection(MathUtils.randomFloat(random), -random.nextFloat());
                    particle.setRollVelocity(MathUtils.randomFloat(random) * 15);
                    particle.getTransform().setSize(new Vector2f(size, size));
                    particle.setGravity(0.5F + random.nextFloat() * 0.5F);
                    particle.setSpeed(1.5F + random.nextFloat() * 1.5F);
                    particle.setGravityDirection(0, 1);
                    particle.setScreen(this.screen);

                    particle.instantiate();
                }

                soundManager.play(SimpleSoundInstance.forUI(SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, 1F));

                if (unlocks >= relic.getMaxLockUnlocks()) {
                    ClientScheduler.schedule(1, this::rebuildActionButtons);

                    for (int i = 0; i < 25; i++) {
                        var center = new Vec2(width / 2F, height / 2F);
                        var margin = new Vec2(center.x + MathUtils.randomFloat(random) * 7F, center.y + MathUtils.randomFloat(random) * 8.5F);

                        var particle = new ExtendedUIParticle(new UIParticle.Texture2D(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/relic/particle/chain.png"), 0, 0, 6, 6, 6, 6),
                                5F, random.nextInt(40, 60), getX() + margin.x, getY() + margin.y, UIParticle.Layer.SCREEN, 110);

                        var size = (random.nextFloat() * 0.5F) + 0.75F;

                        particle.setDirection(MathUtils.randomFloat(random) * 0.25F, -random.nextFloat());
                        particle.setRollVelocity(MathUtils.randomFloat(random) * 15);
                        particle.getTransform().setSize(new Vector2f(size, size));
                        particle.setGravityDirection(0, 1);
                        particle.enableBlend(false);
                        particle.setScreen(this.screen);
                        particle.setGravity(1F);
                        particle.setSpeed(3.5F);

                        particle.instantiate();
                    }

                    soundManager.play(SimpleSoundInstance.forUI(SoundEvents.WITHER_BREAK_BLOCK, 1F));
                    soundManager.play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE, 1F));
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
        this.screen.children().removeIf(entry -> entry instanceof AbstractAbilityActionWidget);
        this.screen.initActionButtons();

        this.screen.children().removeIf(entry -> entry instanceof AbilityModeWidget);
        this.screen.initModeButtons();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = this.minecraft.player;

        if (player == null || !(this.screen.stack.getItem() instanceof IRelicItem relic))
            return;

        var stack = this.screen.getStack();

        var manager = this.minecraft.getTextureManager();
        var poseStack = guiGraphics.pose();

        var unlocks = relic.getLockUnlocks(player, stack, this.ability);

        var isEnoughLevel = relic.isEnoughLevel(player, stack, this.ability);
        var isLockUnlocked = isEnoughLevel && relic.isLockUnlocked(player, stack, this.ability);
        var isAbilityResearched = relic.isAbilityResearched(player, stack, this.ability);

        var canUse = isEnoughLevel && isLockUnlocked && isAbilityResearched;

        var canUpgrade = relic.mayPlayerUpgrade(player, stack, this.ability);
        var canResearch = relic.mayResearch(player, stack, this.ability);

        var canBeUpgraded = relic.canBeUpgraded(player, stack, this.ability);

        var hasAction = canUpgrade || canResearch;

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.enableBlend();

        poseStack.pushPose();

        poseStack.translate((this.getX() + (this.width / 2F)), (this.getY() + (this.height / 2F)), 0);

        poseStack.scale(this.getHoverXSqueeze(), this.getHoverYSqueeze(), 1F);
        poseStack.scale(this.getClickXSqueeze(), this.getClickYSqueeze(), 1F);

        poseStack.mulPose(Axis.ZP.rotation(this.getClickZRotation()));

        var color = (float) ((canUpgrade ? 0.75F : 1.05F) + (Math.sin((player.tickCount + (ability.length() * 10)) * 0.2F) * 0.1F));

        if (isLockUnlocked)
            GUIRenderer.begin(DescriptionTextures.getAbilityCardTexture(stack, ability), poseStack)
                    .color(color, color, color, 1F)
                    .texSize(22, 31)
                    .pos(0, -2)
                    .end();

        if (!canUse) {
            GUIRenderer.begin(isLockUnlocked ? DescriptionTextures.SMALL_CARD_RESEARCH_BACKGROUND : DescriptionTextures.SMALL_CARD_LOCK_BACKGROUND, poseStack)
                    .pos(0, -2)
                    .end();
        }

        GUIRenderer.begin(canBeUpgraded ? canUse ? DescriptionTextures.SMALL_CARD_FRAME_UNLOCKED_ACTIVE : DescriptionTextures.SMALL_CARD_FRAME_UNLOCKED_INACTIVE : canUse ? DescriptionTextures.SMALL_CARD_FRAME_LOCKED_ACTIVE : DescriptionTextures.SMALL_CARD_FRAME_LOCKED_INACTIVE, poseStack).end();

        var level = relic.getAbilityLevel(player, stack, ability);
        var maxLevel = relic.getAbilityTemplate(player, stack, ability).getInitialMaxLevel();

        drawProgressBar(guiGraphics, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/relic/small_card_filler.png"), (-this.width / 2F) + 2, (-this.height / 2F) + 3F, (float) level / maxLevel);

        if (isHovered())
            GUIRenderer.begin(DescriptionTextures.SMALL_CARD_FRAME_SELECTION, poseStack)
                    .end();

        if (isLockUnlocked) {
            if (!isAbilityResearched) {
                var time = minecraft.player.tickCount + (ability.length() * 10F) + partialTick;

                GUIRenderer.begin(DescriptionTextures.RESEARCH, poseStack)
                        .pos((float) Math.sin(time * 0.25F), (float) Math.cos(time * 0.25F) + 0.5F - 2)
                        .patternSize(16, 16)
                        .animation(AnimationData.construct(160, 16, 2))
                        .end();
            }
        } else {
            GUIRenderer.begin(isEnoughLevel ? ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/ability/chains_active_" + unlocks + ".png") : DescriptionTextures.ABILITY_CHAINS_INACTIVE, poseStack)
                    .pos(0, -1)
                    .end();


            poseStack.pushPose();

            GUIRenderer.begin(isEnoughLevel ? ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/relic/icons/lock_active_" + unlocks + ".png") : DescriptionTextures.LOCK_INACTIVE, poseStack)
                    .pos(0, -2)
                    .end();

            poseStack.scale(0.5F, 0.5F, 0.5F);

            var requiredLevelComponent = Component.literal(String.valueOf(relic.getAbilityTemplate(player, stack, ability).getRequiredLevel())).withStyle(ChatFormatting.BOLD);

            guiGraphics.drawString(minecraft.font, requiredLevelComponent, (-(width / 2) + 19) * 2 - minecraft.font.width(requiredLevelComponent) / 2, (-(height / 2) + 24) * 2, isEnoughLevel ? 0xFFE278 : 0xB7AED9, true);

            poseStack.popPose();
        }

        {
            if (canUse) {
                if (canUpgrade) {
                    GUIRenderer.begin(DescriptionTextures.UPGRADE, poseStack)
                            .pos(0, -1)
                            .patternSize(20, 20)
                            .scale(0.9F + ((float) (Math.sin((player.tickCount + partialTick) * 0.25F) * 0.05F)))
                            .animation(AnimationData.construct(200, 20, 2))
                            .end();
                }
            }
        }

        {
            if (canBeUpgraded && canUse) {
                int xOff = 0;

                int quality = relic.calculateAbilityQuality(player, stack, ability);
                boolean isAliquot = quality % 2 == 1;

                for (int i = 0; i < Math.floor(quality / 2D); i++) {
                    GUIRenderer.begin(DescriptionTextures.SMALL_STAR_ACTIVE, poseStack)
                            .pos(-(width / 2F) + xOff + 9, -(height / 2F) + 43)
                            .end();

                    xOff += 5;
                }

                if (isAliquot)
                    GUIRenderer.begin(DescriptionTextures.SMALL_STAR_ACTIVE, poseStack)
                            .pos(-(width / 2F) + xOff + 8, -(height / 2F) + 43)
                            .patternSize(1, 2)
                            .end();
            }
        }

        {
            if (canBeUpgraded) {
                MutableComponent title = Component.literal(canUse ? String.valueOf(relic.getAbilityLevel(player, stack, ability)) : "?").withStyle(ChatFormatting.BOLD);

                float textScale = 0.5F;

                poseStack.scale(textScale, textScale, textScale);

                guiGraphics.drawString(minecraft.font, title, -((width + 1) / 2) - (minecraft.font.width(title) / 2) + 19, (-(height / 2) - 20), canUse ? 0xFFE278 : 0xB7AED9, true);
            }
        }

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    public static void drawProgressBar(GuiGraphics gui, ResourceLocation tex, float x, float y, float progress) {
        var textureWidth = 34;
        var textureHeight = 46;
        var cornerSize = 3;
        var borderThickness = 3;
        var topStartU = 7;
        var topEndU = 27;
        var verticalEdgeLength = textureHeight - 2 * cornerSize;
        var horizontalEdgeLength = textureWidth - 2 * cornerSize;
        var segTop = topStartU - cornerSize + 1;
        var segCornerTopLeft = cornerSize;
        var segLeftEdge = verticalEdgeLength;
        var segCornerBotLeft = cornerSize;
        var segBottom = horizontalEdgeLength;
        var segCornerBotRight = cornerSize;
        var segRightEdge = verticalEdgeLength;
        var segCornerTopRight = cornerSize;
        var segTopRightPart = textureWidth - cornerSize - topEndU;
        var totalLength = segTop + segCornerTopLeft + segLeftEdge + segCornerBotLeft + segBottom + segCornerBotRight + segRightEdge + segCornerTopRight + segTopRightPart;

        var remaining = (int) (Mth.clamp(progress, 0f, 1f) * totalLength);

        if (remaining <= 0)
            return;

        var renderer = GUIRenderer
                .begin(tex, gui.pose())
                .texSize(textureWidth, textureHeight)
                .anchor(SpriteAnchor.TOP_LEFT);

        var drawLen = 0;
        var moveLen = 0;

        drawLen = Math.min(remaining, segTop);

        var u0 = topStartU - drawLen + 1;

        renderer.pos(x + u0, y)
                .patternSize(drawLen, borderThickness)
                .texOff(u0, 0)
                .end();

        remaining -= drawLen;

        if (remaining > 0) {
            drawLen = Math.min(remaining, segCornerTopLeft);

            var offsetU1 = cornerSize - drawLen;

            renderer.pos(x + offsetU1, y)
                    .patternSize(drawLen, borderThickness)
                    .texOff(offsetU1, 0)
                    .end();

            remaining -= drawLen;
        }

        if (remaining > 0) {
            moveLen = Math.min(remaining, segLeftEdge);

            renderer.pos(x, y + cornerSize)
                    .patternSize(borderThickness, moveLen)
                    .texOff(0, cornerSize)
                    .end();

            remaining -= moveLen;
        }

        if (remaining > 0) {
            drawLen = Math.min(remaining, segCornerBotLeft);

            renderer.pos(x, y + textureHeight - borderThickness)
                    .patternSize(drawLen, borderThickness)
                    .texOff(0, textureHeight - borderThickness)
                    .end();

            remaining -= drawLen;
        }

        if (remaining > 0) {
            drawLen = Math.min(remaining, segBottom);

            renderer.pos(x + cornerSize, y + textureHeight - borderThickness)
                    .patternSize(drawLen, borderThickness)
                    .texOff(cornerSize, textureHeight - borderThickness)
                    .end();

            remaining -= drawLen;
        }

        if (remaining > 0) {
            drawLen = Math.min(remaining, segCornerBotRight);

            var offsetUBot = textureWidth - cornerSize;

            renderer.pos(x + offsetUBot, y + textureHeight - borderThickness)
                    .patternSize(drawLen, borderThickness)
                    .texOff(offsetUBot, textureHeight - borderThickness)
                    .end();

            remaining -= drawLen;
        }

        if (remaining > 0) {
            moveLen = Math.min(remaining, segRightEdge);

            var v6 = cornerSize + (verticalEdgeLength - moveLen);

            renderer.pos(x + textureWidth - borderThickness, y + v6)
                    .patternSize(borderThickness, moveLen)
                    .texOff(textureWidth - borderThickness, v6)
                    .end();

            remaining -= moveLen;
        }

        if (remaining > 0) {
            drawLen = Math.min(remaining, segCornerTopRight);

            var u7 = textureWidth - borderThickness;

            renderer.pos(x + u7, y)
                    .patternSize(borderThickness, drawLen)
                    .texOff(u7, 0)
                    .end();

            remaining -= drawLen;
        }

        if (remaining > 0) {
            drawLen = Math.min(remaining, segTopRightPart);

            var startU8 = textureWidth - cornerSize - 1;
            var offsetU8 = startU8 - (drawLen - 1);

            renderer.pos(x + offsetU8, y)
                    .patternSize(drawLen, borderThickness)
                    .texOff(offsetU8, 0)
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
        return screen.getSelectedAbility().equals(ability);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var player = minecraft.player;
        var stack = screen.stack;

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        AbilityTemplate data = relic.getAbilityTemplate(player, stack, ability);

        if (data == null)
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        var title = Component.translatableWithFallback("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".ability." + ability, ability);

        int maxWidth = 110;
        int renderWidth = Math.min((minecraft.font.width(title.withStyle(ChatFormatting.BOLD)) / 2) + 4, maxWidth);

        List<MutableComponent> entries = new ArrayList<>();

        entries.add(Component.literal(" "));

        int level = relic.getRelicLevel(player, stack);
        int requiredLevel = data.getRequiredLevel();

        if (level < requiredLevel) {
            entries.add(Component.literal(" "));

            entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.low_level", Component.literal(String.valueOf(requiredLevel)).withStyle(ChatFormatting.BOLD))));
        } else {
            if (!relic.isLockUnlocked(player, stack, ability)) {
                entries.add(Component.literal(" "));

                entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.ready_to_unlock", Component.literal(String.valueOf(relic.getMaxLockUnlocks() - relic.getLockUnlocks(player, stack, ability))).withStyle(ChatFormatting.BOLD))));
            } else {
                if (!relic.isAbilityResearched(player, stack, ability)) {
                    entries.add(Component.literal(" "));

                    entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.unresearched")));
                } else if (relic.mayPlayerUpgrade(player, stack, ability)) {
                    entries.add(Component.literal(" "));

                    entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.ready_to_upgrade")));
                }
            }
        }

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry)) / 2;

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 4, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        int height = tooltip.size() * 5;

        int y = getHeight() / 2;

        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(false);

        poseStack.translate((getX() + (getWidth() / 2F)), (getY() + (getHeight() / 2F)), 0);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, height, -((renderWidth + 19) / 2), y);

        int yOff = 0;

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        if (!relic.isAbilityUnlocked(player, stack, ability)) {
            title = ScreenUtils.stylizeWithReplacement(title, 1F, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(0x9E00B0), ability.length());

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
        if (!isLocked() && screen.getStack().getItem() instanceof IRelicItem relic && relic.isAbilityUnlocked(minecraft.player, screen.stack, ability))
            super.playDownSound(handler);
    }
}