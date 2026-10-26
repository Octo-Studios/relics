package it.hurts.sskirillss.relics.client.screen.utils.misc;

import lombok.Getter;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class ProgressPath {
    public record Point(float x, float y, float nx, float ny) {
    }

    private record Segment(float x1, float y1, float x2, float y2, float nx, float ny, float start, float length) {
    }

    private final List<Segment> segments = new ArrayList<>();
    private final float jitter;
    private final boolean bothSides;

    @Getter
    private float length = 0F;

    private ProgressPath(float jitter, boolean bothSides) {
        this.jitter = jitter;
        this.bothSides = bothSides;
    }

    public static ProgressPath cardFrame() {
        return new ProgressPath(0F, false)
                .segment(6F, 1.5F, 0F, 1.5F, 0F, -1F)
                .segment(1.5F, 3F, 1.5F, 43F, -1F, 0F)
                .segment(1.5F, 44.5F, 4.5F, 44.5F, -0.7F, 0.7F)
                .segment(3F, 44.5F, 31F, 44.5F, 0F, 1F)
                .segment(31F, 44.5F, 34F, 44.5F, 0.7F, 0.7F)
                .segment(32.5F, 43F, 32.5F, 3F, 1F, 0F)
                .segment(32.5F, 3F, 32.5F, 0F, 0.7F, -0.7F)
                .segment(31F, 1.5F, 28F, 1.5F, 0F, -1F);
    }

    public static ProgressPath horizontalBar(float width, float height) {
        return new ProgressPath(height / 2F, false)
                .segment(0F, height / 2F, width, height / 2F, 0F, -1F);
    }

    public static ProgressPath line(float x1, float y1, float x2, float y2, float jitter) {
        var length = (float) Math.hypot(x2 - x1, y2 - y1);

        var nx = length <= 0F ? 0F : -(y2 - y1) / length;
        var ny = length <= 0F ? -1F : (x2 - x1) / length;

        return new ProgressPath(jitter, true)
                .segment(x1, y1, x2, y2, nx, ny);
    }

    public ProgressPath segment(float x1, float y1, float x2, float y2, float nx, float ny) {
        var segmentLength = (float) Math.hypot(x2 - x1, y2 - y1);

        segments.add(new Segment(x1, y1, x2, y2, nx, ny, length, segmentLength));

        length += segmentLength;

        return this;
    }

    public Point pointAt(float distance, RandomSource random) {
        var clamped = Mth.clamp(distance, 0F, length);

        var segment = segments.get(segments.size() - 1);

        for (var entry : segments) {
            if (clamped <= entry.start + entry.length) {
                segment = entry;

                break;
            }
        }

        var progress = segment.length <= 0F ? 0F : Mth.clamp((clamped - segment.start) / segment.length, 0F, 1F);

        var x = Mth.lerp(progress, segment.x1, segment.x2);
        var y = Mth.lerp(progress, segment.y1, segment.y2);

        var nx = segment.nx;
        var ny = segment.ny;

        if (bothSides && random.nextBoolean()) {
            nx = -nx;
            ny = -ny;
        }

        if (jitter > 0F && segment.length > 0F) {
            var offset = (random.nextFloat() - 0.5F) * 2F * jitter;

            x -= (segment.y2 - segment.y1) / segment.length * offset;
            y += (segment.x2 - segment.x1) / segment.length * offset;
        }

        return new Point(x, y, nx, ny);
    }
}
