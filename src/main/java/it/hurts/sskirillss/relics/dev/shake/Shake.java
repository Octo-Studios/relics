package it.hurts.sskirillss.relics.dev.shake;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.dev.shake.dev.EntityShakeAnchor;
import it.hurts.sskirillss.relics.dev.shake.dev.PositionShakeAnchor;
import it.hurts.sskirillss.relics.dev.shake.dev.ShakeAnchor;
import lombok.Data;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.SplittableRandom;
import java.util.UUID;

@Data
public class Shake {
    private static final float TAU = (float) (Math.PI * 2.0);
    private static final float EPSILON = 1.0e-5f;

    private ShakeAnchor anchor;
    private float radius;
    private float rotationAmplitude;
    private float offsetAmplitude;
    private float fovAmplitude;
    private float rotationSpeed;
    private float offsetSpeed;
    private float fovSpeed;
    private int duration;
    private int fadeInTime;
    private int fadeOutTime;
    private UUID uuid;
    private Vector3f frequency;
    private final Vector3f lastTickOffset = new Vector3f();
    private final Vector3f currentTickOffset = new Vector3f();
    private final Vector3f lastTickRotation = new Vector3f();
    private final Vector3f currentTickRotation = new Vector3f();
    private float lastTickFov;
    private float currentTickFov;
    private int elapsedTime;
    private float timeSec;

    private Shake(
            ShakeAnchor anchor,
            float radius,
            float rotationAmplitude, float offsetAmplitude, float fovAmplitude,
            float rotationSpeed, float offsetSpeed, float fovSpeed,
            int duration, int fadeInTime, int fadeOutTime,
            UUID uuid
    ) {
        this.anchor = anchor;
        this.radius = Math.max(0F, radius);
        this.rotationAmplitude = Math.max(0F, rotationAmplitude);
        this.offsetAmplitude = Math.max(0F, offsetAmplitude);
        this.fovAmplitude = Math.max(0F, fovAmplitude);
        this.rotationSpeed = Math.max(0F, rotationSpeed);
        this.offsetSpeed = Math.max(0F, offsetSpeed);
        this.fovSpeed = Math.max(0F, fovSpeed);
        this.duration = Math.max(0, duration);
        this.fadeInTime = Math.max(0, fadeInTime);
        this.fadeOutTime = fadeOutTime;
        this.uuid = uuid;
        this.frequency = generateFrequency(uuid);
    }

    public static ShakeBuilder builder(ShakeAnchor anchor) {
        return new ShakeBuilder(anchor);
    }

    public static ShakeBuilder builder(Entity entity) {
        return Shake.builder(new EntityShakeAnchor(entity.getId()));
    }

    public static ShakeBuilder builder(Vec3 position) {
        return Shake.builder(new PositionShakeAnchor(position));
    }

    public boolean isFinished() {
        return elapsedTime >= getDurationResolved();
    }

    public boolean update(Player player) {
        elapsedTime++;
        timeSec += 1F / 20F;

        lastTickOffset.set(currentTickOffset);
        lastTickRotation.set(currentTickRotation);
        lastTickFov = currentTickFov;

        var rotAmp = getCumulativeRotationAmplitude(player);
        var rotSpd = getCumulativeRotationSpeed(player);

        if (rotAmp > 0F && rotSpd > 0F) {
            currentTickRotation.set(computeRotationForTick(player, rotAmp, rotSpd, timeSec));
        } else {
            currentTickRotation.zero();
            lastTickRotation.zero();
        }

        var offAmp = getCumulativeOffsetAmplitude(player);
        var offSpd = getCumulativeOffsetSpeed(player);

        if (offAmp > 0F && offSpd > 0F) {
            currentTickOffset.set(computeOffsetForTick(player, offAmp, offSpd, timeSec));
        } else {
            currentTickOffset.zero();
            lastTickOffset.zero();
        }

        var fovAmp = getCumulativeFovAmplitude(player);
        var fovSpd = getCumulativeFovSpeed(player);

        if (fovAmp > 0F && fovSpd > 0F) {
            currentTickFov = computeFovForTick(fovAmp, fovSpd, timeSec);
        } else {
            currentTickFov = 0F;
            lastTickFov = 0F;
        }

        return isFinished();
    }

    public void getShakeOffset(@NotNull Player player, float partialTicks, @NotNull Vector3f out) {
        out.set(
                Mth.lerp(partialTicks, lastTickOffset.x(), currentTickOffset.x()),
                Mth.lerp(partialTicks, lastTickOffset.y(), currentTickOffset.y()),
                Mth.lerp(partialTicks, lastTickOffset.z(), currentTickOffset.z())
        );
    }

    public Vector3f getShakeOffset(@NotNull Player player, float partialTicks) {
        var out = new Vector3f();
        getShakeOffset(player, partialTicks, out);
        return out;
    }

