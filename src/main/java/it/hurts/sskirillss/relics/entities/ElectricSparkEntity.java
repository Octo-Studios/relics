package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.items.relics.necklace.JellyfishNecklaceItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CSyncEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.awt.Color;

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

        if (level.isClientSide())
            this.spawnArcParticles(level, currentTarget);

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
                        relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("shock").getStatisticData().getMetricData("arcs_bounces").addValue(1);

                        relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("shock").getStatisticData().getMetricData("arcs_damage").addValue(damage);

                        relic.getRelicData(entity, stack).getLevelingData().addExperience("shock", "arcs_bouncing", 1);
                    }
                }
            } else {
                this.blacklistedTargets.add(currentTarget.getStringUUID());

                this.setTarget(null);
            }
        } else {
            var targetEye = currentTarget.getEyePosition();

            this.setPos(targetEye.x, targetEye.y - this.getBbHeight() * 0.5F, targetEye.z);
            this.setDeltaMovement(0D, 0D, 0D);
        }
    }

    private void spawnArcParticles(Level level, LivingEntity target) {
        var random = level.random;
        var start = this.position().add(0, this.getBbHeight() * 0.5F, 0);
        var end = target.position().add(0, target.getBbHeight() * 0.5F, 0);
        var color = FlawlessUtils.getColor(this.isFlawless(), new Color(120 + random.nextInt(50), 190 + random.nextInt(50), 255));
        var particle = ParticleUtils.constructSimpleSpark(color, 0.18F + random.nextFloat() * 0.08F, 3, 0.85F);

        ParticleUtils.createLightning(level, start, end, 2, 2.5D, 0.2D, 10D, 0.005D, particle);
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
}
