package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.items.relics.head.PiglinMaskItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;

public class GoldenToothEntity extends Entity {
    private static final EntityDataAccessor<Integer> STACKS = SynchedEntityData.defineId(GoldenToothEntity.class, EntityDataSerializers.INT);

    public GoldenToothEntity(EntityType<? extends GoldenToothEntity> type, Level level) {
        super(type, level);
    }

    public int getStacks() {
        return this.getEntityData().get(GoldenToothEntity.STACKS);
    }

    public void setStacks(int experience) {
        this.getEntityData().set(GoldenToothEntity.STACKS, experience);
    }

    private List<ItemStack> getSuitableRelics(LivingEntity entity) {
        return EntityUtils.findEquippedCurios(entity, RelicsItems.PIGLIN_MASK.get());
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isNoGravity())
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));

        if (!this.level().noCollision(this.getBoundingBox()))
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());

        if (this.tickCount >= 15) {
            for (var tooth : this.level().getEntitiesOfClass(GoldenToothEntity.class, this.getBoundingBox())) {
                if (tooth.getUUID().equals(this.getUUID()) || tooth.isRemoved())
                    continue;

                this.setStacks(this.getStacks() + tooth.getStacks());

                tooth.discard();
            }

            double maxDistance = 16;

            var player = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), maxDistance, entity -> {
                var entry = (Player) entity;

                return !entry.isSpectator() && !this.getSuitableRelics(entry).isEmpty();
            });

            if (player != null) {
                this.setDeltaMovement(this.getDeltaMovement().add(player.position().add(0F, player.getBbHeight() / 2F, 0F).subtract(this.position()).normalize().scale((maxDistance - this.position().distanceTo(player.position())) / (maxDistance * 10))));

                if (this.position().distanceTo(player.position()) <= player.getBbWidth() * 1.25F) {
                    var suitable = this.getSuitableRelics(player);

                    for (var stack : suitable) {
                        var relic = ((PiglinMaskItem) stack.getItem());

                        if (relic.getStacks(stack) < PiglinMaskItem.getMaxStacks()) {
                            relic.addStacks(stack, this.getStacks());
                            relic.setDuration(player, stack, relic.getMaxDuration(player, stack));

                            relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("looting").getStatisticData().getMetricData("teeth_picked_up").addValue(this.getStacks());
                            relic.getRelicData(player, stack).getLevelingData().addExperience("looting", "stack", this.getStacks());
                        }

                        player.addItem(new ItemStack(RelicsItems.GOLDEN_TOOTH.get(), this.getStacks()));

                        this.discard();

                        this.level().playSound(null, this.blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD.value(), SoundSource.MASTER, 0.5F, 1.25F + this.level().getRandom().nextFloat() * 0.75F);
                    }
                }
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        var friction = 0.98F;

        if (this.onGround()) {
            var pos = getBlockPosBelowThatAffectsMyMovement();

            friction = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

        if (this.onGround())
            this.setDeltaMovement(this.getDeltaMovement().multiply(1D, -0.9D, 1D));

        if (this.tickCount >= 1000)
            this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setStacks(tag.getInt("stacks"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("stacks", this.getStacks());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STACKS, 0);
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999F);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public boolean isPushedByFluid(FluidType type) {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (GoldenToothEntity.STACKS.equals(pKey))
            this.refreshDimensions();

        super.onSyncedDataUpdated(pKey);
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<GoldenToothEntity> {
        public TrailProvider(GoldenToothEntity entity) {
            super(entity);
        }

        @Override
        public Vec3 getTrailPosition(float partialTicks) {
            return this.entity.getPosition(partialTicks).add(0F, 0.45F, 0F);
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
            return 0x00FF0000;
        }

        @Override
        public double getTrailScale() {
            return 0.1F;
        }
    }
}