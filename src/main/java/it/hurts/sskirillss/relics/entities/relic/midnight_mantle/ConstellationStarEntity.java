package it.hurts.sskirillss.relics.entities.relic.midnight_mantle;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.midnight_mantle.S2CSyncConstellation;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
    private static final EntityDataAccessor<Boolean> MASTER = SynchedEntityData.defineId(ConstellationStarEntity.class, EntityDataSerializers.BOOLEAN);
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

    public boolean isMaster() {
        return this.getEntityData().get(MASTER);
    }

    public void setMaster(boolean master) {
        this.getEntityData().set(MASTER, master);
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

        if (!this.isMaster())
            return;

        var level = this.level();

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

        var edgeCandidates = new ArrayList<EdgeCandidate>();

        for (int i = 0; i < totalStars; i++) {
            var eu = starEntities[i];
            var a = starCenters[i];

            if (eu == null || a == null || !eu.isStuck())
                continue;

            for (int j = i + 1; j < totalStars; j++) {
                var ev = starEntities[j];
                var b = starCenters[j];

                if (ev == null || b == null || !ev.isStuck())
                    continue;

                var from = eu.position().add(0F, eu.getBbHeight() / 2F, 0F);
                var to = ev.position().add(0F, ev.getBbHeight() / 2F, 0F);

                var ctx = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ev);

                if (level.clip(ctx).getType() != HitResult.Type.MISS)
                    continue;

                double dx = a.x - b.x;
                double dy = a.y - b.y;
                double dz = a.z - b.z;

                edgeCandidates.add(new EdgeCandidate(i, j, dx * dx + dy * dy + dz * dz));
            }
        }

        edgeCandidates.sort(Comparator.comparingDouble(e -> e.weightSquared));

        var planarEdges = new ArrayList<int[]>();
        var exists = new boolean[totalStars][totalStars];

        for (var candidate : edgeCandidates) {
            var u = candidate.indexU;
            var v = candidate.indexV;

            if (exists[u][v])
                continue;

            var a = starCenters[u];
            var b = starCenters[v];

            if (a == null || b == null)
                continue;

            var midY = (midYs[u] + midYs[v]) * 0.5;
            var valid = true;

            for (var edge : planarEdges) {
                var x = edge[0];
                var y = edge[1];

                if (Math.abs(midY - (midYs[x] + midYs[y]) * 0.5) <= 1.0) {
                    if (GEOM.intersectsXZ(a, b, starCenters[x], starCenters[y])) {
                        valid = false;

                        break;
                    }
                }
            }

            if (!valid)
                continue;

            planarEdges.add(new int[]{u, v});
            exists[u][v] = exists[v][u] = true;
        }

        for (var edge : planarEdges) {
            var a = starCenters[edge[0]];
            var b = starCenters[edge[1]];

            if (a == null || b == null)
                continue;

            if (level.isClientSide()) {
                var particleSpacing = 0.15;

                var dir = b.subtract(a);
                var stepCount = Math.max(1, (int) Math.ceil(dir.length() / particleSpacing));

                for (var step = 0; step <= stepCount; step++) {
                    var f = step / (double) stepCount;
                    var pos = a.add(dir.scale(f));
                    var color = this.isFlawless() ? new Color(200 + random.nextInt(50), 150 + random.nextInt(50), 0) : new Color(50 + random.nextInt(50), 50 + random.nextInt(150), 255);
                    var data = ParticleUtils.constructSimpleSpark(color, 0.25F, 0, 1F);

                    level.addParticle(data, true, pos.x, pos.y, pos.z, 0, 0, 0);
                }
            } else {
                var proximityThreshold = 0.5;

                var segmentBox = new AABB(
                        Math.min(a.x, b.x) - proximityThreshold,
                        Math.min(a.y, b.y) - proximityThreshold,
                        Math.min(a.z, b.z) - proximityThreshold,
                        Math.max(a.x, b.x) + proximityThreshold,
                        Math.max(a.y, b.y) + proximityThreshold,
                        Math.max(a.z, b.z) + proximityThreshold
                );

                for (var ent : level.getEntitiesOfClass(LivingEntity.class, segmentBox)) {
                    var AB = b.subtract(a);
                    var AM = ent.position().subtract(a);
                    var t = AM.dot(AB) / AB.lengthSqr();

                    if (t < 0 || t > 1)
                        continue;

                    var proj = a.add(AB.scale(t));

                    if (ent.getBoundingBox().contains(proj))
                        ent.addEffect(new MobEffectInstance(RelicsMobEffects.TREMOR, (int) (this.getTremor() * 20), 0));
                }
            }
        }
    }

    @AllArgsConstructor
    private static class EdgeCandidate {
        final int indexU, indexV;
        final double weightSquared;
    }

    private static class GeometryHelper {
        double orient(double ax, double az, double bx, double bz, double cx, double cz) {
            return (bx - ax) * (cz - az) - (bz - az) * (cx - ax);
        }

        boolean intersectsXZ(Vec3 A, Vec3 B, Vec3 C, Vec3 D) {
            var o1 = orient(A.x, A.z, B.x, B.z, C.x, C.z);
            var o2 = orient(A.x, A.z, B.x, B.z, D.x, D.z);

            if (o1 * o2 >= 0)
                return false;

            var o3 = orient(C.x, C.z, D.x, D.z, A.x, A.z);
            var o4 = orient(C.x, C.z, D.x, D.z, B.x, B.z);

            return o3 * o4 < 0;
        }
    }

    private static final GeometryHelper GEOM = new GeometryHelper();

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
        builder.define(MASTER, false);
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
        tag.putBoolean("master", this.isMaster());
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
        this.setMaster(tag.getBoolean("master"));
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