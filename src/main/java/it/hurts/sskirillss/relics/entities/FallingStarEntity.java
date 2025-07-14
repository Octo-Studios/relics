package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.S2CSpawnParticle;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.Scheduler;
import it.hurts.sskirillss.relics.utils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class FallingStarEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Vector3f> MOTION = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STUN = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.BOOLEAN);

    public void setMotion(Vec3 motion) {
        this.getEntityData().set(MOTION, motion.toVector3f());
    }

    public Vec3 getMotion() {
        return new Vec3(this.getEntityData().get(MOTION));
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setStun(float stun) {
        this.getEntityData().set(STUN, stun);
    }

    public float getStun() {
        return this.getEntityData().get(STUN);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    public FallingStarEntity(EntityType<? extends FallingStarEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        super.tick();
        var ringInterval = 2;
        var cycleTicks = 20;
        var cyclePos = (tickCount % cycleTicks) / (float) cycleTicks;

        var prevX = xOld;
        var prevY = yOld + getBbHeight() / 2F;
        var prevZ = zOld;
        var currX = getX();
        var currY = getY() + getBbHeight() / 2F;
        var currZ = getZ();

        var dx = currX - prevX;
        var dy = currY - prevY;
        var dz = currZ - prevZ;
        var distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        var step = 0.1;
        var segments = (int) Math.ceil(distance / step);

        var swirlRadius = 0.01F;
        var level = level();

        var blueStart = new Color(50, 100, 255);
        var blueEnd = new Color(150, 200, 255);

        for (var i = 0; i <= segments; i++) {
            var t = segments > 0 ? (float) i / segments : 0F;
            var bx = prevX + dx * t;
            var by = prevY + dy * t;
            var bz = prevZ + dz * t;

            var vx = (float) (Math.cos(t * Math.PI * 2) * swirlRadius);
            var vz = (float) (Math.sin(t * Math.PI * 2) * swirlRadius);
            var vy = swirlRadius;

            var r = (int) (blueStart.getRed() * (1 - t) + blueEnd.getRed() * t);
            var g = (int) (blueStart.getGreen() * (1 - t) + blueEnd.getGreen() * t);
            var b = (int) (blueStart.getBlue() * (1 - t) + blueEnd.getBlue() * t);
            var color = new Color(r, g, b);

            level.addParticle(
                    ParticleUtils.constructSimpleSpark(color, 0.5F, 30, 0.925F), true,
                    bx, by, bz, vx, vy, vz
            );
        }

        if (tickCount % ringInterval == 0) {
            var motion = getDeltaMovement();
            if (motion.lengthSqr() > 1e-6) {
                var dir = motion.normalize();
                var v1 = dir.cross(new Vec3(0, 1, 0)).normalize();
                if (v1.lengthSqr() < 1e-6) v1 = dir.cross(new Vec3(1, 0, 0)).normalize();
                var v2 = dir.cross(v1).normalize();

                var ringCount = 50;
                var ringRadius = 0.25F;
                var radialSpeed = 0.05F;
                var tangentialSpeed = 0.02F;
                var upwardSpeed = 0.001F;

                var purpleStart = new Color(128, 0, 255);
                var purpleEnd = new Color(255, 128, 255);

                for (var j = 0; j < ringCount; j++) {
                    var a = 2 * Math.PI * j / ringCount;
                    var offset = v1.scale((float) Math.cos(a) * ringRadius)
                            .add(v2.scale((float) Math.sin(a) * ringRadius));
                    var px = currX + offset.x;
                    var py = currY + offset.y;
                    var pz = currZ + offset.z;

                    var radial = offset.normalize();
                    var tangential = radial.cross(dir).normalize();
                    var vel = radial.scale(radialSpeed)
                            .add(tangential.scale(tangentialSpeed))
                            .add(new Vec3(0, upwardSpeed, 0));

                    var tRing = j / (float) (ringCount - 1);
                    var rr = (int) (purpleStart.getRed() * (1 - tRing) + purpleEnd.getRed() * tRing);
                    var rg = (int) (purpleStart.getGreen() * (1 - tRing) + purpleEnd.getGreen() * tRing);
                    var rb = (int) (purpleStart.getBlue() * (1 - tRing) + purpleEnd.getBlue() * tRing);
                    var ringColor = new Color(rr, rg, rb);

                    level.addParticle(
                            ParticleUtils.constructSimpleSpark(ringColor, 0.5F, 20, 0.9F), true,
                            px, py, pz,
                            (float) vel.x, (float) vel.y, (float) vel.z
                    );
                }
            }
        }

        if (tickCount > 100) discard();
        setDeltaMovement(getMotion());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        var center = result.getBlockPos();
        var level = this.level();

        if (level.isClientSide() || this.noPhysics || !level.getBlockState(center).blocksMotion())
            return;

        var radius = 5;

        var poses = new ArrayList<BlockPos>();

        for (var i = -radius; i <= radius; i++) {
            var r1 = (int) Mth.sqrt(radius * radius - i * i);

            for (var j = -r1; j <= r1; j++)
                poses.add(center.offset(i, 0, j));
        }

        for (var step = 0; step <= radius; step++) {
            int finalStep = step;

            Scheduler.schedule(finalStep, () -> {
                var height = 0.25F;

                var localRandom = new Random();

                poses.stream().filter(pos -> {
                    var dx = pos.getX() - center.getX();
                    var dz = pos.getZ() - center.getZ();
                    var dist = Math.hypot(dx, dz);

                    return dist >= finalStep && dist < finalStep + 1;
                }).forEach(entryPos -> {
                    var centerY = center.getY();

                    int maxOffset = 8;

                    var groundY = WorldUtils.findSurfaceY(level, entryPos.getX(), entryPos.getZ(), centerY, maxOffset);
                    ;

                    var minAllowedY = Math.max(level.getMinBuildHeight(), centerY - maxOffset);
                    var maxAllowedY = Math.min(level.getMaxBuildHeight(), centerY + maxOffset);

                    if (groundY < minAllowedY || groundY > maxAllowedY)
                        return;

                    var surfacePos = new BlockPos(entryPos.getX(), groundY, entryPos.getZ());

                    var shockwave = new ShockwaveBlockEntity(RelicsEntities.SHOCKWAVE_BLOCK.get(), level);

                    shockwave.setPos(surfacePos.getX() + 0.5F, surfacePos.getY(), surfacePos.getZ() + 0.5F);
                    shockwave.setBlockState(level.getBlockState(surfacePos));
                    shockwave.setStun((int) (this.getStun() * 20));
                    shockwave.setDeltaMovement(0, height, 0);
                    shockwave.setDamage(this.getDamage());
                    shockwave.setOwner(this.getOwner());
                    shockwave.setCenter(surfacePos);
                    shockwave.setKnockback(0.75F);

                    level.addFreshEntity(shockwave);

                    var angle = localRandom.nextFloat() * Math.PI * 2;

                    var rad = (finalStep + 0.5F + (localRandom.nextFloat() - 0.5F) * 0.3F);

                    var px = (float) (surfacePos.getX() + Math.cos(angle) * rad + 0.5F);
                    var py = surfacePos.getY() + 1.5F + localRandom.nextFloat() * 0.2F;
                    var pz = (float) (surfacePos.getZ() + Math.sin(angle) * rad + 0.5F);

                    var vx = (float) Math.cos(angle) * 0.2F;
                    var vy = 0.025F + localRandom.nextFloat() * 0.05F;
                    var vz = (float) Math.sin(angle) * 0.2F;

                    NetworkHandler.sendToClientsTrackingEntity(new S2CSpawnParticle(ParticleTypes.CLOUD, new Vector3f(px, py, pz), new Vector3f(vx, vy, vz)), shockwave);
                });
            });
        }

        this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(MOTION, Vec3.ZERO.toVector3f());
        builder.define(DAMAGE, 0F);
        builder.define(STUN, 0F);
        builder.define(FLAWLESS, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("damage", this.getDamage());
        tag.putFloat("stun", this.getStun());
        tag.putBoolean("flawless", this.isFlawless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setDamage(tag.getFloat("damage"));
        this.setStun(tag.getFloat("stun"));
        this.setFlawless(tag.getBoolean("flawless"));
    }

    @Override
    protected double getDefaultGravity() {
        return 0D;
    }
}