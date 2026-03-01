package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class KineticElectricityEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(KineticElectricityEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(KineticElectricityEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PREVIOUS_ENTITY_ID = SynchedEntityData.defineId(KineticElectricityEntity.class, EntityDataSerializers.INT);

    @Getter
    @Setter
    private ItemStack stack = ItemStack.EMPTY;

    public KineticElectricityEntity(EntityType<? extends KineticElectricityEntity> type, Level level) {
        super(type, level);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    public void setPreviousEntityId(int entityId) {
        this.getEntityData().set(PREVIOUS_ENTITY_ID, entityId);
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

        if (this.tickCount > 200)
            this.discard();
    }

    @Nullable
    private ChainSegment resolveChainSegment(Level level) {
        var previousId = this.getPreviousEntityId();

        if (previousId < 0)
            return null;

        Entity previousRaw = level.getEntity(previousId);

        if (!(previousRaw instanceof KineticElectricityEntity previous) || !previous.isAlive())
            return null;

        var start = this.position().add(0, this.getBbHeight() * 0.5F, 0);
        var end = previous.position().add(0, previous.getBbHeight() * 0.5F, 0);
        var distanceSqr = start.distanceToSqr(end);

        if (distanceSqr < 0.01D || distanceSqr > 100D)
            return null;

        return new ChainSegment(start, end);
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

        for (var target : level.getEntitiesOfClass(LivingEntity.class, segmentBox, entity -> entity.isAlive() && entity != owner && !(entity instanceof Player))) {
            if (target.getBoundingBox().inflate(0.1D).clip(start, end).isPresent())
                target.hurt(level.damageSources().thrown(owner instanceof LivingEntity livingOwner ? livingOwner : this, this), damage);
        }
    }

    private void spawnChainParticles(Level level, Vec3 start, Vec3 end) {
        var distance = start.distanceTo(end);
        var segments = Math.max(3, (int) (distance * 3D));
        var previousPoint = start;

        for (var i = 1; i <= segments; i++) {
            var t = (double) i / segments;
            var nextPoint = start.add(end.subtract(start).scale(t));

            if (i < segments) {
                var jitter = 0.25D;

                nextPoint = nextPoint.add((random.nextDouble() * 2D - 1D) * jitter, (random.nextDouble() * 2D - 1D) * jitter, (random.nextDouble() * 2D - 1D) * jitter);
            }

            var color = FlawlessUtils.getColor(this.isFlawless(), new Color(120 + random.nextInt(50), 190 + random.nextInt(50), 255));
            var particle = ParticleUtils.constructSimpleSpark(color, 0.2F + random.nextFloat() * 0.1F, 3, 0.85F);
            var motion = new Vec3((random.nextDouble() * 2D - 1D) * 0.005D, (random.nextDouble() * 2D - 1D) * 0.005D, (random.nextDouble() * 2D - 1D) * 0.005D);
            var amount = Math.max(1, (int) Math.ceil(previousPoint.distanceTo(nextPoint) * 14D));

            ParticleUtils.createLine(particle, level, previousPoint, nextPoint, amount, motion);

            previousPoint = nextPoint;
        }
    }

    private record ChainSegment(Vec3 start, Vec3 end) {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 1F);
        builder.define(FLAWLESS, false);
        builder.define(PREVIOUS_ENTITY_ID, -1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("damage", this.getDamage());
        tag.putBoolean("flawless", this.isFlawless());
        tag.putInt("previousEntityId", this.getPreviousEntityId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setDamage(tag.getFloat("damage"));
        this.setFlawless(tag.getBoolean("flawless"));
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
