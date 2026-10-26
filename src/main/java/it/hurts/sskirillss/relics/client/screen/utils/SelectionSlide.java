package it.hurts.sskirillss.relics.client.screen.utils;

import net.minecraft.util.Mth;

public class SelectionSlide {
    private static final int SLIDE_DURATION = 6;
    private static final int FLASH_DURATION = 8;
    private static final int NUDGE_DURATION = 6;

    private static final float OVERSHOOT = 1.70158F;
    private static final float NUDGE_DISTANCE = 2F;

    private String key = null;
    private float from = 0F;
    private int target = 0;

    private int slideTicks = SLIDE_DURATION;
    private int flashTicks = FLASH_DURATION;
    private int nudgeTicks = NUDGE_DURATION;
    private int nudgeStep = 0;

    public boolean update(String key, int index) {
        if (!key.equals(this.key)) {
            this.key = key;
            this.from = index;
            this.target = index;
            this.slideTicks = SLIDE_DURATION;
            this.flashTicks = FLASH_DURATION;

            return false;
        }

        if (index == target)
            return false;

        from = position(0F);
        target = index;
        slideTicks = 0;
        flashTicks = 0;

        return true;
    }

    public void press(int step) {
        nudgeStep = step;
        nudgeTicks = 0;
    }

    public void tick() {
        if (slideTicks < SLIDE_DURATION)
            slideTicks++;

        if (flashTicks < FLASH_DURATION)
            flashTicks++;

        if (nudgeTicks < NUDGE_DURATION)
            nudgeTicks++;
    }

    public float position(float partialTick) {
        if (slideTicks >= SLIDE_DURATION)
            return target;

        var time = Mth.clamp((slideTicks + partialTick) / SLIDE_DURATION, 0F, 1F) - 1F;

        return Mth.lerp(1F + (OVERSHOOT + 1F) * time * time * time + OVERSHOOT * time * time, from, target);
    }

    public float flash(float partialTick) {
        return Mth.clamp(1F - (flashTicks + partialTick) / FLASH_DURATION, 0F, 1F);
    }

    public float nudge(int step, float partialTick) {
        if (step != nudgeStep || nudgeTicks >= NUDGE_DURATION)
            return 0F;

        return step * NUDGE_DISTANCE * Mth.sin((nudgeTicks + partialTick) / NUDGE_DURATION * Mth.PI);
    }
}
