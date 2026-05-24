package it.hurts.sskirillss.relics.entities.relic.midnight_mantle;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.client.particles.GhostlyFogParticle;
import it.hurts.sskirillss.relics.dev.shake.Shake;
import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import it.hurts.sskirillss.relics.entities.FallingStarShockwaveBlockEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsSounds;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.S2CSpawnParticle;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import it.hurts.sskirillss.relics.utils.WorldUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;

public class FallingStarEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> BOUNCE_CHANCE = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> RADIUS = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> STUN = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(FallingStarEntity.class, EntityDataSerializers.BOOLEAN);

    @Getter
    @Setter
    private ItemStack stack = ItemStack.EMPTY;

    public void setBounceChance(float bounceChance) {
        this.getEntityData().set(BOUNCE_CHANCE, bounceChance);
    }

    public float getBounceChance() {
        return this.getEntityData().get(BOUNCE_CHANCE);
    }

    public void setRadius(int radius) {
        this.getEntityData().set(RADIUS, radius);
    }

    public int getRadius() {
        return this.getEntityData().get(RADIUS);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setStun(int stun) {
        this.getEntityData().set(STUN, stun);
    }

    public int getStun() {
        return this.getEntityData().get(STUN);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    private boolean bounced = false;
    private int bounces = 0;

    public FallingStarEntity(EntityType<? extends FallingStarEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void tick() {
        super.tick();

        var level = this.level();

        if (!level.isClientSide() && tickCount > (250 + this.bounces * 150))
            this.discard();

        this.bounced = false;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        var center = result.getBlockPos();
        var level = this.level();

        ShakeManager.add(level, Shake.builder(this)
                .amplitude(0.075F)
                .duration(5)
                .radius(16)
                .build());

        if (level.isClientSide())
            return;

        var serverLevel = (ServerLevel) level;
        var position = this.position();
        var radius = Math.max(this.getRadius() - this.bounces, 1);

        var ringParticleCount = 250 + random.nextInt(250);

        for (var i = 0; i < ringParticleCount; i++) {
            var angle = 2 * Math.PI * i / ringParticleCount;
            var velocity = new Vec3(Math.cos(angle), 0.15D + random.nextDouble() * 0.1D, Math.sin(angle)).normalize().scale(0.5 + random.nextDouble() * 0.25D);

            var color = this.isFlawless() ? new Color(255, 215, 0) : new Color(50 + random.nextInt(150), 50 + random.nextInt(150), 255);

            NetworkHandler.sendToClientsTrackingEntity(new S2CSpawnParticle(ParticleUtils.constructSimpleSpark(color, 0.75F + random.nextFloat() * 0.25F, 5 + random.nextInt(5), 0.8F), position.toVector3f(), velocity.toVector3f()), this);
        }

        var burstParticleCount = 250 + random.nextInt(100);

        for (var i = 0; i < burstParticleCount; i++) {
            var velocity = new Vec3(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).normalize().scale(0.1 + random.nextDouble() * 0.25);
            var color = this.isFlawless() ? new Color(255, 235, 100) : new Color(100 + random.nextInt(155), 100 + random.nextInt(155), 255);

            NetworkHandler.sendToClientsTrackingEntity(new S2CSpawnParticle(ParticleUtils.constructSimpleSpark(color, 0.25F + random.nextFloat(), 10 + random.nextInt(10), 0.9F), position.toVector3f(), velocity.toVector3f()), this);
        }

        var trailCount = 10;
        var pointCount = 75;
        var maxDistance = 5;

        for (var i = 0; i < trailCount; i++) {
            var direction = new Vec3(random.nextGaussian(), random.nextDouble(), random.nextGaussian()).normalize();

            for (var j = 1; j <= pointCount; j++) {
                var t = j / (double) pointCount;

                var spacedT = Math.pow(t, 1.5);

                var px = position.x + direction.x * spacedT * maxDistance;
                var py = position.y + direction.y * spacedT * maxDistance;
                var pz = position.z + direction.z * spacedT * maxDistance;

                var color = this.isFlawless() ? new Color(255, 200, 50) : new Color(80 + random.nextInt(100), 80 + random.nextInt(100), 255);
                var spark = ParticleUtils.constructSimpleSpark(color, (float) (1F * (1 - t) + 0.1F), 15 + random.nextInt(15), 0.5F);

                NetworkHandler.sendToClientsTrackingEntity(new S2CSpawnParticle(spark, new Vector3f((float) px, (float) py, (float) pz), new Vector3f((float) (direction.x * 0.02F), (float) (direction.y * 0.02F), (float) (direction.z * 0.02F))), this);
            }
        }

        if (this.noPhysics || !level.getBlockState(center).blocksMotion())
            return;

        var poses = new ArrayList<BlockPos>();

        for (var i = -radius; i <= radius; i++) {
            var r1 = (int) Mth.sqrt(radius * radius - i * i);

            for (var j = -r1; j <= r1; j++)
                poses.add(center.offset(i, 0, j));
        }

        for (var step = 0; step <= radius; step++) {
            int finalStep = step;

            ServerScheduler.schedule(finalStep, () -> {
                var height = 0.25F;

                poses.stream().filter(pos -> {
                    var dx = pos.getX() - center.getX();
                    var dz = pos.getZ() - center.getZ();
                    var dist = Math.hypot(dx, dz);

                    return dist >= finalStep && dist < finalStep + 1;
                }).forEach(entryPos -> {
                    var centerY = center.getY();

                    int maxOffset = 8;

                    var groundY = WorldUtils.findSurfaceY(level, entryPos.getX(), entryPos.getZ(), centerY, maxOffset);

                    var minAllowedY = Math.max(level.getMinBuildHeight(), centerY - maxOffset);
                    var maxAllowedY = Math.min(level.getMaxBuildHeight(), centerY + maxOffset);

                    if (groundY < minAllowedY || groundY > maxAllowedY)
                        return;

                    var surfacePos = new BlockPos(entryPos.getX(), groundY, entryPos.getZ());

                    var shockwave = new FallingStarShockwaveBlockEntity(RelicsEntities.SHOCKWAVE_BLOCK.get(), level);

                    shockwave.setPos(surfacePos.getX() + 0.5F, surfacePos.getY(), surfacePos.getZ() + 0.5F);
                    shockwave.setBlockState(level.getBlockState(surfacePos));
                    shockwave.setDeltaMovement(0, height, 0);
                    shockwave.setDamage(this.getDamage());
                    shockwave.setOwner(this.getOwner());
                    shockwave.setStack(this.getStack());
                    shockwave.setStun(this.getStun());
                    shockwave.setCenter(surfacePos);
                    shockwave.setKnockback(0.75F);

                    level.addFreshEntity(shockwave);

                    var px = surfacePos.getX() + 0.15D + random.nextDouble() * 0.7D;
                    var pz = surfacePos.getZ() + 0.15D + random.nextDouble() * 0.7D;

                    var direction = new Vec3(px - (center.getX() + 0.5D), 0D, pz - (center.getZ() + 0.5D));

                    if (direction.lengthSqr() < 0.001D)
                        direction = new Vec3(random.nextDouble() - 0.5D, 0D, random.nextDouble() - 0.5D);

                    direction = direction.normalize();

                    var speed = 0.008D + random.nextDouble() * 0.012D;
                    var lifetime = 10 + random.nextInt(25);
                    var fogPosition = new Vector3f((float) px, surfacePos.getY() + 0.04F, (float) pz);
                    var fogMotion = new Vector3f((float) (direction.x * speed), height / 2F, (float) (direction.z * speed));

                    NetworkHandler.sendToClientsTrackingChunk(new S2CSpawnParticle(new GhostlyFogParticle.Options(lifetime), fogPosition, fogMotion), serverLevel, new ChunkPos(surfacePos));
                });
            });
        }

        level.playSound(null, this.blockPosition(), RelicsSounds.FALLING_STAR_FALL.get(), SoundSource.MASTER, 0.5F, 1F + random.nextFloat() + this.bounces * 0.2F);

        var bounceChance = this.getBounceChance();

        if (bounceChance > 0F) {
            if (this.bounced)
                return;

            if (random.nextFloat() <= bounceChance) {
                var normal = Vec3.atLowerCornerOf(result.getDirection().getNormal()).normalize();

                var motion = this.getDeltaMovement();
                var reflected = motion.subtract(normal.scale(2 * motion.dot(normal))).normalize().scale(0.5F + this.getRadius() * 0.075F);

                this.setDeltaMovement(reflected);

                this.bounced = true;
                this.bounces++;

                var stack = this.getStack();

                if (stack.getItem() instanceof MidnightMantleItem relic && this.getOwner() instanceof LivingEntity owner) {
                    relic.getRelicData(owner, stack).getAbilitiesData().getAbilityData("starfall").getStatisticData().getMetricData("star_bounces").addValue(1);

                    relic.getRelicData(owner, stack).getLevelingData().addExperience("starfall", "star_bounce", 1);
                }
            } else
                this.discard();
        } else
            this.discard();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BOUNCE_CHANCE, 0F);
        builder.define(RADIUS, 1);
        builder.define(DAMAGE, 0F);
        builder.define(STUN, 0);
        builder.define(FLAWLESS, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putFloat("bounce_chance", this.getBounceChance());
        tag.putInt("radius", this.getRadius());
        tag.putFloat("damage", this.getDamage());
        tag.putInt("stun", this.getStun());
        tag.putBoolean("flawless", this.isFlawless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setBounceChance(tag.getFloat("bounce_chance"));
        this.setRadius(tag.getInt("radius"));
        this.setDamage(tag.getFloat("damage"));
        this.setStun(tag.getInt("stun"));
        this.setFlawless(tag.getBoolean("flawless"));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03D;
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<FallingStarEntity> {
        public TrailProvider(FallingStarEntity entity) {
            super(entity);
        }

        @Override
        public Vec3 getTrailPosition(float partialTicks) {
            return this.entity.getPosition(partialTicks).add(0D, this.entity.getBbHeight() / 2D, 0D);
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
            return this.entity.tickCount > 0;
        }

        @Override
        public int getTrailMaxLength() {
            return 10;
        }

        @Override
        public int getTrailFadeInColor() {
            return FlawlessUtils.getColor(entity.isFlawless(), 0xFF0080FF);
        }

        @Override
        public int getTrailFadeOutColor() {
            return FlawlessUtils.getColor(entity.isFlawless(), 0x8000FFFF);
        }

        @Override
        public double getTrailScale() {
            return 0.25F;
        }
    }
}
