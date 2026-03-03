package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class LifeEssenceEntity extends ThrowableProjectile implements ITargetableEntity {
    @Setter
    @Getter
    private float heal;

    private LivingEntity target;

    private static final EntityDataAccessor<Float> DIRECTION_CHOICE = SynchedEntityData.defineId(LifeEssenceEntity.class, EntityDataSerializers.FLOAT);

    public LifeEssenceEntity(EntityType<? extends LifeEssenceEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        super.tick();

        if (target == null)
            return;

        int segments = 10;

        double dx = (this.getX() - xOld) / segments;
        double dy = (this.getY() - yOld) / segments;
        double dz = (this.getZ() - zOld) / segments;

        for (int i = 0; i < segments; i++) {
            level().addParticle((ParticleUtils.constructSimpleSpark(new Color(200, 150 + random.nextInt(50), random.nextInt(50)), 0.25F + (heal * 0.05F), 20 + Math.round(heal * 0.025F), 0.9F)),
                    this.getX() + dx * i, this.getY() + dy * i, this.getZ() + dz * i, -this.getDeltaMovement().x * 0.1 * Math.random(), -this.getDeltaMovement().y * 0.1 * Math.random(), -this.getDeltaMovement().z * 0.1 * Math.random());
        }

        this.moveTowardsTargetInArc(target);

        if (target.isDeadOrDying())
            this.discard();

        if (this.distanceTo(target) <= 1) {
            target.heal(heal);

            this.remove(RemovalReason.KILLED);
        }
    }

    private void moveTowardsTargetInArc(Entity target) {
        Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2F, target.getZ());
        Vec3 direction = targetPos.subtract(this.position()).normalize();

        this.setDeltaMovement(this.position().add(direction.add(new Vec3(getDirectionChoice() * -direction.z, 0, getDirectionChoice() * direction.x)))
                .subtract(this.position()).normalize().scale(this.position().distanceTo(targetPos) * (this.tickCount * 0.005F)));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (target == null || !(result.getEntity() instanceof LivingEntity entity) || entity.getUUID() != target.getUUID())
            return;

        entity.heal(getHeal() + this.getHeal());

        this.discard();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DIRECTION_CHOICE, 0F);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    public float getDirectionChoice() {
        return this.entityData.get(DIRECTION_CHOICE);
    }

    public void setDirectionChoice(float value) {
        this.entityData.set(DIRECTION_CHOICE, value);
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}