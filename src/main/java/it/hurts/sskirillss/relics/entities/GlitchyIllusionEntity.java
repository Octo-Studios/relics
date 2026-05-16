package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.back.GlitchyMantleItem;
import it.hurts.sskirillss.relics.items.relics.necklace.JellyfishNecklaceItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.S2CSpawnParticle;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.UUID;

public class GlitchyIllusionEntity extends Entity {
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STUN_DURATION = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> STUN_RADIUS = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_FLAWLESS = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ECHO_ATTACK_TICKS = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> ECHO_ATTACK_ITEM = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> ECHO_HEAD_PITCH = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> ELECTRIC_CHAIN_ACTIVE = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ELECTRIC_CHAIN_FLAWLESS = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> SNAPSHOT_X_ROT = SynchedEntityData.defineId(GlitchyIllusionEntity.class, EntityDataSerializers.FLOAT);

    private static final int ECHO_ATTACK_DURATION = 6;

    @Nullable
    private UUID ownerUuid;

    public GlitchyIllusionEntity(EntityType<? extends GlitchyIllusionEntity> type, Level level) {
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
        this.getEntityData().set(LIFETIME, lifetime);
    }

    public int getLifetime() {
        return this.getEntityData().get(LIFETIME);
    }

    public void setStunDuration(int duration) {
        this.getEntityData().set(STUN_DURATION, duration);
    }

    public int getStunDuration() {
        return this.getEntityData().get(STUN_DURATION);
    }

    public void setStunRadius(float radius) {
        this.getEntityData().set(STUN_RADIUS, Math.max(0F, radius));
    }

    public float getStunRadius() {
        return this.getEntityData().get(STUN_RADIUS);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(IS_FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(IS_FLAWLESS);
    }

    public void startEchoAttack(LivingEntity target, ItemStack stack) {
        var dx = target.getX() - this.getX();
        var dz = target.getZ() - this.getZ();
        var rotation = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90F;
        var dy = target.getEyeY() - this.getEyeY();
        var horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        var headPitch = (float) -(Mth.atan2(dy, horizontalDistance) * (180F / Math.PI));

        this.setYRot(rotation);
        this.setYHeadRot(rotation);
        this.setXRot(headPitch);
        this.getEntityData().set(ECHO_ATTACK_TICKS, ECHO_ATTACK_DURATION);
        this.getEntityData().set(ECHO_ATTACK_ITEM, stack.copy());
        this.getEntityData().set(ECHO_HEAD_PITCH, headPitch);
    }

    public int getEchoAttackTicks() {
        return this.getEntityData().get(ECHO_ATTACK_TICKS);
    }

    public ItemStack getEchoAttackItem() {
        return this.getEntityData().get(ECHO_ATTACK_ITEM);
    }

    public float getEchoHeadPitch() {
        return this.getEntityData().get(ECHO_HEAD_PITCH);
    }

    public float getEchoAttackProgress(float partialTicks) {
        var ticks = this.getEchoAttackTicks();

        if (ticks <= 0)
            return 0F;

        return Mth.clamp((ECHO_ATTACK_DURATION - ticks + partialTicks) / ECHO_ATTACK_DURATION, 0F, 1F);
    }

    public void setSnapshotXRot(float xRot) {
        this.getEntityData().set(SNAPSHOT_X_ROT, xRot);
    }

    public float getSnapshotXRot() {
        return this.getEntityData().get(SNAPSHOT_X_ROT);
    }

    private void setElectricChainState(boolean active, boolean flawless) {
        this.getEntityData().set(ELECTRIC_CHAIN_ACTIVE, active);
        this.getEntityData().set(ELECTRIC_CHAIN_FLAWLESS, flawless);
    }

    private boolean isElectricChainActive() {
        return this.getEntityData().get(ELECTRIC_CHAIN_ACTIVE);
    }

    private boolean isElectricChainFlawless() {
        return this.getEntityData().get(ELECTRIC_CHAIN_FLAWLESS);
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(0D, 0D, 0D);

        if (!this.level().isClientSide() && this.getEchoAttackTicks() > 0) {
            this.getEntityData().set(ECHO_ATTACK_TICKS, this.getEchoAttackTicks() - 1);

            if (this.getEchoAttackTicks() <= 0)
                this.getEntityData().set(ECHO_ATTACK_ITEM, ItemStack.EMPTY);
        }

        if (this.tickCount > this.getLifetime()) {
            if (!this.level().isClientSide())
                this.spawnGlitchDissolve();

            this.discard();

            return;
        }

        this.tickElectricSynergy();

        if (this.level().isClientSide())
            return;

        var owner = this.getOwner();

        if (this.checkOwnerProjectileHits(owner))
            return;

        var hitTargets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox(), entity -> this.canAffectTarget(entity, owner));

        if (hitTargets.isEmpty())
            return;

        this.detonate(owner, false);
    }

