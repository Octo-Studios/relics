package it.hurts.sskirillss.relics.client.screen.utils.misc;

import it.hurts.octostudios.octolib.client.particle.UIParticle;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.client.screen.particle.PixelUIParticle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

public class UIParticles {
    public static final int GOLD = 0xFFD65A;

    private static final float MAX_SPEED = 5F;
    private static final float Z_OFFSET = 200F;
    private static final float ROLL_VELOCITY = 30F;
    private static final float MIN_BRIGHTNESS = 200F;

    public static void spawn(Screen screen, RandomSource random, float x, float y, float directionX, float directionY, float speed, int lifetime, float size, float gravity, float friction, int blend, OctoColor... colors) {
        var particle = new PixelUIParticle(MAX_SPEED, lifetime, x, y, UIParticle.Layer.SCREEN, Z_OFFSET);

        particle.setColors(colors);
        particle.setBlendFunc(GL11.GL_SRC_ALPHA, blend);
        particle.enableBlend(true);
        particle.setDirection(directionX, directionY);
        particle.setRollVelocity((random.nextFloat() - 0.5F) * ROLL_VELOCITY);
        particle.getTransform().setSize(new Vector2f(size, size));
        particle.setGravityDirection(0F, 1F);
        particle.setGravity(gravity);
        particle.setFriction(friction);
        particle.setSpeed(speed);
        particle.setScreen(screen);

        particle.instantiate();
    }

    public static OctoColor opaque(int rgb) {
        return new OctoColor(0xFF000000 | (rgb & 0xFFFFFF));
    }

    public static OctoColor bright(int rgb) {
        var red = (rgb >> 16) & 0xFF;
        var green = (rgb >> 8) & 0xFF;
        var blue = rgb & 0xFF;

        var factor = Math.max(1F, MIN_BRIGHTNESS / Math.max(1, Math.max(red, Math.max(green, blue))));

        return new OctoColor(Math.min(red * factor / 255F, 1F), Math.min(green * factor / 255F, 1F), Math.min(blue * factor / 255F, 1F), 1F);
    }

    public static OctoColor shade(OctoColor color, float amount) {
        return new OctoColor(Mth.clamp(color.r() * amount, 0F, 1F), Mth.clamp(color.g() * amount, 0F, 1F), Mth.clamp(color.b() * amount, 0F, 1F), 1F);
    }

    public static OctoColor transparent(OctoColor color) {
        return new OctoColor(color.r(), color.g(), color.b(), 0F);
    }
}