    public void getShakeRotation(@NotNull Player player, float partialTicks, @NotNull Vector3f out) {
        out.set(
                Mth.lerp(partialTicks, lastTickRotation.x(), currentTickRotation.x()),
                Mth.lerp(partialTicks, lastTickRotation.y(), currentTickRotation.y()),
                Mth.lerp(partialTicks, lastTickRotation.z(), currentTickRotation.z())
        );
    }

    public Vector3f getShakeRotation(@NotNull Player player, float partialTicks) {
        var out = new Vector3f();
        getShakeRotation(player, partialTicks, out);
        return out;
    }

    public float getShakeFOV(@NotNull Player player, float partialTicks) {
        return Mth.lerp(partialTicks, lastTickFov, currentTickFov);
    }

    private Vector3f computeOffsetForTick(Player player, float amplitude, float speed, float currentTimeSec) {
        var wave = (float) Math.sin(TAU * speed * currentTimeSec);
        var srcPos = anchor.getPosition(player.level());
        var eye = player.getEyePosition();
        var dirV = eye.subtract(srcPos);
        var len = dirV.length();
        if (len < EPSILON) return new Vector3f();
        dirV = dirV.scale(1.0 / len);
        var offsetX = (float) (dirV.x * amplitude * wave);
        var offsetY = (float) (dirV.y * amplitude * wave);
        var offsetZ = (float) (dirV.z * amplitude * wave);
        return new Vector3f(offsetX, offsetY, offsetZ);
    }

    private Vector3f computeRotationForTick(Player player, float amplitude, float speed, float currentTimeSec) {
        var playerView = player.getLookAngle();
        var pitchFactor = (float) playerView.dot(new Vec3(0, 1, 0));
        var up = new Vec3(0, 1, 0);
        var cross = playerView.cross(up);
        var toSrc = anchor.getPosition(player.level()).subtract(player.position());
        var len = toSrc.length();
        if (len < EPSILON) return new Vector3f();
        var dir = toSrc.scale(1.0 / len);
        var yawFactor = (float) cross.dot(dir);
        var angleX = (float) Math.sin(TAU * speed * frequency.x() * currentTimeSec) * amplitude * pitchFactor;
        var angleY = (float) Math.sin(TAU * speed * frequency.y() * currentTimeSec) * amplitude * yawFactor;
        var angleZ = (float) Math.sin(TAU * speed * frequency.z() * currentTimeSec) * amplitude;
        return new Vector3f(angleX, angleY, angleZ);
    }

    private float computeFovForTick(float amplitude, float speed, float currentTimeSec) {
        return (float) Math.sin(TAU * speed * currentTimeSec) * amplitude;
    }

    public float getCumulativeRotationAmplitude(Player player) {
        return getCumulativeAmplitude(player, rotationAmplitude);
    }

    public float getCumulativeOffsetAmplitude(Player player) {
        return getCumulativeAmplitude(player, offsetAmplitude);
    }

    public float getCumulativeFovAmplitude(Player player) {
        return getCumulativeAmplitude(player, fovAmplitude);
    }

    public float getCumulativeRotationSpeed(Player player) {
        return getCumulativeSpeed(player, rotationSpeed);
    }

    public float getCumulativeOffsetSpeed(Player player) {
        return getCumulativeSpeed(player, offsetSpeed);
    }

    public float getCumulativeFovSpeed(Player player) {
        return getCumulativeSpeed(player, fovSpeed);
    }

    private float getCumulativeAmplitude(Player player, float amplitude) {
        var df = distanceFactor(player);
        if (df <= 0F) return 0F;
        var tf = timeFactorSmooth();
        if (tf <= 0F) return 0F;
        return amplitude * df * tf;
    }

    private float getCumulativeSpeed(Player player, float speed) {
        var df = distanceFactor(player);
        if (df <= 0F) return 0F;
        return speed * df;
    }

