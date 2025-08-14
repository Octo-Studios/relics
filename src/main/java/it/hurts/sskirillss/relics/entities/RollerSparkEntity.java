package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.items.relics.feet.RollerSkateItem;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class RollerSparkEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(RollerSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> IGNITE = SynchedEntityData.defineId(RollerSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(RollerSparkEntity.class, EntityDataSerializers.BOOLEAN);

    @Getter
    @Setter
    private ItemStack stack = ItemStack.EMPTY;

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setIgnite(float ignite) {
        this.getEntityData().set(IGNITE, ignite);
    }

    public float getIgnite() {
        return this.getEntityData().get(IGNITE);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    private boolean bounced = false;

    public RollerSparkEntity(EntityType<? extends RollerSparkEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        var motion = this.getDeltaMovement();

        super.tick();

        if (this.tickCount > 100 || motion.length() < 0.1F)
            this.discard();

        this.bounced = false;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        var pos = result.getBlockPos();
        var level = this.level();

        if (level.isClientSide() || this.noPhysics || bounced || !level.getBlockState(pos).blocksMotion())
            return;

        var normal = Vec3.atLowerCornerOf(result.getDirection().getNormal()).normalize();

        var restitution = 0.7D;

        var motion = this.getDeltaMovement();
        var reflected = motion.subtract(normal.scale(2 * motion.dot(normal))).scale(restitution);

        this.setDeltaMovement(reflected);

        this.bounced = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(result.getEntity() instanceof LivingEntity entity) || (!(this.getOwner() instanceof LivingEntity owner) || entity.getStringUUID().equals(owner.getStringUUID())))
            return;

        entity.invulnerableTime = 0;

        var damage = this.getDamage();

        if (entity.hurt(this.level().damageSources().thrown(owner, this), this.getDamage())) {
            var ignite = this.getIgnite();
            var toApply = (int) (ignite * 20);
            var current = entity.getRemainingFireTicks();
            var diff = toApply - current;

            if (ignite > 0)
                entity.setRemainingFireTicks(Math.max(toApply, current));

            this.discard();

            if (stack.getItem() instanceof RollerSkateItem relic) {
                relic.addAbilityMetricValue(owner, stack, "skating", "damage_dealt", damage);

                relic.addRelicExperience(owner, stack, "skating", "spark_hit", 1);

                if (diff > 0)
                    relic.addAbilityMetricValue(owner, stack, "skating", "ignite_duration", diff / 20F);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 0F);
        builder.define(IGNITE, 0F);
        builder.define(FLAWLESS, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("damage", this.getDamage());
        tag.putFloat("ignite", this.getIgnite());
        tag.putBoolean("flawless", this.isFlawless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setDamage(tag.getFloat("damage"));
        this.setIgnite(tag.getFloat("ignite"));
        this.setFlawless(tag.getBoolean("flawless"));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03D;
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<RollerSparkEntity> {
        public TrailProvider(RollerSparkEntity entity) {
            super(entity);
        }

        @Override
        public Vec3 getTrailPosition(float partialTicks) {
            return this.entity.getPosition(partialTicks).add(0F, 0.35F, 0F);
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
            return this.entity.tickCount > 1;
        }

        @Override
        public int getTrailMaxLength() {
            return 3;
        }

        @Override
        public int getTrailFadeInColor() {
            return 0xFFFFFF00;
        }

        @Override
        public int getTrailFadeOutColor() {
            return entity.isFlawless() ? 0x00FF0000 : 0x80FF0000;
        }

        @Override
        public double getTrailScale() {
            return 0.01F;
        }
    }
}