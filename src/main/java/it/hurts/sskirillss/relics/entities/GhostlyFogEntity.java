package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.client.particles.GhostlyFogParticle;
import it.hurts.sskirillss.relics.items.relics.back.GhostlyMantleItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostlyFogEntity extends Entity {
    public static final int PARTICLE_MIN_LIFETIME = 55;
    public static final int PARTICLE_RANDOM_LIFETIME_BOUND = 35;

    private static final Map<String, Long> LAST_EXPOSURE_TICK = new HashMap<>();
    private static long lastExposureCleanupTick = Long.MIN_VALUE;

    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(GhostlyFogEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(GhostlyFogEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(GhostlyFogEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> AIR_DRAIN = SynchedEntityData.defineId(GhostlyFogEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SUFFOCATION_DAMAGE = SynchedEntityData.defineId(GhostlyFogEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> TREMOR_TICKS = SynchedEntityData.defineId(GhostlyFogEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID ownerUuid;

    public GhostlyFogEntity(EntityType<? extends GhostlyFogEntity> type, Level level) {
        super(type, level);

        this.noPhysics = true;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUUID();
        this.getEntityData().set(OWNER_ID, owner.getId());
    }

    @Nullable
    public LivingEntity getOwner() {
        var ownerId = this.getEntityData().get(OWNER_ID);
        var entity = ownerId >= 0 ? this.level().getEntity(ownerId) : null;

        if (entity instanceof LivingEntity owner) {
            if (!this.level().isClientSide())
                this.ownerUuid = owner.getUUID();

            return owner;
        }

        if (this.level().isClientSide() || this.ownerUuid == null || !(this.level() instanceof ServerLevel serverLevel))
            return null;

        var uuidEntity = serverLevel.getEntity(this.ownerUuid);

        if (uuidEntity instanceof LivingEntity owner) {
            this.getEntityData().set(OWNER_ID, owner.getId());

            return owner;
        }

        return null;
    }

    public void setLifetime(int lifetime) {
        this.getEntityData().set(LIFETIME, Math.max(1, lifetime));
    }

    public int getLifetime() {
        return this.getEntityData().get(LIFETIME);
    }

    public void setRadius(float radius) {
        this.getEntityData().set(RADIUS, Math.max(0.1F, radius));
    }

    public float getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    public void setAirDrain(int airDrain) {
        this.getEntityData().set(AIR_DRAIN, Math.max(0, airDrain));
    }

    public int getAirDrain() {
        return this.getEntityData().get(AIR_DRAIN);
    }

    public void setSuffocationDamage(float suffocationDamage) {
        this.getEntityData().set(SUFFOCATION_DAMAGE, Math.max(0F, suffocationDamage));
    }

    public float getSuffocationDamage() {
        return this.getEntityData().get(SUFFOCATION_DAMAGE);
    }

    public void setTremorTicks(int tremorTicks) {
        this.getEntityData().set(TREMOR_TICKS, Math.max(0, tremorTicks));
    }

    public int getTremorTicks() {
        return this.getEntityData().get(TREMOR_TICKS);
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(0D, 0D, 0D);

        if (this.level().isClientSide()) {
            if (this.getLifetime() - this.tickCount > 1) {
                var random = this.random;
                var radius = this.getRadius();

                var x = this.getX() + MathUtils.randomFloat(random) * radius;
                var z = this.getZ() + MathUtils.randomFloat(random) * radius;

                this.level().addParticle(new GhostlyFogParticle.Options(this.getLifetime() - this.tickCount - 1),
                        x,
                        this.getY() + random.nextDouble(),
                        z,
                        MathUtils.randomFloat(random) * 0.01F,
                        -0.025F,
                        MathUtils.randomFloat(random) * 0.01F);
            }

            return;
        }

        if (this.tickCount > this.getLifetime()) {
            this.discard();

            return;
        }

        var owner = this.getOwner();
        var radius = this.getRadius();
        var gameTime = this.level().getGameTime();

        if (lastExposureCleanupTick != gameTime && gameTime % 200L == 0L) {
            lastExposureCleanupTick = gameTime;

            LAST_EXPOSURE_TICK.values().removeIf(tick -> tick < gameTime - 200L);
        }

        for (var target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius), entity -> {
            if (entity == owner || !entity.isAlive())
                return false;

            return !(owner instanceof Player player) || !EntityUtils.isAlliedTo(player, entity);
        })) {
            target.getPersistentData().putFloat(GhostlyMantleItem.FOG_SUFFOCATION_DAMAGE_TAG, this.getSuffocationDamage());
            target.getPersistentData().putLong(GhostlyMantleItem.FOG_SUFFOCATION_UNTIL_TAG, this.level().getGameTime() + 5L);

            if (this.getAirDrain() > 0)
                target.setAirSupply(Math.max(-20, target.getAirSupply() - this.getAirDrain()));

            if (this.getTremorTicks() > 0)
                target.addEffect(new MobEffectInstance(RelicsMobEffects.TREMOR, this.getTremorTicks(), 0, false, false));

            if (target.getAirSupply() <= -20 && this.tickCount % 20 == 0)
                target.hurt(this.level().damageSources().inWall(), 1F);

            if (owner == null)
                continue;

            var key = owner.getStringUUID() + ":" + target.getStringUUID();

            if (LAST_EXPOSURE_TICK.getOrDefault(key, Long.MIN_VALUE) == gameTime)
                continue;

            LAST_EXPOSURE_TICK.put(key, gameTime);

            for (var stack : EntityUtils.findEquippedCurios(owner, RelicsItems.GHOSTLY_MANTLE.get())) {
                var relic = (GhostlyMantleItem) stack.getItem();
                var ability = relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("fog");

                if (!ability.canPlayerUse(owner))
                    continue;

                ability.getStatisticData().getMetricData("fog_exposure").addValue(1D / 20D);
                relic.getRelicData(owner, stack).getLevelingData().addExperience("fog", "fog_exposure", 1D / 20D);

                break;
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(LIFETIME, 100);
        builder.define(RADIUS, 1.2F);
        builder.define(AIR_DRAIN, 2);
        builder.define(SUFFOCATION_DAMAGE, 0F);
        builder.define(TREMOR_TICKS, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setLifetime(tag.getInt("lifetime"));
        this.setRadius(tag.getFloat("radius"));
        this.setAirDrain(tag.getInt("airDrain"));
        this.setSuffocationDamage(tag.getFloat("suffocationDamage"));
        this.setTremorTicks(tag.getInt("tremorTicks"));

        if (tag.hasUUID("owner"))
            this.ownerUuid = tag.getUUID("owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("lifetime", this.getLifetime());
        tag.putFloat("radius", this.getRadius());
        tag.putInt("airDrain", this.getAirDrain());
        tag.putFloat("suffocationDamage", this.getSuffocationDamage());
        tag.putInt("tremorTicks", this.getTremorTicks());

        if (this.ownerUuid != null)
            tag.putUUID("owner", this.ownerUuid);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }
}
