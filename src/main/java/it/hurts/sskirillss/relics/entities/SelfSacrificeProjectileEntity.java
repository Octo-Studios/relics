package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.items.relics.SphereOfSelfSacrifice;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CSyncEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.TargetingUtils;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class SelfSacrificeProjectileEntity extends ThrowableProjectile implements ITargetableEntity {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(SelfSacrificeProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(SelfSacrificeProjectileEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private LivingEntity target;

    @Getter
    @Setter
    private ItemStack stack = ItemStack.EMPTY;

    public SelfSacrificeProjectileEntity(EntityType<? extends SelfSacrificeProjectileEntity> type, Level level) {
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

    @Override
    public void tick() {
        super.tick();

        var level = this.getCommandSenderWorld();
        var target = this.getTarget();

        if (target == null || target.isDeadOrDying() || this.tickCount >= 200 || this.position().distanceTo(target.position()) > 64) {
            if (!level.isClientSide())
                this.discard();

            return;
        }

        if (target.getEyePosition().distanceTo(this.position()) <= 1F) {
            var source = level.damageSources().thrown(this, this.getOwner());
            var damage = this.getDamage();

            target.invulnerableTime = 0;

            TargetingUtils.hurtEnemy(target, source, damage, this.getStack(), "sacrifice");

            if (this.getStack().getItem() instanceof SphereOfSelfSacrifice relic && this.getOwner() instanceof LivingEntity owner)
                relic.getRelicData(owner, this.getStack()).getAbilitiesData().getAbilityData("sacrifice").getStatisticData().getMetricData("blood_projectile_damage").addValue(damage);

            if (!level.isClientSide())
                this.discard();
        } else {
            var motion = this.getDeltaMovement();
            var toTarget = target.getEyePosition().subtract(this.getEyePosition());

            var speed = 0.75F;

            var distance = (float) toTarget.length();

            var minTurn = 0.025F;
            var maxTurn = 0.35F;
            var maxDistance = 24F;

            var t = 1F - Math.min(distance / maxDistance, 1F);
            var turn = minTurn + (maxTurn - minTurn) * t;

            var desired = toTarget.normalize().scale(speed);
            var newMotion = motion.add(desired.subtract(motion).scale(turn));

            var len = newMotion.length();

            if (len > 0.0001F)
                newMotion = newMotion.scale(speed / (float) len);
            else
                newMotion = desired;

            this.setDeltaMovement(newMotion);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 1F);
        builder.define(FLAWLESS, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("damage", this.getDamage());
        tag.putBoolean("flawless", this.isFlawless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setDamage(tag.getFloat("damage"));
        this.setFlawless(tag.getBoolean("flawless"));
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return 0D;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(LivingEntity target) {
        this.target = target;

        if (!level().isClientSide() && target != null)
            NetworkHandler.sendToClientsTrackingEntity(new S2CSyncEntityTargetPacket(this.getId(), target.getId()), this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<SelfSacrificeProjectileEntity> {
        public TrailProvider(SelfSacrificeProjectileEntity entity) {
            super(entity);
        }

        @Override
        public Vec3 getTrailPosition(float partialTicks) {
            return this.entity.getPosition(partialTicks).add(this.entity.getDeltaMovement().scale(-1));
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
            return this.entity.getKnownMovement().length() >= 0.05F;
        }

        @Override
        public int getTrailMaxLength() {
            return 5;
        }

        @Override
        public int getTrailFadeInColor() {
            return FlawlessUtils.getColor(entity.isFlawless(), 0xFFFF0000);
        }

        @Override
        public int getTrailFadeOutColor() {
            return FlawlessUtils.getColor(entity.isFlawless(), 0x60AA0000);
        }

        @Override
        public double getTrailScale() {
            return 0.08F;
        }
    }
}
