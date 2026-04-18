package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.UUID;

public class ChainedElectricityEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ChainedElectricityEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(ChainedElectricityEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(ChainedElectricityEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PREVIOUS_ENTITY_ID = SynchedEntityData.defineId(ChainedElectricityEntity.class, EntityDataSerializers.INT);

    @Getter
    @Setter
    private ItemStack stack = ItemStack.EMPTY;
    @Nullable
    private UUID previousEntityUuid;

    public ChainedElectricityEntity(EntityType<? extends ChainedElectricityEntity> type, Level level) {
        super(type, level);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setLifetime(int lifetime) {
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifetime() {
        return this.getEntityData().get(LIFETIME);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    public void setPreviousEntityId(int entityId) {
        this.getEntityData().set(PREVIOUS_ENTITY_ID, entityId);

        if (entityId < 0) {
            this.previousEntityUuid = null;
            return;
        }

        var previous = this.level().getEntity(entityId);

        if (previous != null)
            this.previousEntityUuid = previous.getUUID();
    }

    public void setPreviousEntity(ChainedElectricityEntity previous) {
        this.getEntityData().set(PREVIOUS_ENTITY_ID, previous.getId());
        this.previousEntityUuid = previous.getUUID();
    }

    public int getPreviousEntityId() {
        return this.getEntityData().get(PREVIOUS_ENTITY_ID);
    }

    @Override
    public void tick() {
        super.tick();

        var level = this.getCommandSenderWorld();
        var segment = this.resolveChainSegment(level);

        if (segment != null) {
            if (level.isClientSide())
                this.spawnChainParticles(level, segment.start(), segment.end());
            else
                this.hurtEntitiesOnChain(level, segment.start(), segment.end());
        }

        if (this.tickCount > this.getLifetime() * 20)
            this.discard();
    }

    @Nullable
    private ChainSegment resolveChainSegment(Level level) {
        var previous = this.resolvePreviousEntity(level);

        if (previous == null)
            return null;

        var start = this.position().add(0, this.getBbHeight() * 0.5F, 0);
        var end = previous.position().add(0, previous.getBbHeight() * 0.5F, 0);
        var distanceSqr = start.distanceToSqr(end);

        if (distanceSqr < 0.01D || distanceSqr > 100D)
            return null;

        return new ChainSegment(start, end);
    }

    @Nullable
    private ChainedElectricityEntity resolvePreviousEntity(Level level) {
        var previousId = this.getPreviousEntityId();

        if (previousId >= 0) {
            var previousRawById = level.getEntity(previousId);

            if (previousRawById instanceof ChainedElectricityEntity previous && previous.isAlive()) {
                if (!level.isClientSide())
                    this.previousEntityUuid = previous.getUUID();

                return previous;
            }
        }

        if (level.isClientSide() || this.previousEntityUuid == null || !(level instanceof ServerLevel serverLevel))
            return null;

        var previousRawByUuid = serverLevel.getEntity(this.previousEntityUuid);

        if (!(previousRawByUuid instanceof ChainedElectricityEntity previous) || !previous.isAlive())
            return null;

        this.getEntityData().set(PREVIOUS_ENTITY_ID, previous.getId());

        return previous;
    }

    private void hurtEntitiesOnChain(Level level, Vec3 start, Vec3 end) {
        var damage = this.getDamage();

        if (damage <= 0F)
            return;

        var radius = 0.35D;

        var owner = this.getOwner();
        var segmentBox = new AABB(
                Math.min(start.x, end.x) - radius,
                Math.min(start.y, end.y) - radius,
                Math.min(start.z, end.z) - radius,
                Math.max(start.x, end.x) + radius,
                Math.max(start.y, end.y) + radius,
                Math.max(start.z, end.z) + radius
        );

        for (var target : level.getEntitiesOfClass(LivingEntity.class, segmentBox, entity -> entity != owner)) {
            if (target.getBoundingBox().inflate(0.1D).clip(start, end).isPresent())
                target.hurt(level.damageSources().thrown(owner instanceof LivingEntity livingOwner ? livingOwner : this, this), damage);
        }
    }

    private void spawnChainParticles(Level level, Vec3 start, Vec3 end) {
        var random = level.random;
        var color = FlawlessUtils.getColor(this.isFlawless(), new Color(120 + random.nextInt(50), 190 + random.nextInt(50), 255));
        var particle = ParticleUtils.constructSimpleSpark(color, 0.2F + random.nextFloat() * 0.1F, 3, 0.85F);

        ParticleUtils.createLightning(level, start, end, 3, 3D, 0.25D, 14D, 0.005D, particle);
    }

    private record ChainSegment(Vec3 start, Vec3 end) {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 1F);
        builder.define(LIFETIME, 1);
        builder.define(FLAWLESS, false);
        builder.define(PREVIOUS_ENTITY_ID, -1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("damage", this.getDamage());
        tag.putInt("lifetime", this.getLifetime());
        tag.putBoolean("flawless", this.isFlawless());
        tag.putInt("previousEntityId", this.getPreviousEntityId());

        if (this.previousEntityUuid != null)
            tag.putUUID("previousEntityUuid", this.previousEntityUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setDamage(tag.getFloat("damage"));
        this.setLifetime(tag.getInt("lifetime"));
        this.setFlawless(tag.getBoolean("flawless"));
        this.previousEntityUuid = tag.hasUUID("previousEntityUuid") ? tag.getUUID("previousEntityUuid") : null;
        this.setPreviousEntityId(tag.getInt("previousEntityId"));
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return 0D;
    }
}
