package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.items.relics.necklace.JellyfishNecklaceItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CSyncEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ElectricSparkEntity extends ThrowableProjectile implements ITargetableEntity {
    private static final EntityDataAccessor<Integer> BOUNCES = SynchedEntityData.defineId(ElectricSparkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ElectricSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DISTANCE = SynchedEntityData.defineId(ElectricSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DAMAGE_MODIFIER = SynchedEntityData.defineId(ElectricSparkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(ElectricSparkEntity.class, EntityDataSerializers.BOOLEAN);

    @Getter
    @Setter
    private ItemStack stack = ItemStack.EMPTY;

    private Set<String> bouncedTargets = new HashSet<>();

    private List<String> blacklistedTargets = new ArrayList<>();

    @Nullable
    private LivingEntity currentTarget = null;
    @Nullable
    private LivingEntity lastTarget = null;

    public ElectricSparkEntity(EntityType<? extends ElectricSparkEntity> type, Level level) {
        super(type, level);
    }

    public void setBounces(int bounces) {
        this.getEntityData().set(BOUNCES, bounces);
    }

    public int getBounces() {
        return this.getEntityData().get(BOUNCES);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setDistance(float distance) {
        this.getEntityData().set(DISTANCE, distance);
    }

    public float getDistance() {
        return this.getEntityData().get(DISTANCE);
    }

    public void setDamageModifier(float damage) {
        this.getEntityData().set(DAMAGE_MODIFIER, damage);
    }

    public float getDamageModifier() {
        return this.getEntityData().get(DAMAGE_MODIFIER);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    public List<LivingEntity> locateNearestTargets() {
        return EntityUtils.gatherPotentialTargets(this, LivingEntity.class, this.getDistance())
                .filter(entity -> (lastTarget == null || !lastTarget.getStringUUID().equals(entity.getStringUUID()))
                        && (!(this.getOwner() instanceof Player player) || !EntityUtils.isAlliedTo(player, entity))
                        && !this.blacklistedTargets.contains(entity.getStringUUID())
                        && !this.bouncedTargets.contains(entity.getStringUUID()))
                .collect(Collectors.toList());
    }

    @Override
    public void tick() {
        super.tick();

        var level = this.getCommandSenderWorld();
        var currentTarget = this.getTarget();
        var distance = this.getDistance();

        if (currentTarget != null) {
            if (this.position().distanceTo(currentTarget.position()) >= distance || currentTarget.isDeadOrDying())
                currentTarget = null;
        } else if (!level.isClientSide()) {
            var selectedTarget = (LivingEntity) null;
            var nearbyTargets = this.locateNearestTargets();

            if (nearbyTargets.isEmpty()) {
                this.bouncedTargets.clear();

                nearbyTargets = this.locateNearestTargets();
            }

            if (!nearbyTargets.isEmpty()) {
                var totalWeight = 0.0;
                var weightList = new ArrayList<Double>();

                for (var targetCandidate : nearbyTargets) {
                    var distanceToCandidate = this.position().distanceTo(targetCandidate.position());
                    var candidateWeight = distance - distanceToCandidate;

                    weightList.add(candidateWeight);

                    totalWeight += candidateWeight;
                }

                var randomPoint = this.random.nextDouble() * totalWeight;
                var cumulativeWeight = 0.0;

                for (var i = 0; i < nearbyTargets.size(); i++) {
                    cumulativeWeight += weightList.get(i);

                    if (randomPoint <= cumulativeWeight) {
                        selectedTarget = nearbyTargets.get(i);

                        break;
                    }
                }
            }

            if (selectedTarget != null) {
                this.setTarget(selectedTarget);

                currentTarget = selectedTarget;
            }
        }

        if (currentTarget == null || currentTarget.isDeadOrDying() || this.tickCount >= 250 || this.getBounces() <= 0) {
            if (!level.isClientSide())
                this.discard();

            return;
        }

        if (this.getEyePosition().distanceTo(currentTarget.getEyePosition()) <= 1.5F) {
            currentTarget.invulnerableTime = 0;

            var damage = this.getDamage();
            var owner = this.getOwner();

            if (currentTarget.hurt(level.damageSources().thrown(this, owner), damage + (currentTarget.isInLiquid() || currentTarget.isInRain() ? damage * this.getDamageModifier() : 0F))) {
                this.bouncedTargets.add(currentTarget.getStringUUID());
                this.lastTarget = currentTarget;

                this.setTarget(null);
                this.setBounces(this.getBounces() - 1);

                if (!level.isClientSide()) {
                    if (stack.getItem() instanceof JellyfishNecklaceItem relic && owner instanceof LivingEntity entity) {
                        relic.addAbilityMetricValue(entity, stack, "shock", "arcs_bounces", 1);

                        relic.addAbilityMetricValue(entity, stack, "shock", "arcs_damage", damage);

                        if (relic.canAddRelicExperience(entity, stack, "shock", "arcs_bouncing"))
                            relic.addRelicExperience(entity, stack, "shock", "arcs_bouncing", 1);
                    }
                }
            } else {
                this.blacklistedTargets.add(currentTarget.getStringUUID());

                this.setTarget(null);
            }
        } else
            this.setDeltaMovement(currentTarget.getEyePosition().subtract(this.getEyePosition()).normalize().scale(1.75F));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BOUNCES, 0);
        builder.define(DAMAGE, 1F);
        builder.define(DISTANCE, 1F);
        builder.define(DAMAGE_MODIFIER, 0F);
        builder.define(FLAWLESS, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("bounces", this.getBounces());
        tag.putFloat("damage", this.getDamage());
        tag.putFloat("distance", this.getDistance());
        tag.putFloat("damage_modifier", this.getDamageModifier());
        tag.putBoolean("flawless", this.isFlawless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setBounces(tag.getInt("bounces"));
        this.setDamage(tag.getFloat("damage"));
        this.setDistance(tag.getFloat("distance"));
        this.setDamageModifier(tag.getFloat("damage_modifier"));
        this.setFlawless(tag.getBoolean("flawless"));
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

    @Override
    public @Nullable LivingEntity getTarget() {
        return currentTarget;
    }

    @Override
    public void setTarget(LivingEntity target) {
        if (target != null && !target.level().isClientSide())
            NetworkHandler.sendToClientsTrackingEntity(new S2CSyncEntityTargetPacket(this.getId(), target.getId()), this);

        this.currentTarget = target;
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<ElectricSparkEntity> {
        public TrailProvider(ElectricSparkEntity entity) {
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
            return this.entity.getKnownMovement().length() >= 0.1F;
        }

        @Override
        public int getTrailMaxLength() {
            return 3;
        }

        @Override
        public int getTrailFadeInColor() {
            return entity.isFlawless() ? 0xFFFFFF00 : 0xFF00FFFF;
        }

        @Override
        public int getTrailFadeOutColor() {
            return entity.isFlawless() ? 0x00FF0000 : 0x800000FF;
        }

        @Override
        public double getTrailScale() {
            return 0.1F;
        }
    }
}