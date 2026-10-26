package it.hurts.sskirillss.relics.client.screen.utils.misc;

import net.minecraft.util.Mth;

public class RankupEffect {
    private static final int SHAKE_DURATION = 8;

    private static final float SHAKE_AMPLITUDE = 3.2F;
    private static final float SHAKE_FREQUENCY_X = 5.1F;
    private static final float SHAKE_FREQUENCY_Y = 6.7F;

    private int age = SHAKE_DURATION;

    public void start() {
        age = 0;
    }

    public void tick() {
        if (age < SHAKE_DURATION)
            age++;
    }

    public float shakeX(float partialTick) {
        return age >= SHAKE_DURATION ? 0F : Mth.sin((age + partialTick) * SHAKE_FREQUENCY_X) * amplitude(partialTick);
    }

    public float shakeY(float partialTick) {
        return age >= SHAKE_DURATION ? 0F : Mth.cos((age + partialTick) * SHAKE_FREQUENCY_Y) * amplitude(partialTick);
    }

    private float amplitude(float partialTick) {
        var remaining = Mth.clamp(1F - (age + partialTick) / SHAKE_DURATION, 0F, 1F);

        return SHAKE_AMPLITUDE * remaining * remaining;
    }
}
