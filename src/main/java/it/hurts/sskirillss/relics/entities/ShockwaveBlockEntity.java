package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ShockwaveBlockEntity extends Projectile {
    private static final EntityDataAccessor<BlockState> BLOCK_STATE = SynchedEntityData.defineId(ShockwaveBlockEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<BlockPos> CENTER = SynchedEntityData.defineId(ShockwaveBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(ShockwaveBlockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> STUN = SynchedEntityData.defineId(ShockwaveBlockEntity.class, EntityDataSerializers.INT);

    public BlockState getBlockState() {
        return this.getEntityData().get(BLOCK_STATE);
    }

    public void setBlockState(BlockState state) {
        this.getEntityData().set(BLOCK_STATE, state);
    }

    public BlockPos getCenter() {
        return this.getEntityData().get(CENTER);
    }

    public void setCenter(BlockPos center) {
        this.getEntityData().set(CENTER, center);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public int getStun() {
        return this.getEntityData().get(STUN);
    }

    public void setStun(int stun) {
        this.getEntityData().set(STUN, stun);
    }

    public ShockwaveBlockEntity(EntityType<? extends ShockwaveBlockEntity> pEntityType, Level level) {
        super(pEntityType, level);

        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();

        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.075D, 0.0D));

        this.move(MoverType.SELF, this.getDeltaMovement());

        if (this.tickCount % 100 == 0 || (this.tickCount > 10 && this.getCommandSenderWorld().getBlockState(this.blockPosition().above()).blocksMotion()))
            this.remove(RemovalReason.KILLED);

        var level = this.level();
        var center = this.getCenter().getCenter();

        var owner = this.getOwner();

        for (var entity : level.getEntitiesOfClass(Entity.class, this.getBoundingBox(), entity -> !(entity instanceof ShockwaveBlockEntity) && (owner == null || !owner.getStringUUID().equals(entity.getStringUUID())))) {
            var motion = entity.position().subtract(center).normalize().add(0F, 1F, 0F);

            entity.setDeltaMovement(motion);

            if (entity instanceof LivingEntity livingEntity) {
                if (livingEntity.hurt(level.damageSources().explosion(owner, this), this.getDamage()))
                    livingEntity.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, this.getStun(), 0, false, false));
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK_STATE, Blocks.AIR.defaultBlockState());
        builder.define(CENTER, BlockPos.ZERO);
        builder.define(DAMAGE, 0F);
        builder.define(STUN, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setBlockState(NbtUtils.readBlockState(this.getCommandSenderWorld().holderLookup(Registries.BLOCK), tag.getCompound("block_state")));
        this.setCenter(NbtUtils.readBlockPos(tag, "center").orElse(BlockPos.ZERO));
        this.setDamage(tag.getFloat("damage"));
        this.setStun(tag.getInt("stun"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.put("block_state", NbtUtils.writeBlockState(this.getBlockState()));
        tag.put("center", NbtUtils.writeBlockPos(this.getCenter()));
        tag.putFloat("damage", this.getDamage());
        tag.putInt("stun", this.getStun());
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }
}