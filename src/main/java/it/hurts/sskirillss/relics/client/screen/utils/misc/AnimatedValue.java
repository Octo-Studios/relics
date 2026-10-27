package it.hurts.sskirillss.relics.client.screen.utils.misc;

import it.hurts.octostudios.octolib.client.animation.Tween;
import it.hurts.octostudios.octolib.client.animation.easing.EaseType;
import it.hurts.octostudios.octolib.client.animation.easing.TransitionType;
import lombok.Getter;
import lombok.Setter;

public class AnimatedValue {
    private static final double TICKS_PER_SECOND = 20D;
    private static final float EPSILON = 0.001F;

    @Getter
    @Setter
    private float value = 0F;

    private float target = 0F;
    @Getter
    private boolean initialized = false;
    private Tween tween;

    public boolean isMoving() {
        return Math.abs(value - target) > EPSILON;
    }

    public int getInt(int fallback) {
        return initialized ? Math.round(value) : fallback;
    }

    public void snap(float newValue) {
        if (tween != null)
            tween.kill();

        initialized = true;
        value = newValue;
        target = newValue;
    }

    public void set(float newTarget, int durationTicks) {
        if (!initialized) {
            snap(newTarget);

            return;
        }

        if (tween != null)
            tween.kill();

        target = newTarget;

        tween = Tween.create();

        tween.tweenMethod(this::setValue, value, newTarget, durationTicks / TICKS_PER_SECOND)
                .setEaseType(EaseType.EASE_OUT)
                .setTransitionType(TransitionType.QUAD);

        tween.start();
    }
}
