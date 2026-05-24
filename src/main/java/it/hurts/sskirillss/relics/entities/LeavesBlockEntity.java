package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.back.LeafyMantleItem;
import it.hurts.sskirillss.relics.items.relics.necklace.ReflectiveNecklaceItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CSyncEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.TargetingUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LeavesBlockEntity extends ThrowableProjectile implements ITargetableEntity {
    private static final EntityDataAccessor<BlockState> BLOCK_STATE = SynchedEntityData.defineId(LeavesBlockEntity.class, EntityDataSerializers.BLOCK_STATE);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(LeavesBlockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PARALYSIS = SynchedEntityData.defineId(LeavesBlockEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> FLAWLESS = SynchedEntityData.defineId(LeavesBlockEntity.class, EntityDataSerializers.BOOLEAN);

    @Getter
    @Setter
    private ItemStack stack = ItemStack.EMPTY;

    public BlockState getBlockState() {
        return this.getEntityData().get(BLOCK_STATE);
    }

    public void setBlockState(BlockState state) {
        this.getEntityData().set(BLOCK_STATE, state);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setParalysis(float paralysis) {
        this.getEntityData().set(PARALYSIS, paralysis);
    }

    public float getParalysis() {
        return this.getEntityData().get(PARALYSIS);
    }

    public void setFlawless(boolean flawless) {
        this.getEntityData().set(FLAWLESS, flawless);
    }

    public boolean isFlawless() {
        return this.getEntityData().get(FLAWLESS);
    }

    private LivingEntity target;

    private List<String> impactedEntities = new ArrayList<>();

    public LeavesBlockEntity(EntityType<? extends LeavesBlockEntity> type, Level worldIn) {
        super(type, worldIn);

        this.noPhysics = true;
    }

    @Override
    public void tick() {
        var motion = this.getDeltaMovement();

        super.tick();

        var level = this.getCommandSenderWorld();
        var random = level.getRandom();

        for (int i = 0; i < 3; i++)
            level.addParticle(ParticleUtils.constructSimpleSpark(this.isFlawless() ? new Color(200 + random.nextInt(50), 150 + random.nextInt(50), 0) : new Color(random.nextInt(75), 150 + random.nextInt(100), random.nextInt(25)), 0.1F + (random.nextFloat() * 0.15F), 25 + random.nextInt(10), 0.9F), this.getX() + MathUtils.randomFloat(random) * 0.25F, this.getY() + MathUtils.randomFloat(random) * 0.25F + this.getBbHeight() / 2F, this.getZ() + MathUtils.randomFloat(random) * 0.25F, MathUtils.randomFloat(random) * 0.01F, MathUtils.randomFloat(random) * 0.01F, MathUtils.randomFloat(random) * 0.01F);

        if (this.target == null || this.target.isDeadOrDying() || this.tickCount >= 200) {
            if (!level.isClientSide())
                this.discard();

            return;
        }

        var targetPos = this.target.position().add(0F, this.target.getBbHeight() / 2F, 0F);

        if (this.position().distanceTo(targetPos) > 1F) {
            var factor = Math.clamp(tickCount * 0.035F, 0F, 1F);

            this.setDeltaMovement(motion.lerp(targetPos.subtract(this.position()).normalize().scale(factor), factor));
        } else {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(result.getEntity() instanceof LivingEntity entity) || this.impactedEntities.contains(entity.getStringUUID())
                || (!(this.getOwner() instanceof LivingEntity owner) || entity.getStringUUID().equals(owner.getStringUUID()))
                || !TargetingUtils.canHarm(owner, entity, this.getStack(), "revival"))
            return;

        var level = this.level();

        entity.invulnerableTime = 0;

        if (TargetingUtils.hurtEnemy(entity, level.damageSources().thrown(owner, this), this.getDamage(), this.getStack(), "revival")) {
            var paralysis = this.getParalysis();

            if (paralysis > 0)
                TargetingUtils.addHarmfulEffect(entity, new MobEffectInstance(RelicsMobEffects.PARALYSIS, (int) (paralysis * 20), 0, false, false), owner, this.getStack(), "revival");

            if (stack.getItem() instanceof LeafyMantleItem relic) {
                relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("revival").getStatisticData().getMetricData("damage_dealt").addValue(this.getDamage());

                if (!level.isClientSide())
                    relic.getRelicData(entity, stack).getLevelingData().addExperience("revival", "leaves_impact", 1);

                if (paralysis > 0)
                    relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("revival").getStatisticData().getMetricData("paralysis_duration").addValue(paralysis);
            }
        }

        this.impactedEntities.add(entity.getStringUUID());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide())
            return false;

        var owner = this.getOwner();
        var attacker = source.getEntity();

        if (owner != null && attacker != null && owner.getStringUUID().equals(attacker.getStringUUID()))
            return false;

        this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1F, 1F);

        this.discard();

        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK_STATE, Blocks.OAK_LEAVES.defaultBlockState());
        builder.define(DAMAGE, 0F);
        builder.define(PARALYSIS, 0F);
        builder.define(FLAWLESS, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.put("block_state", NbtUtils.writeBlockState(this.getBlockState()));
        tag.putFloat("damage", this.getDamage());
        tag.putFloat("paralysis", this.getParalysis());
        tag.putBoolean("flawless", this.isFlawless());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.setBlockState(NbtUtils.readBlockState(this.getCommandSenderWorld().holderLookup(Registries.BLOCK), tag.getCompound("block_state")));
        this.setDamage(tag.getFloat("damage"));
        this.setParalysis(tag.getFloat("paralysis"));
        this.setFlawless(tag.getBoolean("flawless"));
    }

    @Override
    protected double getDefaultGravity() {
        return 0D;
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(LivingEntity target) {
        this.target = target;

        if (!level().isClientSide() && target != null)
            NetworkHandler.sendToClientsTrackingEntity(new S2CSyncEntityTargetPacket(this.getId(), target.getId()), this);
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<LeavesBlockEntity> {
        public TrailProvider(LeavesBlockEntity entity) {
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
            return 10;
        }

        @Override
        public int getTrailFadeInColor() {
            return entity.isFlawless() ? 0xFFFFFF00 : 0xFF00FF00;
        }

        @Override
        public int getTrailFadeOutColor() {
            return entity.isFlawless() ? 0x00FF0000 : 0x80FFFF00;
        }

        @Override
        public double getTrailScale() {
            return 0.15F;
        }
    }
}
