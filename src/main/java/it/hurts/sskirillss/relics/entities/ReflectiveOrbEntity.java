package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.necklace.ReflectiveNecklaceItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReflectiveOrbEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> TARGETED = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> TARGET = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PIERCINGS = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> STUN = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> BOUNCES = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(ReflectiveOrbEntity.class, EntityDataSerializers.BOOLEAN);

    @Getter
    private ItemStack stack = ItemStack.EMPTY;

    private boolean bounced = false;
    private boolean takeBounces = false;
    private boolean spawnBounceParticles = false;

    private List<String> impactedEntities = new ArrayList<>();

    public void setTarget(Vec3 target) {
        this.getEntityData().set(TARGET, target.toVector3f());
    }

    public Vec3 getTarget() {
        var target = this.getEntityData().get(TARGET);

        return new Vec3(target.x(), target.y(), target.z());
    }

    public void setLifetime(int lifetime) {
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifetime() {
        return this.getEntityData().get(LIFETIME);
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

    public void setPiercings(int remainingPierces) {
        this.getEntityData().set(PIERCINGS, remainingPierces);
    }

    public int getPiercings() {
        return this.getEntityData().get(PIERCINGS);
    }

    public void setStun(float stun) {
        this.getEntityData().set(STUN, stun);
    }

    public float getStun() {
        return this.getEntityData().get(STUN);
    }

    public void setBounces(int bounces) {
        this.getEntityData().set(BOUNCES, bounces);
    }

    public int getBounces() {
        return this.getEntityData().get(BOUNCES);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    private static final int ARC_DURATION = 10;

    public ReflectiveOrbEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        var cachedMotion = this.getDeltaMovement();

        super.tick();

        this.noPhysics = this.tickCount <= ARC_DURATION;

        var level = this.level();
        var isTargeted = this.isTargeted();
        var target = this.getTarget();
        var position = this.position();
        var semiTarget = this.getOwner() == null ? target : this.getOwner().position();

        var maxDistance = ReflectiveNecklaceItem.ORB_SEARCH_RADIUS;
        var normal = position.subtract(semiTarget).normalize();
        var origin = semiTarget.add(normal.scale(maxDistance));

        if (this.tickCount >= (this.getLifetime() + (isTargeted ? 100 : 0))) {
            this.discard();

            return;
        }

        if (target.equals(Vec3.ZERO)) {
            if (this.tickCount < ARC_DURATION) {
                var damp = 1D - this.tickCount / (double) ARC_DURATION;

                this.setDeltaMovement(new Vec3(cachedMotion.x() * damp, 0.25D + this.random.nextFloat() * 0.25D, cachedMotion.z() * damp));
            } else
                this.setDeltaMovement(this.getDeltaMovement().scale(0.9F));
        } else if (!isTargeted) {
            this.setTargeted(true);

            this.setDeltaMovement(target.subtract(position).normalize().scale(1.5F));
        } else {
            if (!bounced)
                this.setDeltaMovement(cachedMotion);

            if (position.distanceTo(semiTarget) >= maxDistance)
                if (this.bounce(normal, origin))
                    this.spawnBounceParticles = true;
        }

        if (level.isClientSide()) {
            var random = level.getRandom();

            level.addParticle(ParticleUtils.constructSimpleSpark(this.isFlawless() ? new Color(200 + random.nextInt(50), 150 + random.nextInt(50), 0) : new Color(50 + random.nextInt(100), random.nextInt(100), 255), 0.1F + (random.nextFloat() * 0.15F), 15, 0.9F), this.getX(), this.getY() + this.getBbHeight() / 2F, this.getZ(), MathUtils.randomFloat(random) * 0.05F, MathUtils.randomFloat(random) * 0.05F, MathUtils.randomFloat(random) * 0.05F);
        }

        if (this.takeBounces && position.distanceTo(semiTarget) < maxDistance) {
            this.setBounces(this.getBounces() - 1);

            this.takeBounces = false;

            if (this.spawnBounceParticles) {
                var up = Math.abs(normal.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);

                var axis1 = normal.cross(up).normalize();
                var axis2 = normal.cross(axis1).normalize();

                var segments = 6;
                var radius = 1D;

                var directions = new Vec3[segments];

                for (var i = 0; i < segments; i++) {
                    var angle = 2 * Math.PI * i / segments;

                    directions[i] = axis1.scale(Math.cos(angle) * radius).add(axis2.scale(Math.sin(angle) * radius));
                }

                var fillStep = 0.25;
                var shatterSpeed = 0.075F;

                for (var i = 0; i < segments; i++) {
                    var edge1 = directions[i];
                    var edge2 = directions[(i + 1) % segments];

                    for (var t = 0.0; t <= 1.0; t += fillStep) {
                        for (var u = 0.0; u <= 1.0 - t; u += fillStep) {
                            var point = origin.add(edge1.scale(t)).add(edge2.scale(u));
                            var shardDir = point.subtract(origin).normalize();

                            var vx = (float) (shardDir.x * shatterSpeed);
                            var vy = (float) (shardDir.y * shatterSpeed);
                            var vz = (float) (shardDir.z * shatterSpeed);

                            this.level().addParticle(ParticleUtils.constructSimpleSpark(this.isFlawless() ? new Color(200 + random.nextInt(50), 150 + random.nextInt(50), 0) : new Color(50 + random.nextInt(100), random.nextInt(100), 255), 0.35F, 40, 0.9F), true, point.x, point.y, point.z, vx, vy, vz);
                        }
                    }
                }

                this.spawnBounceParticles = false;
            }
        }

        this.bounced = false;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        var pos = result.getBlockPos();
        var level = this.level();

        if (level.isClientSide() || this.noPhysics || bounced || !level.getBlockState(pos).blocksMotion())
            return;

        var faceNormal = Vec3.atLowerCornerOf(result.getDirection().getNormal()).normalize();
        var hitLocation = result.getLocation();

        this.bounce(faceNormal, hitLocation);
    }

    public boolean bounce(Vec3 normal, Vec3 origin) {
        var bounces = this.getBounces();

        if (bounces <= 0) {
            this.discard();

            return false;
        }

        var motion = this.getDeltaMovement();
        var reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));

        var halfWidth = this.getBbWidth() * 0.5D;
        var halfHeight = this.getBbHeight() * 0.5D;

        var eps = 1e-3;

        var pushDist = Math.abs(normal.x) * halfWidth + Math.abs(normal.y) * halfHeight + Math.abs(normal.z) * halfWidth + eps;

        var newPos = origin.add(normal.scale(pushDist));

        this.setPos(newPos.x, newPos.y, newPos.z);
        this.setDeltaMovement(reflected);

        this.bounced = true;
        this.takeBounces = true;

        if (stack.getItem() instanceof ReflectiveNecklaceItem relic && this.getOwner() instanceof LivingEntity owner)
            relic.addAbilityMetricValue(owner, stack, "reflection", "total_bounces", 1);

        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.tickCount < ARC_DURATION || !(result.getEntity() instanceof LivingEntity entity) || this.impactedEntities.contains(entity.getStringUUID())
                || (!(this.getOwner() instanceof LivingEntity owner) || entity.getStringUUID().equals(owner.getStringUUID())))
            return;

        entity.invulnerableTime = 0;

        if (entity.hurt(this.level().damageSources().thrown(owner, this), this.getDamage())) {
            var stun = this.getStun();

            if (stun > 0)
                entity.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, (int) (stun * 20), 0, false, false));

            if (stack.getItem() instanceof ReflectiveNecklaceItem relic) {
                relic.addAbilityMetricValue(entity, stack, "reflection", "total_damage", this.getDamage());

                if (stun > 0)
                    relic.addAbilityMetricValue(entity, stack, "reflection", "total_stun", stun);

                if (relic.canAddRelicExperience(entity, stack, "reflection", "impact"))
                    relic.addRelicExperience(entity, stack, "reflection", "impact", 1);
            }
        }

        this.impactedEntities.add(entity.getStringUUID());

        var remainingPierces = this.getPiercings();

        if (remainingPierces <= 0)
            this.discard();
        else
            this.setPiercings(remainingPierces - 1);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide())
            return false;

        var owner = this.getOwner();
        var attacker = source.getEntity();

        if (owner != null && attacker != null && owner.getStringUUID().equals(attacker.getStringUUID()))
            return false;

        this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1F, 1F);

        this.discard();

        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 1F);
        builder.define(TARGETED, false);
        builder.define(TARGET, Vec3.ZERO.toVector3f());
        builder.define(LIFETIME, 0);
        builder.define(PIERCINGS, 0);
        builder.define(STUN, 0F);
        builder.define(BOUNCES, 0);
        builder.define(FLAWLESS, false);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("damage", this.getDamage());
        tag.putBoolean("targeted", this.isTargeted());
        tag.putInt("lifetime", this.getLifetime());
        tag.putInt("piercings", this.getPiercings());
        tag.putInt("bounces", this.getBounces());
        tag.putFloat("stun", this.getStun());
        tag.putBoolean("flawless", this.isFlawless());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setDamage(tag.getFloat("damage"));
        this.setTargeted(tag.getBoolean("targeted"));
        this.setLifetime(tag.getInt("lifetime"));
        this.setPiercings(tag.getInt("piercings"));
        this.setBounces(tag.getInt("bounces"));
        this.setStun(tag.getFloat("stun"));
        this.setFlawless(tag.getBoolean("flawless"));
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return 0D;
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<ReflectiveOrbEntity> {
        public TrailProvider(ReflectiveOrbEntity entity) {
            super(entity);
        }

        @Override
        public Vec3 getTrailPosition(float partialTicks) {
            return this.entity.getPosition(partialTicks).add(0D, this.entity.getBbHeight() / 2D, 0D);
        }

        @Override
        public int getTrailUpdateFrequency() {
            return 1;
        }

        @Override
        public boolean isTrailAlive() {
            return this.entity.isAlive();
        }

        @Override
        public boolean isTrailGrowing() {
            return this.entity.tickCount > 0;
        }

        @Override
        public int getTrailMaxLength() {
            return 7;
        }

        @Override
        public int getTrailFadeInColor() {
            return entity.isFlawless() ? 0xFFFFFF00 : 0xFF8000FF;
        }

        @Override
        public int getTrailFadeOutColor() {
            return entity.isFlawless() ? 0x00FF0000 : 0x800000FF;
        }

        @Override
        public double getTrailScale() {
            return 0.15F;
        }
    }
}