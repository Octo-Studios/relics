package it.hurts.sskirillss.relics.client.screen.utils.misc;

import net.minecraft.Util;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

public class ToggleSwitchAnimator {
    private static final float DURATION = 250F;

    private final Map<Object, State> states = new HashMap<>();

    public float amount(Object key, boolean enabled) {
        var now = Util.getMillis();

        var state = states.computeIfAbsent(key, entry -> new State(enabled ? 1F : 0F, now));

        var step = (now - state.time) / DURATION;

        state.time = now;
        state.value += Mth.clamp((enabled ? 1F : 0F) - state.value, -step, step);

        return state.value;
    }

    private static class State {
        private float value;
        private long time;

        private State(float value, long time) {
            this.value = value;
            this.time = time;
        }
    }
}
