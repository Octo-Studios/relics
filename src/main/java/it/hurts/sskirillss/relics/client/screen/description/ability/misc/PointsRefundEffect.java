package it.hurts.sskirillss.relics.client.screen.description.ability.misc;

import it.hurts.sskirillss.relics.client.screen.description.general.particles.FlyingParticleData;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.LevelingPointsPlateWidget;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.client.screen.utils.misc.LevelUpEffect;
import it.hurts.sskirillss.relics.client.screen.utils.misc.UIParticles;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class PointsRefundEffect {
    private static class Arrival {
        private int ticks;
        private float x;
        private float y;
        private int amount;
        private boolean last;
        private LevelingPointsPlateWidget plate;
    }

    private static final int TINT = UIParticles.GOLD;
    private static final int BASE_FLIGHT_DELAY = 6;
    private static final int BASE_FLIGHT_DURATION = 13;
    private static final int MAX_TRAILS = 20;
    private static final int FLIGHT_JITTER = 4;
    private static final int ARRIVAL_PARTICLES = 2;
    private static final int LAST_ARRIVAL_PARTICLES = 14;

    private static final float POINTS_SPEED_STEP = 15F;
    private static final float POINTS_SPEED_MAX = 3F;
    private static final float FLIGHT_DELAY_FACTOR = 1.5F;
    private static final float FLIGHT_SCALE = 1.6F;
    private static final float FLIGHT_START_WIDTH = 24F;
    private static final float FLIGHT_START_HEIGHT = 50F;
    private static final float FLIGHT_CONTROL_WIDTH = 70F;
    private static final float FLIGHT_CONTROL_RISE = 10F;
    private static final float FLIGHT_CONTROL_RISE_RANGE = 40F;
    private static final float PLATE_TARGET_OFFSET = 10F;
    private static final float ARRIVAL_PULSE = 0.3F;
    private static final float LAST_ARRIVAL_PULSE = 0.6F;

    private final List<Arrival> arrivals = new ArrayList<>();

    public static float getSpeed(int points) {
        return Mth.clamp(1F + points / POINTS_SPEED_STEP, 1F, POINTS_SPEED_MAX);
    }

    public void start(Screen screen, RandomSource random, float centerX, float centerY, int levels, int requiredPoints) {
        var points = levels * requiredPoints;

        var plate = screen.children().stream()
                .filter(LevelingPointsPlateWidget.class::isInstance)
                .map(LevelingPointsPlateWidget.class::cast)
                .findFirst()
                .orElse(null);

        if (plate == null || points <= 0)
            return;

        plate.hide(points);

        var delay = Math.round(BASE_FLIGHT_DELAY * getSpeed(points) * FLIGHT_DELAY_FACTOR);

        var targetX = plate.getX() + PLATE_TARGET_OFFSET;
        var targetY = plate.getY() + PLATE_TARGET_OFFSET;

        var color = new Color(TINT);
        var batch = new ArrayList<Arrival>();

        for (var i = 0; i < Mth.clamp(levels, 1, MAX_TRAILS); i++) {
            var startX = centerX + (random.nextFloat() - 0.5F) * FLIGHT_START_WIDTH;
            var startY = centerY + (random.nextFloat() - 0.5F) * FLIGHT_START_HEIGHT;

            var controlX = (startX + targetX) / 2F + (random.nextFloat() - 0.5F) * FLIGHT_CONTROL_WIDTH;
            var controlY = Math.min(startY, targetY) - FLIGHT_CONTROL_RISE - random.nextFloat() * FLIGHT_CONTROL_RISE_RANGE;

            var particleDelay = random.nextInt(delay + 1);
            var particleFlight = BASE_FLIGHT_DURATION - random.nextInt(FLIGHT_JITTER);

            ParticleStorage.addParticle(screen, new FlyingParticleData(color, startX, startY, targetX, targetY, controlX, controlY, particleDelay, particleFlight, FLIGHT_SCALE, random.nextInt()));

            var arrival = new Arrival();

            arrival.ticks = particleDelay + particleFlight + 1;
            arrival.x = targetX;
            arrival.y = targetY;
            arrival.amount = requiredPoints;
            arrival.plate = plate;

            batch.add(arrival);
        }

        var last = batch.get(0);

        for (var entry : batch)
            if (entry.ticks >= last.ticks)
                last = entry;

        last.last = true;

        arrivals.addAll(batch);
    }

    public void tick(Screen screen, RandomSource random) {
        arrivals.forEach(arrival -> arrival.ticks--);

        for (var arrival : arrivals) {
            if (arrival.ticks != 0)
                continue;

            arrival.plate.pulse(arrival.last ? LAST_ARRIVAL_PULSE : ARRIVAL_PULSE);
            arrival.plate.reveal(arrival.last ? Integer.MAX_VALUE : arrival.amount);

            LevelUpEffect.burstAt(screen, random, arrival.x, arrival.y, TINT, arrival.last ? LAST_ARRIVAL_PARTICLES : ARRIVAL_PARTICLES);
        }

        arrivals.removeIf(arrival -> arrival.ticks <= 0);
    }
}
