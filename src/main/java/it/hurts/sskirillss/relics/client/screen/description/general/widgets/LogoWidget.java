package it.hurts.sskirillss.relics.client.screen.description.general.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import it.hurts.sskirillss.relics.client.screen.base.IRelicScreenProvider;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.base.DescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import org.joml.Vector2d;

import java.awt.*;

public class LogoWidget extends AbstractDescriptionWidget implements ITickingWidget {
    @Getter
    private IRelicScreenProvider provider;

    public Vector2d squeeze = new Vector2d(1, 1);

    public LogoWidget(int x, int y, IRelicScreenProvider provider) {
        super(x, y, 55, 18);

        this.provider = provider;
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

        poseStack.scale((float) this.squeeze.x(), (float) this.squeeze.y(), 1F);

        GUIRenderer.begin(DescriptionTextures.LOGO, poseStack)
                .anchor(SpriteAnchor.CENTER)
                .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.popPose();
    }

    @Override
    public void onPress() {
        super.onPress();

        var tween = Tween.create().setParallel(true);

        tween.tweenProperty(this, "squeeze.y", 1.2, 0.05).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.QUAD);
        tween.tweenProperty(this, "squeeze.x", 1.2, 0.1).setEaseType(EaseType.EASE_OUT).setTransitionType(TransitionType.QUAD);
        tween.tweenProperty(this, "squeeze.y", 1, 0.1).setDelay(0.05).setEaseType(EaseType.EASE_OUT);
        tween.tweenProperty(this, "squeeze.x", 1, 0.1).setDelay(0.1).setEaseType(EaseType.EASE_IN);

        LogoWidget.addClicks(1);

        if (LogoWidget.getRemainingClicks() == 0)
            ((DescriptionScreen) provider).rebuildWidgets();
    }

    @Override
    public void onTick() {
        var player = minecraft.player;

        if (player == null)
            return;

        var random = player.getRandom();

        if (minecraft.player.tickCount % 5 == 0)
            ParticleStorage.addParticle((Screen) provider, new ExperienceParticleData(new Color(200 + random.nextInt(50), 150 + random.nextInt(100), 0),
                    getX() + random.nextInt(width), getY() + random.nextInt(3), 1F + (random.nextFloat() * 0.25F), 50 + random.nextInt(50)));
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