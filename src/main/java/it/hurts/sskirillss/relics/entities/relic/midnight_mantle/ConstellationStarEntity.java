package it.hurts.sskirillss.relics.entities.relic.midnight_mantle;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.midnight_mantle.S2CSyncConstellation;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ConstellationStarEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Vector3f> CENTER = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TREMOR = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> STUN = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> CONSTELLATION = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> STUCK = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.BOOLEAN);

    public void setCenter(Vec3 center) {
        this.getEntityData().set(CENTER, center.toVector3f());
    }

    public Vec3 getCenter() {
        return new Vec3(this.getEntityData().get(CENTER));
    }

    public void setLifetime(int lifetime) {
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifetime() {
        return this.getEntityData().get(LIFETIME);
    }

    public void setTremor(float tremor) {
        this.getEntityData().set(TREMOR, tremor);
    }

    public float getTremor() {
        return this.getEntityData().get(TREMOR);
    }

    public void setStun(float stun) {
        this.getEntityData().set(STUN, stun);
    }

    public float getStun() {
        return this.getEntityData().get(STUN);
    }

    public void setRadius(float radius) {
        this.getEntityData().set(RADIUS, radius);
    }

    public float getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setRawConstellation(String constellation) {
        this.getEntityData().set(CONSTELLATION, constellation);
    }

    public void setConstellation(List<UUID> constellation) {
        this.setRawConstellation(constellation.stream().map(UUID::toString).collect(Collectors.joining("+")));
    }

    public String getRawConstellation() {
        return this.getEntityData().get(CONSTELLATION);
    }

    public List<UUID> getConstellation() {
        return Arrays.stream(this.getRawConstellation().split("\\+")).map(String::trim).map(UUID::fromString).toList();
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isStuck() {
        return this.getEntityData().get(STUCK);
    }

    public void setStuck(boolean stuck) {
        this.getEntityData().set(STUCK, stuck);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    @Getter
    @Setter
    private List<Integer> clientConstellation = new ArrayList<>();

    public ConstellationStarEntity(EntityType<? extends ConstellationStarEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isStuck())
            this.setDeltaMovement(Vec3.ZERO);

        var level = this.level();

        if (this.isStuck()) {
            if (!level.isClientSide()) {
                if (!level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox(), entity -> this.getOwner() == null || !this.getOwner().getStringUUID().equals(entity.getStringUUID())).isEmpty())
                    this.discard();

                if (this.tickCount % 5 == 0 && this.tickCount >= this.getLifetime() * 20 && random.nextInt(20) == 0)
                    this.discard();
            }

            if (level.isClientSide()) {
                var color = isFlawless() ? new Color(200 + random.nextInt(50), 150 + random.nextInt(50), 0) : new Color(50 + random.nextInt(50), 50 + random.nextInt(150), 255);
                var data = ParticleUtils.constructSimpleSpark(color, 0.2F + random.nextFloat() * 0.1F, 30 + random.nextInt(20), 0.95F);

                level.addParticle(data, this.getX() + MathUtils.randomFloat(random) * this.getBbWidth() / 2F, this.getY(), this.getZ() + MathUtils.randomFloat(random) * this.getBbWidth() / 2F, 0, 0.1F + random.nextFloat() * 0.1F, 0);
            }

            var originEntityId = this.getId();

            var entityIdQueue = new ArrayDeque<Integer>();
            var discoveredIds = new HashSet<Integer>();

            entityIdQueue.add(originEntityId);
            discoveredIds.add(originEntityId);

            if (level.isClientSide()) {
                while (!entityIdQueue.isEmpty()) {
                    var currentEntityId = entityIdQueue.removeFirst();

                    if (!(level.getEntity(currentEntityId) instanceof ConstellationStarEntity foundEntity))
                        continue;

                    for (var neighborStarId : foundEntity.getClientConstellation())
                        if (discoveredIds.add(neighborStarId))
                            entityIdQueue.add(neighborStarId);
                }
            } else {
                var serverLevel = (ServerLevel) level;

                while (!entityIdQueue.isEmpty()) {
                    var currentEntityId = entityIdQueue.removeFirst();

                    if (!(serverLevel.getEntity(currentEntityId) instanceof ConstellationStarEntity foundEntity))
                        continue;

                    for (var neighborStarUuid : foundEntity.getConstellation()) {
                        var entity = serverLevel.getEntity(neighborStarUuid);
                        if (entity == null)
                            continue;

                        var id = entity.getId();

                        if (discoveredIds.add(id))
                            entityIdQueue.add(id);
                    }
                }
            }

            if (discoveredIds.size() < 2)
                return;

            var sortedIds = new ArrayList<>(discoveredIds);

            Collections.sort(sortedIds);

            var originIndex = sortedIds.indexOf(originEntityId);

            var totalStars = sortedIds.size();
            var starEntities = new ConstellationStarEntity[totalStars];
            var starCenters = new Vec3[totalStars];
            var midYs = new double[totalStars];

            for (var i = 0; i < totalStars; i++) {
                var e = (ConstellationStarEntity) level.getEntity(sortedIds.get(i));

                starEntities[i] = e;

                if (e != null) {
                    var c = e.getBoundingBox().getCenter();

                    starCenters[i] = c;
                    midYs[i] = c.y;
                }
            }

            var edges = new ArrayList<int[]>();
            var exists = new boolean[totalStars][totalStars];

            for (var i = 0; i < totalStars; i++) {
                var a = starCenters[i];

                if (a == null || !starEntities[i].isStuck())
                    continue;

                var neighbors = new ArrayList<EdgeCandidate>();

                for (var j = 0; j < totalStars; j++) {
                    if (i == j)
                        continue;

                    var b = starCenters[j];

                    if (b == null || !starEntities[j].isStuck())
                        continue;

                    var dx = a.x - b.x;
                    var dy = a.y - b.y;
                    var dz = a.z - b.z;

                    neighbors.add(new EdgeCandidate(i, j, dx * dx + dy * dy + dz * dz));
                }

                neighbors.sort(Comparator.comparingDouble(e -> e.weightSquared));

                var connections = 0;

                for (var candidate : neighbors) {
                    if (connections >= 2)
                        break;

                    var u = candidate.indexU;
                    var v = candidate.indexV;

                    if (exists[u][v])
                        continue;

                    edges.add(new int[]{u, v});
                    exists[u][v] = exists[v][u] = true;

                    connections++;
                }
            }

            for (var edge : edges) {
                if (edge[0] != originIndex)
                    continue;

                var a = starCenters[edge[0]];
                var b = starCenters[edge[1]];

                if (a == null || b == null)
                    continue;

                var dir = b.subtract(a);
                var length = dir.length();
                var dirNorm = dir.scale(1 / length);

                var proximityThreshold = 0.5;

                var segmentBox = new AABB(
                        Math.min(a.x(), b.x()) - proximityThreshold,
                        Math.min(a.y(), b.y()) - proximityThreshold,
                        Math.min(a.z(), b.z()) - proximityThreshold,
                        Math.max(a.x(), b.x()) + proximityThreshold,
                        Math.max(a.y(), b.y()) + proximityThreshold,
                        Math.max(a.z(), b.z()) + proximityThreshold
                );

                var crossed = false;

                for (var ent : level.getEntitiesOfClass(LivingEntity.class, segmentBox, ent -> ent != this.getOwner())) {
                    if (ent.getBoundingBox().clip(a, b).isPresent()) {
                        crossed = true;

                        ent.addEffect(new MobEffectInstance(RelicsMobEffects.TREMOR, (int) (this.getTremor() * 20), 0));
                    }
                }

                if (level.isClientSide()) {
                    var particleSpacing = 0.15;

                    var up = new Vec3(0, 1, 0);
                    var u = dirNorm.cross(up);

                    if (u.length() < 1e-4)
                        u = dirNorm.cross(new Vec3(1, 0, 0));

                    u = u.normalize();

                    var v = dirNorm.cross(u).normalize();

                    var stepCount = Math.max(1, (int) Math.ceil(length / particleSpacing));

                    var amplitude = 0.05D;
                    var wavelength = 2D;
                    var speed = 0.05D;

                    var time = (level.getGameTime() + this.getId()) * speed;

                    for (var step = 0; step <= stepCount; step++) {
                        var f = step / (double) stepCount;
                        var basePos = a.add(dir.scale(f));
                        var phase = (f * length) / wavelength * 2 * Math.PI + time;
                        var offset = u.scale(Math.cos(phase) * amplitude).add(v.scale(Math.sin(phase) * amplitude));
                        var pos = basePos.add(offset);

                        if (crossed) {
                            var color = new Color(255, random.nextInt(50), 0);
                            var data = ParticleUtils.constructSimpleSpark(color, 0.25F, 5, 0.9F);

                            var motion = 0.05F;

                            level.addParticle(data, true, pos.x, pos.y, pos.z, MathUtils.randomFloat(random) * motion, MathUtils.randomFloat(random) * motion, MathUtils.randomFloat(random) * motion);
                        } else {
                            var color = isFlawless() ? new Color(200 + random.nextInt(50), 150 + random.nextInt(50), 0) : new Color(50 + random.nextInt(50), 50 + random.nextInt(150), 255);
                            var data = ParticleUtils.constructSimpleSpark(color, 0.25F, 0, 1F);

                            level.addParticle(data, true, pos.x, pos.y, pos.z, 0, 0, 0);
                        }
                    }
                }
            }
        }
    }

    @AllArgsConstructor
    private static class EdgeCandidate {
        final int indexU, indexV;
        final double weightSquared;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level().isClientSide() || this.noPhysics)
            return;

        var vec = result.getLocation();
        var face = result.getDirection();

        var pushBack = -0.1;

        var x = vec.x() - face.getStepX() * pushBack;
        var y = vec.y() - face.getStepY() * pushBack;
        var z = vec.z() - face.getStepZ() * pushBack;

        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.setPos(x, y, z);
        this.setStuck(true);
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
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();

        var level = this.level();
        var random = level.getRandom();
        var position = this.position();

        for (var target : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(this.getRadius()), entity -> this.getOwner() == null || !this.getOwner().getStringUUID().equals(entity.getStringUUID()))) {
            target.invulnerableTime = 0;

            if (target.hurt(this.level().damageSources().thrown(this.getOwner() instanceof LivingEntity owner ? owner : this, this), 1 + this.getDamage()))
                target.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, (int) (this.getStun() * 20), 0));
        }

        var ringParticleCount = 100 + random.nextInt(50);

        for (var i = 0; i < ringParticleCount; i++) {
            var angle = 2 * Math.PI * i / ringParticleCount;
            var velocity = new Vec3(Math.cos(angle), 0.15D + random.nextDouble() * 0.1D, Math.sin(angle)).normalize().scale(0.05D + random.nextDouble() * 0.05D);

            var color = this.isFlawless() ? new Color(255, 215, 0) : new Color(50 + random.nextInt(150), 50 + random.nextInt(150), 255);

            level.addParticle(ParticleUtils.constructSimpleSpark(color, 0.75F + random.nextFloat() * 0.25F, 50 + random.nextInt(25), 0.95F), true, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        }

        var burstParticleCount = 250 + random.nextInt(100);

        for (var i = 0; i < burstParticleCount; i++) {
            var velocity = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(0.1 + random.nextDouble() * 0.1);
            var color = this.isFlawless() ? new Color(255, 235, 100) : new Color(100 + random.nextInt(155), 100 + random.nextInt(155), 255);

            var spark = ParticleUtils.constructSimpleSpark(color, 0.25F + random.nextFloat(), 20 + random.nextInt(10), 0.9F);

            level.addParticle(spark, true, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
        }

        var trailCount = 10;
        var pointCount = 25;
        var maxDistance = 2.5;

        for (var i = 0; i < trailCount; i++) {
            var direction = new Vec3(random.nextGaussian(), random.nextDouble(), random.nextGaussian()).normalize();

            for (var j = 1; j <= pointCount; j++) {
                var t = j / (double) pointCount;

                var spacedT = Math.pow(t, 1.5);

                var px = position.x + direction.x * spacedT * maxDistance;
                var py = position.y + direction.y * spacedT * maxDistance;
                var pz = position.z + direction.z * spacedT * maxDistance;

                var color = this.isFlawless() ? new Color(255, 200, 50) : new Color(80 + random.nextInt(100), 80 + random.nextInt(100), 255);
                var spark = ParticleUtils.constructSimpleSpark(color, (float) (1F * (1 - t) + 0.1F), 15 + random.nextInt(15), 0.5F);

                level.addParticle(spark, true, px, py, pz, direction.x * 0.02, direction.y * 0.02, direction.z * 0.02);
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);

        if (!(serverPlayer.level() instanceof ServerLevel level))
            return;

        if (!this.getRawConstellation().isEmpty())
            NetworkHandler.sendToClient(new S2CSyncConstellation(this.getId(), this.getConstellation().stream().map(level::getEntity).filter(Objects::nonNull).map(Entity::getId).collect(Collectors.toList())), serverPlayer);
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CENTER, Vec3.ZERO.toVector3f());
        builder.define(LIFETIME, 0);
        builder.define(CONSTELLATION, "");
        builder.define(STUCK, false);
        builder.define(FLAWLESS, false);
        builder.define(TREMOR, 0F);
        builder.define(STUN, 0F);
        builder.define(RADIUS, 0F);
        builder.define(DAMAGE, 0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("lifetime", this.getLifetime());
        tag.putString("constellation", this.getRawConstellation());
        tag.putBoolean("stuck", this.isStuck());
        tag.putBoolean("flawless", this.isFlawless());
        tag.putFloat("tremor", this.getTremor());
        tag.putFloat("stun", this.getStun());
        tag.putFloat("radius", this.getRadius());
        tag.putFloat("damage", this.getDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setLifetime(tag.getInt("lifetime"));
        this.setRawConstellation(tag.getString("constellation"));
        this.setStuck(tag.getBoolean("stuck"));
        this.setFlawless(tag.getBoolean("flawless"));
        this.setTremor(tag.getFloat("tremor"));
        this.setStun(tag.getFloat("stun"));
        this.setRadius(tag.getFloat("radius"));
        this.setDamage(tag.getFloat("damage"));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03D;
    }
}