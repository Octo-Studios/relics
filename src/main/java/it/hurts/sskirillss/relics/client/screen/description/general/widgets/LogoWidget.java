package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.octostudios.octolib.client.particle.ExtendedUIParticle;
import it.hurts.octostudios.octolib.client.particle.UIParticle;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.particle.PixelUIParticle;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

public class LogoWidget extends AbstractDescriptionWidget implements ITickingWidget {
    @Getter
    private DescriptionScreen screen;

    @Getter
    @Setter
    private float xSqueeze = 1F;
    @Getter
    @Setter
    private float ySqueeze = 1F;

    public LogoWidget(int x, int y, DescriptionScreen screen) {
        super(x, y, 55, 18);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var player = minecraft.player;

        if (player == null)
            return;

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        var color = (float) (1.05F + (Math.sin(player.tickCount * 0.25F) * 0.1F));

        RenderSystem.setShaderColor(color, color, color, 1F);
        RenderSystem.setShaderTexture(0, DescriptionTextures.LOGO);

        poseStack.translate(this.getX() + (this.width / 2F) + Math.sin((player.tickCount + pPartialTick) * 0.075F), this.getY() + (this.height / 2F) + Math.cos((player.tickCount + pPartialTick) * 0.075F) * 0.5F, 100);

        var modifier = 1F + LogoWidget.getCurrentClicks() * 0.15F;

        poseStack.scale(this.getXSqueeze(), this.getYSqueeze(), 1F);
        poseStack.scale(modifier, modifier, 1F);

        GUIRenderer.begin(DescriptionTextures.LOGO, poseStack)
                .anchor(SpriteAnchor.CENTER)
                .patternSize(55, 18)
                .animation(AnimationData.construct(576, 18, 2))
                .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.popPose();
    }

    @Override
    public void onPress() {
        super.onPress();

        var intensity = 0.75F + LogoWidget.getCurrentClicks() * 0.15F;

        var yPeak = 1F + (1.1F - 1F) * intensity;
        var xPeak = 1F + (1.2F - 1F) * intensity;
        var yValley = 1F - (1F - 0.95F) * intensity;
        var xValley = 1F - (1F - 0.9F) * intensity;

        var tween = Tween.create().setParallel(true);

        tween.tweenMethod(this::setYSqueeze, this.getYSqueeze(), yPeak, 0.2D)
                .setEaseType(EaseType.EASE_OUT)
                .setTransitionType(TransitionType.QUAD);
        tween.tweenMethod(this::setXSqueeze, this.getXSqueeze(), xPeak, 0.15D)
                .setEaseType(EaseType.EASE_OUT)
                .setTransitionType(TransitionType.QUAD);

        tween.tweenMethod(this::setYSqueeze, yPeak, yValley, 0.18D)
                .setDelay(0.2D)
                .setEaseType(EaseType.EASE_IN_OUT)
                .setTransitionType(TransitionType.QUAD);
        tween.tweenMethod(this::setXSqueeze, xPeak, xValley, 0.17D)
                .setDelay(0.15D)
                .setEaseType(EaseType.EASE_IN_OUT)
                .setTransitionType(TransitionType.QUAD);

        tween.tweenMethod(this::setYSqueeze, yValley, 1F, 0.25D)
                .setDelay(0.38D)
                .setEaseType(EaseType.EASE_IN)
                .setTransitionType(TransitionType.QUAD);
        tween.tweenMethod(this::setXSqueeze, xValley, 1F, 0.2D)
                .setDelay(0.32D)
                .setEaseType(EaseType.EASE_IN)
                .setTransitionType(TransitionType.QUAD);

        tween.start();

        LogoWidget.setNoClickDuration(0);

        var remainingClicks = LogoWidget.getRemainingClicks();

        if (remainingClicks > 0)
            LogoWidget.addCurrentClicks(1);

        if (remainingClicks <= 1) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(RelicsSounds.LOGO_EXPLOSION.get(), 1F, 1F));

            var random = minecraft.player.getRandom();

