package it.hurts.sskirillss.relics.client.astral;

import it.hurts.sskirillss.relics.items.AstralSatchelItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.astral_satchel.C2SMergeAstralSatchelItems;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AstralMotion {
    public static final float BIRTH_DURATION = 0.95F;
    public static final float FLY_DURATION = 0.5F;
    public static final float FLY_STAGGER = 0.07F;
    public static final float SLIDE_DURATION = 2.2F;
    public static final float THROW_DURATION = 1.6F;
    public static final float SLIDE_SHARPNESS = 7F;
    public static final float SLIDE_TAIL = 0.5F;
    public static final float PULSE_DURATION = 0.28F;
    public static final float PULSE_GROW = 0.35F;
    public static final float PULSE_SHRINK = -0.25F;
    public static final float TICK_DELTA = 0.05F;
    public static final double ATTRACT_MIN_SPEED = 0.7D;
    public static final double ATTRACT_MAX_SPEED = 7D;
    public static final double ATTRACT_CONTACT = 0.35D;
    public static final double OFFSET_DECAY = 2.5D;
    public static final long MERGE_TIMEOUT = 1_500_000_000L;

    private static final Minecraft MC = Minecraft.getInstance();
    private static final Map<UUID, Long> BIRTHS = new HashMap<>();
    private static final Map<UUID, Slide> SLIDES = new HashMap<>();
    private static final Map<UUID, Vec3> OFFSETS = new HashMap<>();
    private static final Map<UUID, Vec3> PREVIOUS_OFFSETS = new HashMap<>();
    private static final Map<UUID, Pulse> PULSES = new HashMap<>();
    private static final List<Flyer> FLYERS = new ArrayList<>();

    private static Map<UUID, AstralSatchelItem.Entry> previous = Map.of();
    private static Merge pendingMerge;

    public record Pulse(long born, float strength) {
    }

    public record Flyer(ItemStack stack, Vec3 start, long born, Vec3 bend) {
    }

    public record Merge(UUID first, UUID second, long born) {
    }

    public record Slide(List<Vec3> path, Vec3 origin, long born, boolean thrown) {
        public float duration() {
            return thrown ? THROW_DURATION : SLIDE_DURATION;
        }

        public float progress(float lag) {
            return Mth.clamp(((System.nanoTime() - born) / 1.0E9F - lag) / duration(), 0F, 1F);
        }

        public double fraction(float lag) {
            var progress = progress(lag);

            if (thrown) {
                var remaining = 1D - progress;

                return 1D - remaining * remaining * remaining;
            }

            return (1D - Math.exp(-progress * SLIDE_SHARPNESS) * (1D + progress * SLIDE_SHARPNESS)) / (1D - Math.exp(-SLIDE_SHARPNESS) * (1D + SLIDE_SHARPNESS));
        }
    }

    public static void begin(List<AstralSatchelItem.Entry> entries) {
        reset();

        var map = new HashMap<UUID, AstralSatchelItem.Entry>();

        for (var entry : entries) {
            BIRTHS.put(entry.id(), -1L);

            map.put(entry.id(), entry);
        }

        previous = map;
    }

    public static void reset() {
        BIRTHS.clear();
        SLIDES.clear();
        OFFSETS.clear();
        PREVIOUS_OFFSETS.clear();
        PULSES.clear();
        FLYERS.clear();

        previous = Map.of();
        pendingMerge = null;
    }

    public static void onMotion(UUID id, List<Vec3> path, boolean thrown) {
        if (!AstralSpace.isEngaged() || path.isEmpty())
            return;

        var points = new ArrayList<Vec3>();

        if (!thrown) {
            var offset = OFFSETS.remove(id);
            var existing = SLIDES.get(id);

            PREVIOUS_OFFSETS.remove(id);

            if (existing != null && existing.progress(0F) < 1F)
                points.add(getPathPoint(existing.path(), existing.fraction(0F)));
            else if (offset != null)
                points.add(path.getFirst().add(offset));
        }

        points.addAll(path);

        SLIDES.put(id, new Slide(points, path.getFirst(), System.nanoTime(), thrown));
    }

    public static void pulse(UUID id, float strength) {
        PULSES.put(id, new Pulse(System.nanoTime(), strength));
    }

    public static void tick(List<AstralSatchelItem.Entry> entries) {
        var now = System.nanoTime();
        var current = new HashMap<UUID, AstralSatchelItem.Entry>();

        for (var entry : entries)
            current.put(entry.id(), entry);

        PREVIOUS_OFFSETS.clear();
        PREVIOUS_OFFSETS.putAll(OFFSETS);

        for (var old : previous.values()) {
            if (current.containsKey(old.id()) || pendingMerge != null && pendingMerge.second().equals(old.id()))
                continue;

            spawnFlyers(old.stack(), getPosition(old, 1F), old.amount(), now);
        }

        for (var entry : entries) {
            var old = previous.get(entry.id());

            if (old == null) {
                var slide = SLIDES.get(entry.id());

                BIRTHS.putIfAbsent(entry.id(), slide == null ? now : slide.born());

                continue;
            }

            if (!old.position().equals(entry.position()) && !SLIDES.containsKey(entry.id()))
                OFFSETS.merge(entry.id(), getSettled(old.position()).subtract(getSettled(entry.position())), Vec3::add);

            if (old.amount() == entry.amount())
                continue;

            pulse(entry.id(), entry.amount() > old.amount() ? PULSE_GROW : PULSE_SHRINK);

            if (entry.amount() < old.amount())
                spawnFlyers(entry.stack(), getPosition(entry, 1F), old.amount() - entry.amount(), now);
        }

        if (pendingMerge != null && (!current.containsKey(pendingMerge.second()) || now - pendingMerge.born() > MERGE_TIMEOUT))
            pendingMerge = null;

        SLIDES.values().removeIf(slide -> slide.progress(-SLIDE_TAIL) >= 1F);
        PULSES.values().removeIf(pulse -> (now - pulse.born()) / 1.0E9F > PULSE_DURATION);
        FLYERS.removeIf(flyer -> (now - flyer.born()) / 1.0E9F > FLY_DURATION);

        BIRTHS.keySet().retainAll(current.keySet());
        OFFSETS.keySet().retainAll(current.keySet());
        PULSES.keySet().retainAll(current.keySet());

        attract(entries, now);

        previous = current;
    }

    private static void attract(List<AstralSatchelItem.Entry> entries, long now) {
        var player = MC.player;

        if (player == null)
            return;

        var pulled = new HashSet<UUID>();
        var positions = new ArrayList<Vec3>();

        for (var entry : entries)
            positions.add(getPosition(entry, 1F));

        for (var i = 0; i < entries.size(); i++) {
            var a = entries.get(i);

            if (pulled.contains(a.id()))
                continue;

            for (var j = i + 1; j < entries.size(); j++) {
                var b = entries.get(j);

                if (pulled.contains(b.id()) || !ItemStack.isSameItemSameComponents(a.stack(), b.stack()) || a.position().distanceTo(b.position()) > AstralSatchelItem.MERGE_RANGE)
                    continue;

                if ((long) a.amount() + b.amount() > AstralSatchelItem.getStackLimit(player, a.stack()))
                    continue;

                pulled.add(a.id());
                pulled.add(b.id());

                var gap = positions.get(j).subtract(positions.get(i));
                var distance = gap.length();

                if (distance < ATTRACT_CONTACT) {
                    if (pendingMerge == null) {
                        pendingMerge = new Merge(a.id(), b.id(), now);

                        NetworkHandler.sendToServer(new C2SMergeAstralSatchelItems(a.id(), b.id()));
                    }

                    break;
                }

                var closeness = Mth.clamp(1D - distance / AstralSatchelItem.MERGE_RANGE, 0D, 1D);
                var speed = Mth.lerp(closeness * closeness, ATTRACT_MIN_SPEED, ATTRACT_MAX_SPEED);
                var step = gap.scale(Math.min(speed * TICK_DELTA, distance) / distance * 0.5D);

                OFFSETS.merge(a.id(), step, Vec3::add);
                OFFSETS.merge(b.id(), step.reverse(), Vec3::add);

                break;
            }
        }

        var decay = Math.exp(-OFFSET_DECAY * TICK_DELTA);

        OFFSETS.replaceAll((id, offset) -> pulled.contains(id) ? offset : offset.scale(decay));
        OFFSETS.entrySet().removeIf(entry -> !pulled.contains(entry.getKey()) && entry.getValue().lengthSqr() < 1.0E-4D);
    }

    public static int getCopies(int amount) {
        if (amount <= 1)
            return 1;

        if (amount <= 8)
            return 2;

        if (amount <= 32)
            return 3;

        if (amount <= 128)
            return 4;

        return amount <= 1024 ? 5 : 6;
    }

    private static void spawnFlyers(ItemStack stack, Vec3 position, int amount, long now) {
        var count = getCopies(amount);

        for (var i = 0; i < count; i++) {
            var angle = i * 2.4F;
            var offset = i == 0 ? Vec3.ZERO : new Vec3(Mth.cos(angle) * 0.2D, 0D, Mth.sin(angle) * 0.2D);
            var bend = i == 0 ? Vec3.ZERO : new Vec3(Mth.cos(angle) * 0.9D, 0.3D * (i % 2), Mth.sin(angle) * 0.9D);

            FLYERS.add(new Flyer(stack, position.add(offset), now + (long) (i * FLY_STAGGER * 1.0E9F), bend));
        }
    }

    public static float getBirth(AstralSatchelItem.Entry entry) {
        var born = BIRTHS.get(entry.id());

        if (born == null)
            return 0F;

        if (born < 0L)
            return 1F;

        var progress = Mth.clamp((System.nanoTime() - born) / 1.0E9F / BIRTH_DURATION, 0F, 1F);
        var remaining = 1F - progress;

        return 1F - remaining * remaining * remaining * remaining;
    }

    public static float getPulse(UUID id) {
        var pulse = PULSES.get(id);

        if (pulse == null)
            return 1F;

        var progress = Mth.clamp((System.nanoTime() - pulse.born()) / 1.0E9F / PULSE_DURATION, 0F, 1F);

        return 1F + pulse.strength() * Mth.sin((float) Math.PI * progress);
    }

    public static double getSlideFraction(UUID id, float lag) {
        var slide = SLIDES.get(id);

        return slide == null ? 1D : slide.fraction(lag);
    }

    public static float getSlideEnergy(UUID id) {
        var slide = SLIDES.get(id);

        if (slide == null)
            return 0F;

        var progress = slide.progress(0F);

        if (slide.thrown())
            return (1F - progress) * (1F - progress);

        var scaled = progress * SLIDE_SHARPNESS;

        return Math.min(1F, scaled * (float) Math.exp(1D - scaled));
    }

    public static Vec3 getPosition(AstralSatchelItem.Entry entry, float partialTick) {
        return getPosition(entry, partialTick, 0F);
    }

    public static Vec3 getPosition(AstralSatchelItem.Entry entry, float partialTick, float lag) {
        var offset = OFFSETS.getOrDefault(entry.id(), Vec3.ZERO);

        return getBasePosition(entry, lag).add(PREVIOUS_OFFSETS.getOrDefault(entry.id(), offset).lerp(offset, partialTick));
    }

    private static Vec3 getBasePosition(AstralSatchelItem.Entry entry, float lag) {
        var position = entry.position();
        var slide = SLIDES.get(entry.id());

        if (slide == null)
            return getSettled(position);

        var fraction = slide.fraction(lag);

        return getPathPoint(slide.path(), fraction).add(getDrift(slide.origin()).lerp(getDrift(position), fraction));
    }

    private static Vec3 getSettled(Vec3 position) {
        return AstralSatchelItem.clamp(position, AstralSpace.getRadius()).add(getDrift(position));
    }

    private static Vec3 getDrift(Vec3 position) {
        var phase = (float) (position.x * 12.9898D + position.y * 78.233D + position.z * 37.719D);
        var time = AstralSpace.getTime();

        return new Vec3(
                Mth.sin(time * 0.7F + phase) * 0.25D,
                Mth.sin(time * 0.9F + phase * 1.3F) * 0.2D,
                Mth.cos(time * 0.6F + phase * 0.7F) * 0.25D
        );
    }

    private static Vec3 getPathPoint(List<Vec3> path, double fraction) {
        var total = 0D;

        for (var i = 1; i < path.size(); i++)
            total += path.get(i).distanceTo(path.get(i - 1));

        var remaining = total * fraction;

        for (var i = 1; i < path.size(); i++) {
            var length = path.get(i).distanceTo(path.get(i - 1));

            if (remaining <= length && length > 1.0E-6D)
                return path.get(i - 1).lerp(path.get(i), remaining / length);

            remaining -= length;
        }

        return path.getLast();
    }

    public static List<Flyer> getFlyers() {
        return FLYERS;
    }

    public static float getFlyerProgress(Flyer flyer) {
        var progress = Mth.clamp((System.nanoTime() - flyer.born()) / 1.0E9F / FLY_DURATION, 0F, 1F);

        return progress * progress * (3F - 2F * progress);
    }

    public static Vec3 getFlyerPosition(Flyer flyer, float partialTick) {
        var player = MC.player;

        if (player == null)
            return flyer.start();

        var t = getFlyerProgress(flyer);
        var target = AstralSpace.getCamera(partialTick).add(player.getViewVector(1F).scale(0.7D)).add(0D, -0.55D, 0D);
        var control = flyer.start().lerp(target, 0.5D).add(0D, 1.2D, 0D).add(flyer.bend());
        var inverse = 1D - t;

        return flyer.start().scale(inverse * inverse).add(control.scale(2D * inverse * t)).add(target.scale((double) t * t));
    }
}
