package it.hurts.sskirillss.relics.handlers;

import net.minecraft.world.item.ItemStack;

public record DismantleProgress(int session, int container, int slot, ItemStack stack, int count, int rank, int hits, long lastHit) {
    public static final int REQUIRED_HITS = 5;
    public static final int HEAL_DELAY = 100;
    public static final int HEAL_INTERVAL = 20;
    public static final DismantleProgress EMPTY = new DismantleProgress(0, -1, -1, ItemStack.EMPTY, 0, 0, 0, 0);

    public static int remainingHits(int hits, long elapsed) {
        return elapsed < HEAL_DELAY ? hits : (int) Math.max(0, hits - 1 - (elapsed - HEAL_DELAY) / HEAL_INTERVAL);
    }
}
