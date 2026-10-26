package it.hurts.sskirillss.relics.client.screen.description.general.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.description.general.particles.base.ParticleData;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class FlyingParticleData extends ParticleData {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/particles/pixel.png");

    private static final int TRAIL_SAMPLES = 28;
    private static final int CLOUD_DOTS = 3;
    private static final float TRAIL_STEP = 0.2F;
    private static final float TRAIL_SPREAD = 11F;
    private static final float TRAIL_SHRINK = 0.55F;
    private static final float CLOUD_ALONG = 1.5F;
    private static final float CLOUD_EDGE_FADE = 0.6F;
    private static final float CLOUD_SIZE = 0.7F;

    private static final float HEAD_ALPHA = 0.9F;
    private static final float TRAIL_ALPHA = 0.3F;
    private static final float CLOUD_ALPHA = 0.26F;
    private static final float APPEAR_SPEED = 0.5F;
    private static final float ARRIVAL_FADE = 2F;
    private static final float BLINK = 0.04F;
    private static final float BLINK_SPEED = 0.9F;
    private static final float Z_LEVEL = 300F;

    private final float targetX;
    private final float targetY;
    private final float controlX;
    private final float controlY;
    private final int delay;
    private final int flight;
    private final int seed;

    public FlyingParticleData(Color color, float startX, float startY, float targetX, float targetY, float controlX, float controlY, int delay, int flight, float scale, int seed) {
        super(TEXTURE, color, startX, startY, scale, delay + flight + 1);

        this.targetX = targetX;
        this.targetY = targetY;
        this.controlX = controlX;
        this.controlY = controlY;
        this.delay = delay;
        this.flight = flight;
        this.seed = seed;
    }

    @Override
    public void tick(Screen screen) {
        super.tick(screen);

        var progress = progress(getMaxLifeTime() - getLifeTime());

        setX(bezier(getXStart(), controlX, targetX, progress));
        setY(bezier(getYStart(), controlY, targetY, progress));
    }

    @Override
    public void render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var age = getMaxLifeTime() - getLifeTime() + partialTick;

        if (age <= delay)
            return;

        var blink = BLINK + (float) Math.sin(age * BLINK_SPEED) * BLINK;
        var visibility = Math.min((age - delay) * APPEAR_SPEED, 1F) * Mth.clamp(1F - (age - delay - flight) / ARRIVAL_FADE, 0F, 1F);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        for (var sample = TRAIL_SAMPLES - 1; sample >= 0; sample--) {
            var sampleAge = age - sample * TRAIL_STEP;

            if (sampleAge < delay)
                continue;

            var fraction = sample / (float) TRAIL_SAMPLES;
            var progress = progress(sampleAge);

            var tangentX = tangent(getXStart(), controlX, targetX, progress);
            var tangentY = tangent(getYStart(), controlY, targetY, progress);
            var length = Math.max((float) Math.hypot(tangentX, tangentY), 0.001F);

            var directionX = tangentX / length;
            var directionY = tangentY / length;

            var x = bezier(getXStart(), controlX, targetX, progress);
            var y = bezier(getYStart(), controlY, targetY, progress);

            var size = getScale() * (1F - fraction * TRAIL_SHRINK);

            drawDot(guiGraphics, x, y, size, visibility * (sample < 2 ? HEAD_ALPHA : TRAIL_ALPHA * (float) Math.pow(1F - fraction, 1.3D)), blink);

            var quantized = Math.round(sampleAge / TRAIL_STEP);
            var width = TRAIL_SPREAD * (0.15F + fraction);

            for (var dot = 0; dot < CLOUD_DOTS; dot++) {
                var hash = quantized * 131 + dot * 17 + seed;

                var lateral = (noise(hash) + noise(hash + 1)) * 0.5F * width;
                var along = noise(hash + 2) * CLOUD_ALONG;

                var edge = 1F - Math.min(1F, Math.abs(lateral) / width) * CLOUD_EDGE_FADE;
                var alpha = visibility * CLOUD_ALPHA * (float) Math.pow(1F - fraction, 1.2D) * edge;

                drawDot(guiGraphics, x - directionY * lateral + directionX * along, y + directionX * lateral + directionY * along, size * (CLOUD_SIZE + Math.abs(noise(hash + 3)) * (1F - CLOUD_SIZE)), alpha, blink);
            }
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void drawDot(GuiGraphics guiGraphics, float x, float y, float size, float alpha, float blink) {
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        poseStack.translate(x, y, Z_LEVEL);

        RenderSystem.setShaderColor(Math.min(getColor().getRed() / 255F + blink, 1F), Math.min(getColor().getGreen() / 255F + blink, 1F), Math.min(getColor().getBlue() / 255F + blink, 1F), alpha);

        GUIRenderer.begin(TEXTURE, poseStack)
                .scale(size)
                .anchor(SpriteAnchor.CENTER)
                .end();

        poseStack.popPose();
    }

    private float progress(float age) {
        var time = Mth.clamp((age - delay) / flight, 0F, 1F);

        return time * time * (1.6F - 0.6F * time);
    }

    private float bezier(float start, float control, float target, float time) {
        var inverse = 1F - time;

        return inverse * inverse * start + 2F * inverse * time * control + time * time * target;
    }

    private float tangent(float start, float control, float target, float time) {
        return 2F * (1F - time) * (control - start) + 2F * time * (target - control);
    }

    private static float noise(int value) {
        return (Mth.murmurHash3Mixer(value) & 0xFFFF) / 32768F - 1F;
    }
}
