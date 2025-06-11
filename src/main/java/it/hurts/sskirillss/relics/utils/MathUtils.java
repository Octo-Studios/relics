package it.hurts.sskirillss.relics.utils;

import net.minecraft.util.RandomSource;

import java.util.Random;

public class MathUtils {
    public static float randomFloat(RandomSource random) {
        return -1 + 2 * random.nextFloat();
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(max, Math.min(value, min));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(max, Math.min(min, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(max, Math.min(value, min));
    }

    public static float randomBetween(Random random, float min, float max) {
        return random.nextFloat() * (max - min) + min;
    }

    public static double randomBetween(Random random, double min, double max) {
        return random.nextDouble() * (max - min) + min;
    }

    public static int randomBetween(Random random, int min, int max) {
        return (int) Math.round(randomBetween(random, (double) min, (double) max));
    }

    public static double round(double value, int steps) {
        double multiplier = Math.pow(10, steps);

        return Math.round(value * multiplier) / multiplier;
    }

    @Deprecated(forRemoval = true)
    public static int multicast(RandomSource random, double chance, double chanceMultiplier) {
        return random.nextDouble() <= chance ? multicast(random, chance * chanceMultiplier, chanceMultiplier) + 1 : 0;
    }

    public static int multicast(RandomSource random, double chance, int maxIterations) {
        int count = 0;

        while (count < maxIterations && random.nextDouble() <= chance)
            count++;

        return count;
    }

    public static int multicast(RandomSource random, double chance) {
        return multicast(random, chance, 100);
    }

    public static String formatTime(long totalSeconds) {
        final var SECONDS_PER_MINUTE = 60;
        final var SECONDS_PER_HOUR = 60 * SECONDS_PER_MINUTE;
        final var SECONDS_PER_DAY = 24 * SECONDS_PER_HOUR;

        var days = totalSeconds / SECONDS_PER_DAY;
        var rem = totalSeconds % SECONDS_PER_DAY;

        var hours = rem / SECONDS_PER_HOUR;

        rem %= SECONDS_PER_HOUR;

        var minutes = rem / SECONDS_PER_MINUTE;
        var seconds = rem % SECONDS_PER_MINUTE;

        long[] parts = { days, hours, minutes, seconds };

        var first = 0;

        while (first < parts.length && parts[first] == 0)
            first++;

        if (first == parts.length)
            return "0";

        var builder = new StringBuilder();

        for (int i = first; i < parts.length; i++) {
            if (i > first)
                builder.append(':');
            builder.append(String.format("%02d", parts[i]));
        }

        return builder.toString();
    }
}