package it.hurts.sskirillss.relics.client.screen.utils.misc;

import it.hurts.octostudios.octolib.util.OctoColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.lwjgl.opengl.GL11;

public class ProgressBarEffect {
    private record Speed(float min, float max) {
    }

    public record Palette(OctoColor brightFrom, OctoColor brightTo, OctoColor visibleFrom, OctoColor visibleTo) {
        public static final Palette PURPLE = new Palette(new OctoColor(0.85F, 0.55F, 1F, 1F), new OctoColor(1F, 0.9F, 1F, 1F), new OctoColor(0.65F, 0.25F, 1F, 1F), new OctoColor(0.85F, 0.55F, 1F, 1F));
        public static final Palette GREEN = new Palette(new OctoColor(0.75F, 1F, 0.45F, 1F), new OctoColor(0.95F, 1F, 0.8F, 1F), new OctoColor(0.4F, 0.85F, 0.15F, 1F), new OctoColor(0.65F, 1F, 0.35F, 1F));
    }

    private static final Speed HEAD_SPEED = new Speed(0.4F, 1.4F);
    private static final Speed TRAIL_SPEED = new Speed(0.2F, 0.9F);
    private static final Speed FINISH_SPEED = new Speed(0.6F, 2F);

    private static final int DEFAULT_DURATION = 16;
    private static final int GLOW_DURATION = 8;
    private static final int FINISH_COUNT = 8;

    private static final float EPSILON = 0.0001F;
    private static final float HEAD_SPREAD = 2F;
    private static final float FINISH_SPREAD = 4F;
    private static final float DIRECTION_SPREAD = 1.2F;
    private static final float SIZE = 0.8F;
    private static final float SIZE_RANGE = 0.8F;
    private static final float GRAVITY = 0.02F;
    private static final float FRICTION = 0.08F;
    private static final int LIFETIME = 14;
    private static final int LIFETIME_RANGE = 14;

    private final ProgressPath path;
    private final int duration;
    private final Palette palette;

    private boolean animateDecrease = false;

    private float from;
    private float target;
    private float current = -1F;
    private int ticks;

    public ProgressBarEffect(ProgressPath path) {
        this(path, DEFAULT_DURATION);
    }

    public ProgressBarEffect(ProgressPath path, int duration) {
        this(path, duration, Palette.PURPLE);
    }

    public ProgressBarEffect(ProgressPath path, int duration, Palette palette) {
        this.path = path;
        this.duration = duration;
        this.palette = palette;
        this.ticks = duration + GLOW_DURATION;
    }

    public ProgressBarEffect animateDecrease(boolean animateDecrease) {
        this.animateDecrease = animateDecrease;

        return this;
    }

    public boolean isActive() {
        return ticks < duration + GLOW_DURATION;
    }

    public boolean isMoving() {
        return ticks < duration;
    }

    public boolean isRising() {
        return target >= from;
    }

    public void snap(float value) {
        from = current = target = value;
        ticks = duration + GLOW_DURATION;
    }

    public void update(float newTarget) {
        if (current < 0F) {
            from = current = target = newTarget;

            return;
        }

        if (newTarget > target + EPSILON || (newTarget < target - EPSILON && animateDecrease)) {
            from = progress(0F);
            target = newTarget;
            ticks = 0;
        } else if (newTarget < target - EPSILON)
            snap(newTarget);
    }

    public void tick(Screen screen, RandomSource random, float x, float y, float scaleX, float scaleY) {
        if (!isActive())
            return;

        ticks++;

        current = progress(0F);

        if (ticks > duration)
            return;

        var headLength = current * path.getLength();
        var fromLength = from * path.getLength();

        emit(screen, random, x, y, scaleX, scaleY, headLength + (random.nextFloat() - 0.5F) * HEAD_SPREAD, HEAD_SPEED);

        if (Math.abs(headLength - fromLength) > 1F && ticks % 2 == 0)
            emit(screen, random, x, y, scaleX, scaleY, Mth.lerp(random.nextFloat(), Math.min(fromLength, headLength), Math.max(fromLength, headLength)), TRAIL_SPEED);

        if (ticks == duration)
            for (var i = 0; i < FINISH_COUNT; i++)
                emit(screen, random, x, y, scaleX, scaleY, headLength + (random.nextFloat() - 0.5F) * FINISH_SPREAD, FINISH_SPEED);
    }

    public float progress(float partialTick) {
        if (ticks >= duration)
            return target;

        var time = Mth.clamp((ticks + partialTick) / duration, 0F, 1F);

        return Mth.lerp(1F - (1F - time) * (1F - time), from, target);
    }

    public float progressOr(float fallback, float partialTick) {
        return current < 0F ? fallback : progress(partialTick);
    }

    public float flash(float partialTick) {
        if (!isActive())
            return 0F;

        if (ticks < duration)
            return 1F;

        return Mth.clamp(1F - (ticks - duration + partialTick) / GLOW_DURATION, 0F, 1F);
    }

    private void emit(Screen screen, RandomSource random, float x, float y, float scaleX, float scaleY, float length, Speed speed) {
        var point = path.pointAt(length, random);

        var warmth = random.nextFloat();

        var bright = palette.brightFrom().lerp(palette.brightTo(), warmth);
        var visible = palette.visibleFrom().lerp(palette.visibleTo(), warmth);

        var directionX = point.nx() + (random.nextFloat() - 0.5F) * DIRECTION_SPREAD;
        var directionY = point.ny() + (random.nextFloat() - 0.5F) * DIRECTION_SPREAD;

        var size = SIZE + random.nextFloat() * SIZE_RANGE;
        var lifetime = LIFETIME + random.nextInt(LIFETIME_RANGE);

        UIParticles.spawn(screen, random, x + point.x() * scaleX, y + point.y() * scaleY, directionX, directionY, Mth.lerp(random.nextFloat(), speed.min(), speed.max()), lifetime, size, GRAVITY, FRICTION, GL11.GL_ONE, bright, visible, UIParticles.transparent(visible));
    }
}
