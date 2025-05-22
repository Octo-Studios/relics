package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.modules.particles.OctoRenderManager;
import it.hurts.octostudios.octolib.modules.particles.trail.TrailProvider;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;

public class ReflectiveOrbEntity extends ThrowableProjectile implements TrailProvider {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> TARGETED = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> MOTION = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.INT);

    @Getter
    @Setter
    @Nullable
    private LivingEntity target;

    public void setMotion(Vec3 motion) {
        this.getEntityData().set(MOTION, motion.toVector3f());
    }

    public Vec3 getMotion() {
        var motion = this.getEntityData().get(MOTION);

        return new Vec3(motion.x(), motion.y(), motion.z());
    }

    public void setLifetime(int lifetime) {
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifetime() {
        return this.getEntityData().get(LIFETIME);
    }

    public void setDelay(int delay) {
        this.getEntityData().set(DELAY, delay);
    }

    public int getDelay() {
        return this.getEntityData().get(DELAY);
    }

    public void setTargeted(boolean targeted) {
        this.getEntityData().set(TARGETED, targeted);
    }

    public boolean isTargeted() {
        return this.getEntityData().get(TARGETED);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    private static final int ARC_DURATION = 10;

    public ReflectiveOrbEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        var cachedMotion = this.getDeltaMovement();

        super.tick();

        this.noPhysics = true;

        var level = this.level();
        var isTargeted = this.isTargeted();

        if (tickCount >= (this.getLifetime() + (isTargeted ? 100 : 0))) {
            this.discard();

            return;
        }

        var motion = Vec3.ZERO;

        if (tickCount < ARC_DURATION) {
            var damp = 1D - tickCount / (double) ARC_DURATION;

            motion = new Vec3(cachedMotion.x() * damp, 0.25D, cachedMotion.z() * damp);
        } else if (isTargeted) {
            var delay = this.getDelay();
            var target = this.getTarget();

            if (delay > 0) {
                this.setDelay(--delay);
            } else {
                if (!level.isClientSide() && target != null) {
                    this.setMotion(target.position().add(0D, target.getBbHeight() / 2D, 0D).subtract(this.position()).normalize());

                    this.setTarget(null);
                }

                motion = this.getMotion();
            }
        }

        this.setDeltaMovement(motion);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        var pos = result.getBlockPos();
        var level = this.level();

        if (!level.getBlockState(pos).blocksMotion() || this.tickCount < ARC_DURATION)
            return;

        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(result.getEntity() instanceof LivingEntity entity) || this.tickCount < ARC_DURATION
                || (!(this.getOwner() instanceof LivingEntity owner) || entity.getStringUUID().equals(owner.getStringUUID())))
            return;

        entity.invulnerableTime = 0;

        entity.hurt(this.level().damageSources().mobAttack(owner), this.getDamage());
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        OctoRenderManager.registerProvider(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 1F);
        builder.define(TARGETED, false);
        builder.define(DELAY, 0);
        builder.define(MOTION, Vec3.ZERO.toVector3f());
        builder.define(LIFETIME, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        setDamage(compound.getFloat("damage"));
        setTargeted(compound.getBoolean("targeted"));
        setDelay(compound.getInt("delay"));
        setLifetime(compound.getInt("lifetime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("damage", this.getDamage());
        compound.putBoolean("targeted", this.isTargeted());
        compound.putInt("delay", this.getDelay());
        compound.putInt("lifetime", this.getLifetime());
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return 0D;
    }

    @Override
    public Vec3 getTrailPosition(float partialTicks) {
        return getPosition(partialTicks).add(getDeltaMovement().scale(-1));
    }

    @Override
    public int getTrailUpdateFrequency() {
        return 1;
    }

    @Override
    public boolean isTrailAlive() {
        return isAlive();
    }

    @Override
    public boolean isTrailGrowing() {
        return this.tickCount > 0;
    }

    @Override
    public int getTrailMaxLength() {
        return 5;
    }

    @Override
    public int getTrailFadeInColor() {
        return 0xFF8000FF;
    }

    @Override
    public int getTrailFadeOutColor() {
        return 0x800000FF;
    }

    @Override
    public double getTrailScale() {
        return 0.075F;
    }
}