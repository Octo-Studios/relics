package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.PacketPlayerMotion;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DissectionEntity extends Entity {
    private static final EntityDataAccessor<Integer> LIFE_TIME = SynchedEntityData.defineId(DissectionEntity.class, EntityDataSerializers.INT);

    @Getter
    @Setter
    private boolean isMaster = false;

    @Getter
    @Setter
    private int maxLifeTime;

    public int getLifeTime() {
        return this.getEntityData().get(LIFE_TIME);
    }

    public void setLifeTime(int amount) {
        this.getEntityData().set(LIFE_TIME, amount);
    }

    public List<UUID> entities = new ArrayList<>();
    public List<UUID> blacklist = new ArrayList<>();

    public boolean locked = true;
    public UUID pair;

    public DissectionEntity(EntityType<?> pEntityType, Level level) {
        super(pEntityType, level);

        this.noPhysics = true;
    }

    public DissectionEntity(Level level) {
        super(EntityRegistry.DISSECTION.get(), level);

        this.noPhysics = true;
    }

    @Nullable
    public DissectionEntity getPair() {
        if (pair == null || level().isClientSide())
            return null;

        return ((ServerLevel) level()).getEntity(pair) instanceof DissectionEntity dissection ? dissection : null;
    }

    public void setPair(Entity entity) {
        if (!(entity instanceof DissectionEntity))
            return;

        this.pair = entity.getUUID();
    }

    @Override
    public void tick() {
        super.tick();

        RandomSource random = level().getRandom();

        clearFire();

        if (this.tickCount > 5) {
            for (int i = 0; i < 5; i++) {
                float step = Math.max(Math.min(getLifeTime() > 20 ? (tickCount - 5) * 0.075F : getLifeTime() * 0.075F, 1F), 0F);
                float mul = random.nextFloat() * 0.3F;

                Vec3 pos = this.position().add(this.getLookAngle()).add(0, this.getBbHeight() / 2, 0)
                        .add(MathUtils.randomFloat(random) * step,
                                MathUtils.randomFloat(random) * step,
                                MathUtils.randomFloat(random) * step);
                Vec3 angle = this.getLookAngle().normalize().multiply(mul, mul, mul);

                level().addParticle(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(100), 100, 0),
                                0.2F + random.nextFloat() * 0.1F, 10 + random.nextInt(20), 0.9F),
                        pos.x(), pos.y(), pos.z(), angle.x(), angle.y(), angle.z());
            }
        }

        if (level().isClientSide())
            return;

        if (this.tickCount > 1200) {
            this.discard();

            return;
        }

        locked = false;

        ServerLevel serverLevel = (ServerLevel) level();

        DissectionEntity pair = getPair();

        if (pair == null) {
            if (!this.isMaster() || getMaxLifeTime() == 0
                    || this.getLifeTime() != this.getMaxLifeTime())
                this.discard();

            return;
        }

        if (isMaster()) {
            int time = getLifeTime();

            if (time > 0) {
                time--;

                setLifeTime(time);

                pair.setLifeTime(time);
            } else {
                this.discard();
            }
        }

        this.lookAt(EntityAnchorArgument.Anchor.FEET, pair.position());

        Vec3 currentVec = this.position().add(this.getLookAngle()).add(0, 1.25, 0);
        Vec3 nextVec = pair.position().add(pair.getLookAngle()).add(0, 1.25, 0);

        int distance = (int) Math.round(currentVec.distanceTo(nextVec));

        Vec3 finalVec = currentVec.add(nextVec.subtract(currentVec).normalize().multiply(distance, distance, distance));

        distance = (int) Math.round(currentVec.distanceTo(finalVec));

        for (int j = 0; j < distance; j++) {
            float x = (float) (((finalVec.x - currentVec.x) * j / distance) + currentVec.x);
            float y = (float) (((finalVec.y - currentVec.y) * j / distance) + currentVec.y);
            float z = (float) (((finalVec.z - currentVec.z) * j / distance) + currentVec.z);

            BlockPos.betweenClosedStream(new AABB(new BlockPos((int) x, (int) y, (int) z))).forEach(pos -> {
                if (serverLevel.getBlockState(pos).blocksMotion()) {
                    locked = true;

                    serverLevel.sendParticles(ParticleUtils.constructSimpleSpark(new Color(255, random.nextInt(50), 0), 0.1F, 10, 0.9F),
                            pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 1, 0.3F, 0.3F, 0.3F, 0.025F);
                }
            });
        }

        if (locked) {
            entities.clear();

            return;
        }

        List<UUID> toRemoveFromEntities = new ArrayList<>();

        for (UUID uuid : entities) {
            Entity target = serverLevel.getEntity(uuid);

            if (!(target instanceof LivingEntity)) {
                toRemoveFromEntities.add(uuid);

                continue;
            }

            if (blacklist.contains(uuid))
                continue;

            if (target.position().distanceTo(this.position()) > this.position().distanceTo(pair.position())) {
                toRemoveFromEntities.add(uuid);

                continue;
            }

            if (!serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()).contains(target)) {
                double mul = this.position().distanceTo(pair.position()) * 0.05F;

                Vec3 motion = this.position().add(0, this.getBbHeight() / 2, 0).subtract(target.position()).normalize().multiply(mul, mul, mul);

                if (target instanceof ServerPlayer player)
                    NetworkHandler.sendToClient(new PacketPlayerMotion(motion.x(), motion.y(), motion.z()), player);
                else
                    target.setDeltaMovement(motion);

                target.fallDistance = 0F;

                ((LivingEntity) target).addEffect(new MobEffectInstance(EffectRegistry.VANISHING, 5, 0, false, false));

                serverLevel.sendParticles(ParticleUtils.constructSimpleSpark(new Color(150 + random.nextInt(100), 100, 0), 0.2F, 20, 0.9F),
                        target.getX(), target.getY() + 1.25F, target.getZ(), Math.round(target.getBbHeight() * 3), 0.1F, 0.1F, 0.1F, 0.05F);
            } else {
                blacklist.add(target.getUUID());

                toRemoveFromEntities.add(target.getUUID());
            }
        }

        if (!toRemoveFromEntities.isEmpty())
            entities.removeAll(toRemoveFromEntities);

        List<UUID> toRemoveFromBlacklist = new ArrayList<>();

        for (UUID uuid : blacklist) {
            if (!serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox()).stream().map(Entity::getUUID).toList().contains(uuid))
                toRemoveFromBlacklist.add(uuid);
        }

        if (!toRemoveFromBlacklist.isEmpty())
            blacklist.removeAll(toRemoveFromBlacklist);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox())) {
            UUID uuid = target.getUUID();

            if (blacklist.contains(uuid) || getPair().entities.contains(uuid))
                continue;

            getPair().entities.add(uuid);
        }
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();

        if (this.isMaster())
            return;

        DissectionEntity pair = this.getPair();

        if (pair == null)
            return;

        pair.setLifeTime(Math.min(pair.getLifeTime(), 20));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIFE_TIME, 100);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        setMaxLifeTime(compound.getInt("maxLifeTime"));
        setMaster(compound.getBoolean("isMaster"));
        setLifeTime(compound.getInt("lifeTime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("maxLifeTime", getMaxLifeTime());
        compound.putBoolean("isMaster", isMaster());
        compound.putInt("lifeTime", getLifeTime());
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

//    @Nonnull
//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }
}