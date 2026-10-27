package it.hurts.sskirillss.relics.client.screen.utils.misc;

public class DeltaCounter {
    private final FloatingTexts texts;

    private FloatingTexts.Entry entry = null;
    private boolean initialized = false;
    private int last = 0;
    private int delta = 0;

    public DeltaCounter(FloatingTexts texts) {
        this.texts = texts;
    }

    public void reset() {
        release();

        initialized = false;
    }

    public void update(int shown, boolean moving, int positiveColor, int negativeColor, float x, float y) {
        if (!initialized) {
            initialized = true;
            last = shown;

            return;
        }

        if (shown != last) {
            var step = shown - last;

            if (entry != null && Integer.signum(step) != Integer.signum(delta))
                release();

            delta += step;

            entry = texts.counter(entry, (delta > 0 ? "+" : "") + delta, delta > 0 ? positiveColor : negativeColor, x, y, delta > 0 ? -1F : 1F);
        }

        last = shown;

        if (entry != null && !moving)
            release();
    }

    private void release() {
        texts.release(entry);

        entry = null;
        delta = 0;
    }
}
