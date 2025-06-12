package it.hurts.sskirillss.relics.utils.data;

import com.google.gson.JsonParser;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class AnimationData {
    private final List<Pair<Integer, Integer>> frames = new ArrayList<>();

    public int getLength() {
        return frames.stream()
                .mapToInt(Pair::getRight)
                .sum();
    }

    @Deprecated(forRemoval = true)
    public Pair<Integer, Integer> getFrameByTime(final long time) {
        final long total = getLength();
        if (total == 0) {
            return Pair.of(0, 0);
        }

        var rem = time % total;
        final int size = frames.size();
        var idx = 0;

        while (rem >= 0) {
            final var p = frames.get(idx);
            rem -= p.getRight();
            if (rem < 0) {
                return p;
            }
            idx = (idx + 1) % size;
        }

        return frames.getFirst();
    }

    public AnimationData frame(final int index, final int time) {
        frames.add(Pair.of(index, time));
        return this;
    }

    public static AnimationData construct(final int texHeight, final int patternHeight, final int frameTime) {
        var data = new AnimationData();
        final var count = texHeight / patternHeight;
        for (var i = 0; i < count; i++) {
            data.frame(i, frameTime);
        }
        return data;
    }

    public static AnimationData fromMcmeta(final Path path) {
        try (var in = Files.newInputStream(path)) {
            return fromMcmeta(in);
        } catch (final IOException e) {
            return new AnimationData();
        }
    }

    public static AnimationData fromMcmeta(final ResourceLocation location) {
        var manager = Minecraft.getInstance().getResourceManager();
        var opt = manager.getResource(location);
        if (opt.isEmpty()) {
            return new AnimationData();
        }

        try (var in = opt.get().open()) {
            return fromMcmeta(in);
        } catch (final IOException e) {
            return new AnimationData();
        }
    }

    private static AnimationData fromMcmeta(final InputStream input) {
        var reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        var rootElem = JsonParser.parseReader(reader);
        if (!rootElem.isJsonObject()) {
            return new AnimationData();
        }

        var obj = rootElem.getAsJsonObject();
        var anim = obj.has("animation") && obj.get("animation").isJsonObject()
                ? obj.getAsJsonObject("animation")
                : null;
        if (anim == null) {
            return new AnimationData();
        }

        var data = new AnimationData();
        final var defTime = anim.has("frametime")
                ? anim.get("frametime").getAsInt()
                : 1;

        if (anim.has("frames") && anim.get("frames").isJsonArray()) {
            var arr = anim.getAsJsonArray("frames");
            for (var e : arr) {
                if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber()) {
                    data.frame(e.getAsInt(), defTime);
                } else if (e.isJsonObject()) {
                    var o = e.getAsJsonObject();
                    final var idx = o.has("index")
                            ? o.get("index").getAsInt()
                            : 0;
                    final var t = o.has("time")
                            ? o.get("time").getAsInt()
                            : defTime;
                    data.frame(idx, t);
                }
            }
        }

        return data;
    }

    public static AnimationData builder() {
        return new AnimationData();
    }
}