            for (int i = 0; i < 100; i++) {
                var particle = new ExtendedUIParticle(new UIParticle.Texture2D(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/particle/logo_air.png"), 0, 0, 10, 10, 10, 10),
                        5F, random.nextInt(50, 100), this.getX() + random.nextInt(this.getWidth()), this.getY() + random.nextInt(this.getHeight()), UIParticle.Layer.SCREEN, 10);

                float size = (random.nextFloat() * 0.5F) + 0.75F;

                particle.setColors(new OctoColor(1F, 1F, 1F, 1F), new OctoColor(1F, 1F, 1F, 0F));
                particle.setDirection(MathUtils.randomFloat(random), -random.nextFloat() * 0.5F);
                particle.setRollVelocity(MathUtils.randomFloat(random) * 15);
                particle.getTransform().setSize(new Vector2f(size, size));
                particle.setGravityDirection(0, -1);
                particle.setScreen(this.screen);
                particle.setGravity(0.01F);
                particle.setFriction(0.075F);
                particle.setSpeed(random.nextFloat() * 5F);

                particle.instantiate();
            }

            for (int i = 0; i < 20; i++) {
                var particle = new ExtendedUIParticle(new UIParticle.Texture2D(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/particle/logo_part_" + (random.nextInt(4) + 1) + ".png"), 0, 0, 10, 10, 10, 10),
                        5F, random.nextInt(50, 100), this.getX() + random.nextInt(this.getWidth()), this.getY() + random.nextInt(this.getHeight()), UIParticle.Layer.SCREEN, 10);

                float size = (random.nextFloat() * 0.5F) + 0.75F;

                particle.setColors(new OctoColor(1F, 1F, 1F, 1F), new OctoColor(1F, 1F, 1F, 0F));
                particle.setDirection(MathUtils.randomFloat(random), MathUtils.randomFloat(random) * 0.5F);
                particle.setRollVelocity(MathUtils.randomFloat(random) * 15);
                particle.getTransform().setSize(new Vector2f(size, size));
                particle.setGravityDirection(0, 1);
                particle.setScreen(this.screen);
                particle.setGravity(0.25F);
                particle.enableBlend(false);
                particle.setSpeed(random.nextFloat() * 3.5F);

                particle.instantiate();
            }

            screen.rebuildWidgets();
        } else
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(RelicsSounds.LOGO_INFLATE.get(), 1F + LogoWidget.getCurrentClicks() * (1F / LogoWidget.MAX_CLICKS)));
    }

    @Override
    public void onTick() {
        var player = minecraft.player;

        if (player == null)
            return;

        var noClickDuration = LogoWidget.getNoClickDuration();
        var currentClicks = LogoWidget.getCurrentClicks();

        if (noClickDuration < MAX_NO_CLICK_DURATION)
            LogoWidget.addNoClickDuration(1);
        else if (currentClicks > 0 && currentClicks < MAX_CLICKS && player.tickCount % 10 == 0) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(RelicsSounds.LOGO_INFLATE.get(), 1F));

            var intensity = 0.75F + LogoWidget.MAX_CLICKS * 0.15F;

            var yPeak = 1F + (1.2F - 1F) * intensity;
            var xPeak = 1F + (1.4F - 1F) * intensity;
            var yValley = 1F - (1F - 0.9F) * intensity;
            var xValley = 1F - (1F - 0.85F) * intensity;

            var tween = Tween.create().setParallel(true);

            tween.tweenMethod(this::setYSqueeze, yPeak, yValley, 0.2D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.QUAD);
            tween.tweenMethod(this::setXSqueeze, xPeak, xValley, 0.15D)
                    .setEaseType(EaseType.EASE_OUT)
                    .setTransitionType(TransitionType.QUAD);

            tween.tweenMethod(this::setYSqueeze, yValley, 1F, 0.25D)
                    .setDelay(0.2D)
                    .setEaseType(EaseType.EASE_IN)
                    .setTransitionType(TransitionType.QUAD);
            tween.tweenMethod(this::setXSqueeze, xValley, 1F, 0.2D)
                    .setDelay(0.15D)
                    .setEaseType(EaseType.EASE_IN)
                    .setTransitionType(TransitionType.QUAD);

            tween.start();

            LogoWidget.setCurrentClicks(0);
        }

        var random = player.getRandom();

        if (minecraft.player.tickCount % 2 == 0) {
            var widthModifier = 1F + LogoWidget.getCurrentClicks() * 0.15F;
            var heightModifier = LogoWidget.getCurrentClicks() * 1.5F;
            var semiWidth = (int) (width * widthModifier / 2F);

            var particle = new PixelUIParticle(0.4F, random.nextInt(30, 50), this.getX() + width / 2F + random.nextInt(-semiWidth, semiWidth), this.getY() + random.nextInt(3) - heightModifier, UIParticle.Layer.SCREEN, 10);

            float size = (random.nextFloat() * 0.5F) + 0.75F + LogoWidget.getCurrentClicks() * 0.1F;

            particle.setColors(new OctoColor(1F, 1F, random.nextFloat() * 0.25F, 1F), new OctoColor(1F, 0F, 0F, 0F));
            particle.setDirection(MathUtils.randomFloat(random) * 0.5F, random.nextFloat() * -0.5F);
            particle.setRollVelocity(MathUtils.randomFloat(random) * 15);
            particle.getTransform().setSize(new Vector2f(size, size));
            particle.setGravityDirection(0, -1);
            particle.setAngularVelocity(10F);
            particle.setScreen(this.screen);
            particle.setFriction(0.025F);
            particle.setGravity(0.035F);

            particle.instantiate();
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }

    private static int CLICKS_AMOUNT = 0;
    private static final int MAX_CLICKS = 6;

    public static int getCurrentClicks() {
        return CLICKS_AMOUNT;
    }

    public static void setCurrentClicks(int clicks) {
        CLICKS_AMOUNT = Math.clamp(clicks, 0, MAX_CLICKS);
    }

    public static void addCurrentClicks(int clicks) {
        LogoWidget.setCurrentClicks(LogoWidget.getCurrentClicks() + clicks);
    }

    public static int getRemainingClicks() {
        return MAX_CLICKS - LogoWidget.getCurrentClicks();
    }

    private static int NO_CLICK_DURATION = 0;
    private static final int MAX_NO_CLICK_DURATION = 100;

    public static int getNoClickDuration() {
        return NO_CLICK_DURATION;
    }

    public static void setNoClickDuration(int duration) {
        NO_CLICK_DURATION = Math.clamp(duration, 0, MAX_NO_CLICK_DURATION);
    }

    public static void addNoClickDuration(int duration) {
        LogoWidget.setNoClickDuration(LogoWidget.getNoClickDuration() + duration);
    }
}