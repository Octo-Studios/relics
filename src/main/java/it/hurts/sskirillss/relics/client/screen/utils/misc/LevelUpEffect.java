package it.hurts.sskirillss.relics.client.screen.utils.misc;

import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.lwjgl.opengl.GL11;

public class LevelUpEffect {
    private record Wave(float share, float minSpeed, float maxSpeed, float sizeScale, int minLifetime, int maxLifetime) {
    }

    private static final Wave MAIN_WAVE = new Wave(0.6F, 0.5F, 2.7F, 1F, 24, 48);
    private static final Wave SECOND_WAVE = new Wave(0.4F, 1.2F, 3.4F, 0.8F, 18, 32);

    private static final int FLASH_DURATION = 14;
    private static final int SECOND_WAVE_DELAY = 3;

    private static final float WAVE_GRAVITY = 0.03F;
    private static final float WAVE_FRICTION = 0.09F;
    private static final float WAVE_REACH_MIN = 0.55F;
    private static final float WAVE_REACH_RANGE = 0.5F;
    private static final float WAVE_SPREAD = 0.7F;

    private static final float BURST_GRAVITY = 0.03F;
    private static final float BURST_FRICTION = 0.09F;
    private static final float BURST_SIZE = 0.8F;
    private static final float BURST_SIZE_RANGE = 0.9F;
    private static final float BURST_SPEED = 0.4F;
    private static final float BURST_SPEED_RANGE = 1.3F;
    private static final int BURST_LIFETIME = 12;
    private static final int BURST_LIFETIME_RANGE = 14;

    private static final float CRUMBLE_GRAVITY = 0.12F;
    private static final float CRUMBLE_FRICTION = 0.05F;
    private static final float CRUMBLE_WIDTH = 5F;
    private static final float CRUMBLE_SIZE = 0.8F;
    private static final float CRUMBLE_SIZE_RANGE = 0.8F;
    private static final float CRUMBLE_DRIFT_X = 0.8F;
    private static final float CRUMBLE_DRIFT_Y = 0.6F;
    private static final float CRUMBLE_SPEED = 0.3F;
    private static final float CRUMBLE_SPEED_RANGE = 0.8F;
    private static final float CRUMBLE_SHADE = 0.7F;
    private static final float CRUMBLE_SHADE_RANGE = 0.4F;
    private static final int CRUMBLE_LIFETIME = 14;
    private static final int CRUMBLE_LIFETIME_RANGE = 12;

    private static final float SHADE_MIN = 0.85F;
    private static final float SHADE_RANGE = 0.3F;

    private int ticks = FLASH_DURATION + 1;
    private int count;

    private Screen screen;
    private RandomSource random;
    private float x;
    private float y;
    private float width;
    private float height;
    private OctoColor color;

    public void start(Screen screen, RandomSource random, float x, float y, float width, float height, int tint, int count) {
        this.screen = screen;
        this.random = random;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = UIParticles.bright(tint);
        this.count = count;
        this.ticks = 0;

        emitWave(MAIN_WAVE);
    }

    public void tick() {
        if (ticks > FLASH_DURATION)
            return;

        ticks++;

        if (ticks == SECOND_WAVE_DELAY)
            emitWave(SECOND_WAVE);
    }

    public float flash(float partialTick) {
        if (ticks > FLASH_DURATION)
            return 0F;

        return Mth.clamp(1F - (ticks + partialTick) / FLASH_DURATION, 0F, 1F);
    }

    public static void burstAt(Screen screen, RandomSource random, float x, float y, int tint, int count) {
        var base = UIParticles.bright(tint);

        for (var i = 0; i < count; i++) {
            var angle = random.nextFloat() * Mth.TWO_PI;

            var visible = UIParticles.shade(base, SHADE_MIN + random.nextFloat() * SHADE_RANGE);
            var size = BURST_SIZE + random.nextFloat() * BURST_SIZE_RANGE;
            var speed = BURST_SPEED + random.nextFloat() * BURST_SPEED_RANGE;
            var lifetime = BURST_LIFETIME + random.nextInt(BURST_LIFETIME_RANGE);

            UIParticles.spawn(screen, random, x, y, Mth.cos(angle), Mth.sin(angle), speed, lifetime, size, BURST_GRAVITY, BURST_FRICTION, GL11.GL_ONE, visible, visible, UIParticles.transparent(visible));
        }
    }

    public static void crumbleAt(Screen screen, RandomSource random, float x, float y, int tint, int count) {
        var base = UIParticles.opaque(tint);

        for (var i = 0; i < count; i++) {
            var visible = UIParticles.shade(base, CRUMBLE_SHADE + random.nextFloat() * CRUMBLE_SHADE_RANGE);
            var size = CRUMBLE_SIZE + random.nextFloat() * CRUMBLE_SIZE_RANGE;
            var speed = CRUMBLE_SPEED + random.nextFloat() * CRUMBLE_SPEED_RANGE;
            var lifetime = CRUMBLE_LIFETIME + random.nextInt(CRUMBLE_LIFETIME_RANGE);

            var directionX = (random.nextFloat() - 0.5F) * CRUMBLE_DRIFT_X;
            var directionY = random.nextFloat() * CRUMBLE_DRIFT_Y;

            UIParticles.spawn(screen, random, x + (random.nextFloat() - 0.5F) * CRUMBLE_WIDTH, y, directionX, directionY, speed, lifetime, size, CRUMBLE_GRAVITY, CRUMBLE_FRICTION, GL11.GL_ONE_MINUS_SRC_ALPHA, visible, visible, UIParticles.transparent(visible));
        }
    }

    private void emitWave(Wave wave) {
        var centerX = x + width / 2F;
        var centerY = y + height / 2F;

        for (var i = 0; i < Math.round(count * wave.share()); i++) {
            var angle = random.nextFloat() * Mth.TWO_PI;

            var cos = Mth.cos(angle);
            var sin = Mth.sin(angle);

            var reach = WAVE_REACH_MIN + random.nextFloat() * WAVE_REACH_RANGE;

            var visible = UIParticles.shade(color, SHADE_MIN + random.nextFloat() * SHADE_RANGE);
            var hidden = UIParticles.transparent(visible);

            var size = (1F + random.nextFloat()) * wave.sizeScale();
            var speed = Mth.lerp(random.nextFloat(), wave.minSpeed(), wave.maxSpeed());
            var lifetime = wave.minLifetime() + random.nextInt(wave.maxLifetime() - wave.minLifetime() + 1);

            var directionX = cos + (random.nextFloat() - 0.5F) * WAVE_SPREAD;
            var directionY = sin + (random.nextFloat() - 0.5F) * WAVE_SPREAD;

            UIParticles.spawn(screen, random, centerX + cos * width / 2F * reach, centerY + sin * height / 2F * reach, directionX, directionY, speed, lifetime, size, WAVE_GRAVITY, WAVE_FRICTION, GL11.GL_ONE, hidden, visible, visible, visible, hidden);
        }
    }
}
