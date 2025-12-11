package it.hurts.sskirillss.relics.dev.chromatic_aberration;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.dev.anchor.Anchor;
import it.hurts.sskirillss.relics.dev.anchor.EntityAnchor;
import it.hurts.sskirillss.relics.dev.anchor.PositionAnchor;
import lombok.Data;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
public class ChromaticAberration {
    private static final float EPSILON = 1.0e-5f;

    private Anchor anchor;
    private float radius;
    private int duration;
    private int fadeInTime;
    private int fadeOutTime;
    private float strength;
    private UUID uuid;
    private List<Integer> colors;
    private int elapsedTime;

    private ChromaticAberration(
            Anchor anchor,
            float radius,
            int duration,
            int fadeInTime,
            int fadeOutTime,
            float strength,
            List<Integer> colors,
            UUID uuid
    ) {
        this.anchor = anchor;
        this.radius = Math.max(0F, radius);
        this.duration = Math.max(0, duration);
        this.fadeInTime = Math.max(0, fadeInTime);
        this.fadeOutTime = fadeOutTime;
        this.strength = Math.max(0F, strength);
        this.colors = Objects.requireNonNull(colors, "colors");
        this.uuid = uuid;
    }

    public static Builder builder(Anchor anchor) {
        return new Builder(anchor);
    }

    public static Builder builder(Entity entity) {
        return ChromaticAberration.builder(new EntityAnchor(entity.getId()));
    }

    public static Builder builder(Vec3 position) {
        return ChromaticAberration.builder(new PositionAnchor(position));
    }

    public boolean isFinished() {
        return elapsedTime >= getDurationResolved();
    }

    public boolean update(Player player) {
        elapsedTime++;
        return isFinished();
    }

    public float getStrength(@NotNull Player player) {
        var df = distanceFactor(player);
        if (df <= 0F) return 0F;
        var tf = timeFactorSmooth();
        if (tf <= 0F) return 0F;
        return df * tf * strength;
    }

    private float distanceFactor(Player player) {
        var r = radius;
        if (r <= 0F) return 0F;
        var src = anchor.getPosition(player.level());
        var distance = (float) player.position().distanceTo(src);
        if (distance < EPSILON) return 1F;
        if (distance >= r) return 0F;
        return Mth.clamp(1F - (distance / r), 0F, 1F);
    }

    private float timeFactorSmooth() {
        var dur = getDurationResolved();
        if (dur <= 0) return 0F;
        var fadeIn = Math.max(0, fadeInTime);
        var fadeOut = getFadeOutTimeResolved();

        if (elapsedTime < fadeIn && fadeIn > 0) {
            var t = (float) elapsedTime / fadeIn;
            return smoothstep(t);
        }

        if (elapsedTime > dur - fadeOut && fadeOut > 0) {
            var t = (float) (dur - elapsedTime) / fadeOut;
            return smoothstep(Mth.clamp(t, 0F, 1F));
        }

        return 1F;
    }

    private static float smoothstep(float t) {
        t = Mth.clamp(t, 0F, 1F);
        return t * t * (3F - 2F * t);
    }

    private int getDurationResolved() {
        return Math.max(0, duration);
    }

    private int getFadeOutTimeResolved() {
        if (fadeOutTime == -1) {
            var dur = getDurationResolved();
            return Math.max(0, dur - Math.max(0, fadeInTime));
        }
        return Math.max(0, fadeOutTime);
    }

    public float getProgress() {
        var dur = Math.max(1, getDurationResolved());
        return Mth.clamp((float) elapsedTime / dur, 0F, 1F);
    }

    @Deprecated
    public float getRangeMultiplier() {
        return radius;
    }

    @Deprecated
    public void setRangeMultiplier(float v) {
        this.radius = Math.max(0F, v);
    }

    public static final StreamCodec<ByteBuf, ChromaticAberration> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ChromaticAberration decode(ByteBuf buffer) {
            var fbb = new FriendlyByteBuf(buffer);

            var type = Anchor.AnchorType.values()[fbb.readByte()];

            var anchor = switch (type) {
                case POSITION -> new PositionAnchor(new Vec3(FriendlyByteBuf.readVector3f(fbb)));
                case ENTITY -> new EntityAnchor(fbb.readInt());
            };

            var radius = fbb.readFloat();
            var duration = fbb.readInt();
            var fadeIn = fbb.readInt();
            var fadeOut = fbb.readInt();
            var strength = fbb.readFloat();

            var count = fbb.readVarInt();
            var colors = new ArrayList<Integer>(count);
            for (var i = 0; i < count; i++) {
                colors.add(fbb.readInt());
            }

            return ChromaticAberration.builder(anchor)
                    .radius(radius)
                    .duration(duration)
                    .fadeInTime(fadeIn)
                    .fadeOutTime(fadeOut)
                    .strength(strength)
                    .colors(colors)
                    .build();
        }

        @Override
        public void encode(ByteBuf buffer, ChromaticAberration effect) {
            var fbb = new FriendlyByteBuf(buffer);
            var anchor = effect.getAnchor();

            fbb.writeByte(anchor.getType().ordinal());

            switch (anchor.getType()) {
                case POSITION -> FriendlyByteBuf.writeVector3f(fbb, ((PositionAnchor) anchor).getPosition().toVector3f());
                case ENTITY -> fbb.writeInt(((EntityAnchor) anchor).getId());
            }

            fbb.writeFloat(effect.getRadius());
            fbb.writeInt(effect.getDuration());
            fbb.writeInt(effect.getFadeInTime());
            fbb.writeInt(effect.getFadeOutTime());
            fbb.writeFloat(effect.getStrength());

            var colors = effect.getColors();
            fbb.writeVarInt(colors.size());
            for (var color : colors) {
                fbb.writeInt(color);
            }
        }
    };

    public static class Builder {
        private final Anchor anchor;
        private float radius = 1F;
        private int duration = 20;
        private int fadeInTime = 0;
        private int fadeOutTime = -1;
        private float strength = 1F;
        private List<Integer> colors = new ArrayList<>();
        private UUID uuid = UUID.randomUUID();

        public Builder(Anchor anchor) {
            this.anchor = anchor;
        }

        public Builder radius(float v) {
            this.radius = v;
            return this;
        }

        @Deprecated
        public Builder rangeMultiplier(float v) {
            return radius(v);
        }

        public Builder duration(int v) {
            this.duration = v;
            return this;
        }

        public Builder fadeInTime(int v) {
            this.fadeInTime = v;
            return this;
        }

        public Builder fadeOutTime(int v) {
            this.fadeOutTime = v;
            return this;
        }

        public Builder strength(float v) {
            this.strength = v;
            return this;
        }

        public Builder colors(List<Integer> v) {
            this.colors = Objects.requireNonNull(v, "colors");
            return this;
        }

        public Builder color(int v) {
            this.colors.add(v);
            return this;
        }

        public Builder uuid(UUID id) {
            this.uuid = Objects.requireNonNull(id, "uuid");
            return this;
        }

        public ChromaticAberration build() {
            if (radius < 0F) throw new IllegalArgumentException("radius must be >= 0");
            if (duration < 0) throw new IllegalArgumentException("duration must be >= 0");
            if (colors.isEmpty()) throw new IllegalArgumentException("colors must not be empty");
            if (strength < 0F) throw new IllegalArgumentException("strength must be >= 0");

            return new ChromaticAberration(
                    anchor,
                    radius,
                    duration,
                    fadeInTime,
                    fadeOutTime,
                    strength,
                    colors,
                    uuid
            );
        }
    }
}
