package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RegistryRegistry;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.containers.base.RelicContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelicExperienceOrbEntity extends Entity {
    private static final EntityDataAccessor<Integer> EXPERIENCE = SynchedEntityData.defineId(RelicExperienceOrbEntity.class, EntityDataSerializers.INT);

    public RelicExperienceOrbEntity(EntityType<? extends RelicExperienceOrbEntity> type, Level level) {
        super(type, level);
    }

    public static int getMaxExperience() {
        return 10;
    }

    public int getExperience() {
        return this.getEntityData().get(EXPERIENCE);
    }

    public void setExperience(int experience) {
        this.getEntityData().set(EXPERIENCE, experience);
    }

    public int getStage() {
        int stages = 5;

        return Mth.clamp(getExperience() <= 1 ? 1 : Math.min((getExperience() / (getMaxExperience() / Math.max(1, stages - 1))) + 1, stages), 1, stages);
    }

    private List<ItemStack> getUpgradeableRelics(Player player) {
        List<ItemStack> relics = new ArrayList<>();

        for (RelicContainer source : RegistryRegistry.RELIC_CONTAINER_REGISTRY.entrySet().stream().map(Map.Entry::getValue).toList())
            relics.addAll(source.gatherRelics().apply(player).stream().filter(entry -> !((IRelicItem) entry.getItem()).isRelicMaxLevel(player, entry)).toList());

        return relics;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isNoGravity())
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));

        if (!this.level().noCollision(this.getBoundingBox()))
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());

        if (this.tickCount >= 15) {
            if (this.getExperience() < getMaxExperience()) {
                for (RelicExperienceOrbEntity orb : this.level().getEntitiesOfClass(RelicExperienceOrbEntity.class, this.getBoundingBox())) {
                    if (orb.getUUID().equals(this.getUUID()) || orb.isRemoved() || orb.getExperience() >= getMaxExperience())
                        continue;

                    int diff = getMaxExperience() - this.getExperience();

                    if (orb.getExperience() < diff) {
                        this.setExperience(this.getExperience() + orb.getExperience());

                        orb.discard();
                    } else {
                        orb.setExperience(orb.getExperience() - diff);

                        this.setExperience(getMaxExperience());
                    }
                }
            }

            double maxDistance = 16;

            Player player = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), maxDistance, entity -> {
                Player entry = (Player) entity;

                return !entry.isSpectator() && !getUpgradeableRelics(entry).isEmpty();
            });

            if (player != null) {
                this.setDeltaMovement(this.getDeltaMovement().add(player.position().add(0F, player.getBbHeight() / 2F, 0F).subtract(this.position()).normalize().scale((maxDistance - this.position().distanceTo(player.position())) / (maxDistance * 10))));

                if (this.position().distanceTo(player.position()) <= player.getBbWidth() * 1.25F) {
                    List<ItemStack> upgradeable = getUpgradeableRelics(player);

                    if (!upgradeable.isEmpty()) {
                        ItemStack stack = upgradeable.get(random.nextInt(upgradeable.size()));

                        ((IRelicItem) stack.getItem()).spreadRelicExperience(player, stack, this.getExperience());

                        this.discard();

                        this.level().playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 0.5F, 1.25F + this.level().getRandom().nextFloat() * 0.75F);
                    }
                }
            }
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        float friction = 0.98F;

        if (this.onGround()) {
            BlockPos pos = getBlockPosBelowThatAffectsMyMovement();

            friction = this.level().getBlockState(pos).getFriction(this.level(), pos, this) * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(friction, 0.98D, friction));

        if (this.onGround())
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, -0.9D, 1.0D));

        if (this.tickCount >= 1000)
            this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setExperience(tag.getInt("experience"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("experience", getExperience());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(EXPERIENCE, 0);
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
        if (EXPERIENCE.equals(pKey))
            this.refreshDimensions();

        super.onSyncedDataUpdated(pKey);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        float scale = 0.15F + (getStage() * 0.05F);

        return EntityDimensions.scalable(scale, scale);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        double d0 = this.getBoundingBox().inflate(0.2F).getSize();

        if (Double.isNaN(d0))
            d0 = 1D;

        d0 *= 64D;

        return pDistance < d0 * d0;
    }
}