    private void tickElectricSynergy() {
        var owner = this.getOwner();

        if (owner == null)
            return;

        ElectricSynergy synergy;

        if (this.level().isClientSide()) {
            if (!this.isElectricChainActive())
                return;

            synergy = new ElectricSynergy(0F, this.isElectricChainFlawless());
        } else {
            synergy = resolveElectricSynergy(owner);

            this.setElectricChainState(synergy != null, synergy != null && synergy.flawless());
        }

        if (synergy == null)
            return;

        var illusions = this.level().getEntitiesOfClass(GlitchyIllusionEntity.class, owner.getBoundingBox().inflate(96D),
                        illusion -> illusion.isAlive() && illusion.getOwner() == owner)
                .stream()
                .sorted(Comparator.comparingInt(GlitchyIllusionEntity::getId))
                .toList();

        if (illusions.size() < 2 || illusions.getFirst() != this)
            return;

        var damagedTargets = new HashSet<UUID>();

        for (var i = 1; i < illusions.size(); i++) {
            var previous = illusions.get(i - 1);
            var current = illusions.get(i);
            var start = previous.position().add(0D, previous.getBbHeight() * 0.55D, 0D);
            var end = current.position().add(0D, current.getBbHeight() * 0.55D, 0D);

            if (this.level().isClientSide())
                this.spawnElectricChainParticles(start, end, synergy.flawless());
            else
                this.hurtElectricChainTargets(owner, start, end, synergy.damage(), damagedTargets);
        }
    }

    @Nullable
    private static ElectricSynergy resolveElectricSynergy(LivingEntity owner) {
        var hasShock = EntityUtils.findEquippedCurios(owner, RelicsItems.JELLYFISH_NECKLACE.get()).stream()
                .filter(stack -> stack.getItem() instanceof JellyfishNecklaceItem relic
                        && relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("shock").canPlayerUse(owner))
                .anyMatch(stack -> {
                    var relic = (JellyfishNecklaceItem) stack.getItem();
                    return relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("shock").getMode().equals("enabled");
                });

        if (!hasShock)
            return null;

        var damage = 0D;
        var flawless = false;

        for (var stack : EntityUtils.findEquippedCurios(owner, RelicsItems.GLITCHY_MANTLE.get())) {
            if (!(stack.getItem() instanceof GlitchyMantleItem relic))
                continue;

            var relicData = relic.getRelicData(owner, stack);
            var illusion = relicData.getAbilitiesData().getAbilityData("illusion");
            var synergy = relicData.getAbilitiesData().getSynergyData("electricity");

            if (!illusion.canPlayerUse(owner) || !illusion.getMode().equals("enabled") || !synergy.isUnlocked() || !synergy.getMode().equals("enabled"))
                continue;

            damage = Math.max(damage, synergy.getStatData("damage").getValue());
            flawless |= relicData.isVisuallyFlawless();
        }

        return damage > 0D ? new ElectricSynergy((float) damage, flawless) : null;
    }

    private void hurtElectricChainTargets(LivingEntity owner, Vec3 start, Vec3 end, float damage, HashSet<UUID> damagedTargets) {
        var radius = 0.35D;
        var segmentBox = new AABB(
                Math.min(start.x, end.x) - radius,
                Math.min(start.y, end.y) - radius,
                Math.min(start.z, end.z) - radius,
                Math.max(start.x, end.x) + radius,
                Math.max(start.y, end.y) + radius,
                Math.max(start.z, end.z) + radius
        );

        for (var target : this.level().getEntitiesOfClass(LivingEntity.class, segmentBox,
                entity -> entity != owner && entity.isAlive())) {
            if (!target.getBoundingBox().inflate(0.1D).clip(start, end).isPresent() || !damagedTargets.add(target.getUUID()))
                continue;

            target.hurt(this.level().damageSources().thrown(this, owner), damage);
        }
    }

    private void spawnElectricChainParticles(Vec3 start, Vec3 end, boolean flawless) {
        var random = this.random;
        var color = FlawlessUtils.getColor(flawless, new Color(20 + random.nextInt(50), 220 + random.nextInt(35), 80 + random.nextInt(90)));
        var particle = ParticleUtils.constructSimpleSpark(color, 0.18F + random.nextFloat() * 0.08F, 3, 0.86F);

        ParticleUtils.createLightning(this.level(), start, end, 3, 3.5D, 0.2D, 12D, 0.004D, particle);
    }

