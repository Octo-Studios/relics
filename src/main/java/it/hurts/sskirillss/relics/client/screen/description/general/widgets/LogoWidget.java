package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.octostudios.octolib.client.particle.UIParticle;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.particle.PixelUIParticle;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import org.joml.Vector2f;

import java.awt.*;

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

        poseStack.translate(this.getX() + (this.width / 2F) + Math.sin((player.tickCount + pPartialTick) * 0.075F), this.getY() + (this.height / 2F) + Math.cos((player.tickCount + pPartialTick) * 0.075F) * 0.5F, 0);

        poseStack.scale(this.getXSqueeze(), this.getYSqueeze(), 1F);

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

        var tween = Tween.create().setParallel(true);

        tween.tweenMethod(this::setYSqueeze, this.getYSqueeze(), 1.2F, 0.2D).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.QUAD);
        tween.tweenMethod(this::setXSqueeze, this.getXSqueeze(), 1.2F, 0.2D).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.QUAD);
        tween.tweenMethod(this::setYSqueeze, 1.2F, 1F, 0.4D).setDelay(0.2D).setEaseType(EaseType.EASE_OUT);
        tween.tweenMethod(this::setXSqueeze, 1.2F, 1F, 0.4D).setDelay(0.2D).setEaseType(EaseType.EASE_IN);

        tween.start();

        LogoWidget.addClicks(1);

//        if (LogoWidget.getRemainingClicks() == 0)
//            screen.rebuildWidgets();
    }

    @Override
    public void onTick() {
        var player = minecraft.player;

        if (player == null)
            return;

        var random = player.getRandom();

        if (minecraft.player.tickCount % 2 == 0) {
            var particle = new PixelUIParticle(0.4F, random.nextInt(30, 50), this.getX() + 5 + random.nextInt(width), this.getY() + random.nextInt(3), UIParticle.Layer.SCREEN, 10);

            float size = (random.nextFloat() * 0.5F) + 0.75F;

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
    private static final int MAX_CLICKS = 10;

    public static int getCurrentClicks() {
        return CLICKS_AMOUNT;
    }

    public static void setCurrentClicks(int clicks) {
        CLICKS_AMOUNT = Math.clamp(clicks, 0, MAX_CLICKS);
    }

    public static void addClicks(int clicks) {
        setCurrentClicks(getCurrentClicks() + clicks);
    }

    public static int getRemainingClicks() {
        return MAX_CLICKS - getCurrentClicks();
    }
}