    private float distanceFactor(Player player) {
        var r = radius;
        if (r <= 0F) return 0F;
        var src = anchor.getPosition(player.level());
        var distance = (float) player.position().distanceTo(src);
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

    private static Vector3f generateFrequency(UUID id) {
        var seed = id.getMostSignificantBits() ^ id.getLeastSignificantBits();
        var rnd = new SplittableRandom(seed);
        return new Vector3f(
                0.8F + rnd.nextFloat() * 0.4F,
                0.8F + rnd.nextFloat() * 0.4F,
                0.8F + rnd.nextFloat() * 0.4F
        );
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

    public static final StreamCodec<ByteBuf, Shake> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public Shake decode(ByteBuf buffer) {
            var type = ShakeAnchor.AnchorType.values()[buffer.readByte()];

            var anchor = switch (type) {
                case POSITION -> new PositionShakeAnchor(new Vec3(FriendlyByteBuf.readVector3f(buffer)));
                case ENTITY -> new EntityShakeAnchor(buffer.readInt());
            };

            var shake = Shake.builder(anchor).build();

            shake.setRadius(buffer.readFloat());
            shake.setRotationAmplitude(buffer.readFloat());
            shake.setOffsetAmplitude(buffer.readFloat());
            shake.setFovAmplitude(buffer.readFloat());
            shake.setRotationSpeed(buffer.readFloat());
            shake.setOffsetSpeed(buffer.readFloat());
            shake.setFovSpeed(buffer.readFloat());
            shake.setDuration(buffer.readInt());
            shake.setFadeInTime(buffer.readInt());
            shake.setFadeOutTime(buffer.readInt());

            return shake;
        }

        @Override
        public void encode(ByteBuf buffer, Shake shake) {
            var anchor = shake.getAnchor();

            buffer.writeByte(anchor.getType().ordinal());

            switch (anchor.getType()) {
                case POSITION -> FriendlyByteBuf.writeVector3f(buffer, ((PositionShakeAnchor) anchor).getPosition().toVector3f());
                case ENTITY -> buffer.writeInt(((EntityShakeAnchor) anchor).getId());
            }

            buffer.writeFloat(shake.getRadius());
            buffer.writeFloat(shake.getRotationAmplitude());
            buffer.writeFloat(shake.getOffsetAmplitude());
            buffer.writeFloat(shake.getFovAmplitude());
            buffer.writeFloat(shake.getRotationSpeed());
            buffer.writeFloat(shake.getOffsetSpeed());
            buffer.writeFloat(shake.getFovSpeed());
            buffer.writeInt(shake.getDuration());
            buffer.writeInt(shake.getFadeInTime());
            buffer.writeInt(shake.getFadeOutTime());
        }
    };

    public static class ShakeBuilder {
        private final ShakeAnchor anchor;
        private float radius = 1F;
        private float rotationAmplitude = 1F;
        private float offsetAmplitude = 1F;
        private float fovAmplitude = 1F;
        private float rotationSpeed = 5F;
        private float offsetSpeed = 5F;
        private float fovSpeed = 5F;
        private int duration = 20;
        private int fadeInTime = 0;
        private int fadeOutTime = -1;
        private UUID uuid = UUID.randomUUID();

        public ShakeBuilder(ShakeAnchor anchor) {
            this.anchor = anchor;
        }

        public ShakeBuilder radius(float v) {
            this.radius = v;
            return this;
        }

        @Deprecated
        public ShakeBuilder rangeMultiplier(float v) {
            return radius(v);
        }

        public ShakeBuilder amplitude(float v) {
            this.rotationAmplitude = v;
            this.offsetAmplitude = v;
            this.fovAmplitude = v;
            return this;
        }

        public ShakeBuilder amplitude(float rot, float off, float fov) {
            this.rotationAmplitude = rot;
            this.offsetAmplitude = off;
            this.fovAmplitude = fov;
            return this;
        }

        public ShakeBuilder rotationAmplitude(float v) {
            this.rotationAmplitude = v;
            return this;
        }

        public ShakeBuilder offsetAmplitude(float v) {
            this.offsetAmplitude = v;
            return this;
        }

        public ShakeBuilder fovAmplitude(float v) {
            this.fovAmplitude = v;
            return this;
        }

        public ShakeBuilder speed(float v) {
            this.rotationSpeed = v;
            this.offsetSpeed = v;
            this.fovSpeed = v;
            return this;
        }

        public ShakeBuilder speed(float rot, float off, float fov) {
            this.rotationSpeed = rot;
            this.offsetSpeed = off;
            this.fovSpeed = fov;
            return this;
        }

        public ShakeBuilder rotationSpeed(float v) {
            this.rotationSpeed = v;
            return this;
        }

        public ShakeBuilder offsetSpeed(float v) {
            this.offsetSpeed = v;
            return this;
        }

        public ShakeBuilder fovSpeed(float v) {
            this.fovSpeed = v;
            return this;
        }

        public ShakeBuilder duration(int v) {
            this.duration = v;
            return this;
        }

        public ShakeBuilder fadeInTime(int v) {
            this.fadeInTime = v;
            return this;
        }

        public ShakeBuilder fadeOutTime(int v) {
            this.fadeOutTime = v;
            return this;
        }

        public ShakeBuilder uuid(UUID id) {
            this.uuid = Objects.requireNonNull(id, "uuid");
            return this;
        }

        public Shake build() {
            if (radius < 0F) throw new IllegalArgumentException("radius must be >= 0");
            if (duration < 0) throw new IllegalArgumentException("duration must be >= 0");
            if (rotationAmplitude < 0F || offsetAmplitude < 0F || fovAmplitude < 0F)
                throw new IllegalArgumentException("amplitudes must be >= 0");
            if (rotationSpeed < 0F || offsetSpeed < 0F || fovSpeed < 0F)
                throw new IllegalArgumentException("speeds must be >= 0");

            return new Shake(
                    anchor,
                    radius,
                    rotationAmplitude, offsetAmplitude, fovAmplitude,
                    rotationSpeed, offsetSpeed, fovSpeed,
                    duration, fadeInTime, fadeOutTime,
                    uuid
            );
        }
    }
}