    private void detonate(@Nullable LivingEntity owner, boolean byOwner) {
        if (this.level().isClientSide())
            return;

        var radius = this.getStunRadius();
        var radiusSqr = radius * radius;
        var stunnedTargets = 0;

        for (var target : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius), entity -> this.canAffectTarget(entity, owner))) {
            if (target.distanceToSqr(this) <= radiusSqr) {
                target.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, this.getStunDuration(), 0, false, false));
                target.addEffect(new MobEffectInstance(RelicsMobEffects.GLITCH, this.getStunDuration(), 0, false, false));

                stunnedTargets++;
            }
        }

        this.addDetonationExperience(owner, byOwner, stunnedTargets);
        this.spawnGlitchBurst();
        this.discard();
    }

    private void addDetonationExperience(@Nullable LivingEntity owner, boolean byOwner, int stunnedTargets) {
        if (owner == null)
            return;

        for (var stack : EntityUtils.findEquippedCurios(owner, RelicsItems.GLITCHY_MANTLE.get())) {
            if (!(stack.getItem() instanceof GlitchyMantleItem relic))
                continue;

            var relicData = relic.getRelicData(owner, stack);
            var ability = relicData.getAbilitiesData().getAbilityData("illusion");

            if (!ability.canPlayerUse(owner) || !ability.getMode().equals("enabled"))
                continue;

            relicData.getLevelingData().addExperience("illusion", "detonation", 1);
            ability.getStatisticData().getMetricData(byOwner ? "owner_detonations" : "target_detonations").addValue(1);
            ability.getStatisticData().getMetricData("stun_duration").addValue(stunnedTargets * (this.getStunDuration() / 20D));

            return;
        }
    }

    private boolean canAffectTarget(LivingEntity entity, @Nullable LivingEntity owner) {
        return entity != owner && entity.isAlive();
    }

    private boolean checkOwnerProjectileHits(@Nullable LivingEntity owner) {
        if (owner == null)
            return false;

        var bounds = this.getBoundingBox().inflate(0.12D);

        for (var projectile : this.level().getEntitiesOfClass(Projectile.class, bounds.inflate(1D),
                projectile -> projectile.isAlive() && projectile.getOwner() == owner)) {
            var start = new Vec3(projectile.xOld, projectile.yOld, projectile.zOld);
            var end = projectile.position();

            if (bounds.intersects(projectile.getBoundingBox()) || bounds.clip(start, end).isPresent()) {
                this.detonate(owner, true);

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide())
            return false;

        var owner = this.getOwner();
        var attacker = source.getEntity();
        var direct = source.getDirectEntity();

        if (direct == this || owner == null || attacker == null || !owner.getUUID().equals(attacker.getUUID()))
            return false;

        this.detonate(owner, true);

        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    public HumanoidArm getMainArm() {
        var owner = this.getOwner();

        return owner != null ? owner.getMainArm() : HumanoidArm.RIGHT;
    }

    private void spawnGlitchDissolve() {
        var random = this.random;
        var bounds = this.getBoundingBox();
        var particleCount = 22 + random.nextInt(14);

        for (var i = 0; i < particleCount; i++) {
            var px = bounds.minX + random.nextDouble() * bounds.getXsize();
            var py = bounds.minY + random.nextDouble() * bounds.getYsize();
            var pz = bounds.minZ + random.nextDouble() * bounds.getZsize();
            var velocity = new Vector3f(
                    (random.nextFloat() - 0.5F) * 0.035F,
                    (random.nextFloat() - 0.15F) * 0.045F,
                    (random.nextFloat() - 0.5F) * 0.035F
            );
            var spark = ParticleUtils.constructSimpleSpark(this.randomGlitchColor(), 0.08F + random.nextFloat() * 0.16F, 8 + random.nextInt(8), 0.7F);

            NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CSpawnParticle(spark, new Vector3f((float) px, (float) py, (float) pz), velocity), this);
        }
    }

    private void spawnGlitchBurst() {
        var position = this.position().add(0D, this.getBbHeight() * 0.55D, 0D);
        var random = this.random;

        var ringParticleCount = 90 + random.nextInt(60);

        for (var i = 0; i < ringParticleCount; i++) {
            var angle = 2D * Math.PI * i / ringParticleCount;
            var velocity = new Vec3(Math.cos(angle), 0.05D + random.nextDouble() * 0.15D, Math.sin(angle)).normalize().scale(0.25D + random.nextDouble() * 0.35D);
            var spark = ParticleUtils.constructSimpleSpark(this.randomGlitchColor(), 0.35F + random.nextFloat() * 0.3F, 8 + random.nextInt(7), 0.78F);

            NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CSpawnParticle(spark, position.toVector3f(), velocity.toVector3f()), this);
        }

        var burstParticleCount = 120 + random.nextInt(80);

        for (var i = 0; i < burstParticleCount; i++) {
            var velocity = new Vec3(random.nextGaussian(), random.nextGaussian() * 0.75D, random.nextGaussian()).normalize().scale(0.08D + random.nextDouble() * 0.3D);
            var spark = ParticleUtils.constructSimpleSpark(this.randomGlitchColor(), 0.15F + random.nextFloat() * 0.45F, 12 + random.nextInt(12), 0.86F);

            NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CSpawnParticle(spark, position.toVector3f(), velocity.toVector3f()), this);
        }

        var traceCount = 7 + random.nextInt(5);
        var pointCount = 28 + random.nextInt(18);

        for (var i = 0; i < traceCount; i++) {
            var direction = new Vec3(random.nextGaussian(), random.nextDouble() * 0.6D - 0.15D, random.nextGaussian()).normalize();
            var maxDistance = 1.75D + random.nextDouble() * 2.25D;

            for (var j = 1; j <= pointCount; j++) {
                var t = j / (double) pointCount;
                var step = Math.pow(t, 1.35D) * maxDistance;
                var glitchOffset = (random.nextDouble() - 0.5D) * 0.12D;
                var px = position.x + direction.x * step + glitchOffset;
                var py = position.y + direction.y * step + (random.nextDouble() - 0.5D) * 0.08D;
                var pz = position.z + direction.z * step - glitchOffset;
                var spark = ParticleUtils.constructSimpleSpark(this.randomGlitchColor(), (float) (0.45D * (1D - t) + 0.08D), 10 + random.nextInt(12), 0.72F);

                NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CSpawnParticle(spark, new Vector3f((float) px, (float) py, (float) pz),
                        new Vector3f((float) (direction.x * 0.015D), (float) (direction.y * 0.015D), (float) (direction.z * 0.015D))), this);
            }
        }

        var columnCount = 18 + random.nextInt(12);

        for (var i = 0; i < columnCount; i++) {
            var px = position.x + (random.nextDouble() - 0.5D) * 1.2D;
            var pz = position.z + (random.nextDouble() - 0.5D) * 1.2D;
            var py = position.y - 0.8D + random.nextDouble() * 1.6D;
            var velocity = new Vector3f(0F, 0.08F + random.nextFloat() * 0.18F, 0F);
            var spark = ParticleUtils.constructSimpleSpark(this.randomGlitchColor(), 0.25F + random.nextFloat() * 0.25F, 6 + random.nextInt(8), 0.6F);

            NetworkHandler.sendToClientsTrackingEntityAndSelf(new S2CSpawnParticle(spark, new Vector3f((float) px, (float) py, (float) pz), velocity), this);
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.65F, 1.45F + random.nextFloat() * 0.35F);
    }

    private Color randomGlitchColor() {
        var random = this.random;
        var variant = random.nextInt(4);

        var color = switch (variant) {
            case 0 -> new Color(40 + random.nextInt(60), 255, 80 + random.nextInt(80));
            case 1 -> new Color(0, 200 + random.nextInt(55), 120 + random.nextInt(100));
            case 2 -> new Color(120 + random.nextInt(80), 255, 40 + random.nextInt(80));
            default -> new Color(10 + random.nextInt(30), 255, 200 + random.nextInt(55));
        };

        return FlawlessUtils.getColor(this.isFlawless(), color);
    }

    private record ElectricSynergy(float damage, boolean flawless) {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_ID, -1);
        builder.define(LIFETIME, 100);
        builder.define(STUN_DURATION, 20);
        builder.define(STUN_RADIUS, 3F);
        builder.define(IS_FLAWLESS, false);
        builder.define(ECHO_ATTACK_TICKS, 0);
        builder.define(ECHO_ATTACK_ITEM, ItemStack.EMPTY);
        builder.define(ECHO_HEAD_PITCH, 0F);
        builder.define(ELECTRIC_CHAIN_ACTIVE, false);
        builder.define(ELECTRIC_CHAIN_FLAWLESS, false);
        builder.define(SNAPSHOT_X_ROT, 0F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.setLifetime(tag.getInt("lifetime"));
        this.setStunDuration(tag.getInt("stunDuration"));
        this.setStunRadius(tag.getFloat("stunRadius"));
        this.setFlawless(tag.getBoolean("flawless"));
        this.setSnapshotXRot(tag.getFloat("snapshotXRot"));

        if (tag.hasUUID("owner"))
            this.ownerUuid = tag.getUUID("owner");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("lifetime", this.getLifetime());
        tag.putInt("stunDuration", this.getStunDuration());
        tag.putFloat("stunRadius", this.getStunRadius());
        tag.putBoolean("flawless", this.isFlawless());
        tag.putFloat("snapshotXRot", this.getSnapshotXRot());

        if (this.ownerUuid != null)
            tag.putUUID("owner", this.ownerUuid);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }
}
