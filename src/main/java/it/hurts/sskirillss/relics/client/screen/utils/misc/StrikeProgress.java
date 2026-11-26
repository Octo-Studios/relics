package it.hurts.sskirillss.relics.client.screen.utils.misc;

public record StrikeProgress(int hits, long lastHit) {
    private static final int HEAL_DELAY = 100;
    private static final int HEAL_INTERVAL = 20;

    public static int remainingHits(int hits, long elapsed) {
        return elapsed < HEAL_DELAY ? hits : (int) Math.max(0, hits - 1 - (elapsed - HEAL_DELAY) / HEAL_INTERVAL);
    }
}
