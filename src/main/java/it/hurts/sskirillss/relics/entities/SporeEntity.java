package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class SporeEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(SporeEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> STUCK = SynchedEntityData.defineId(SporeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(SporeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(SporeEntity.class, EntityDataSerializers.ITEM_STACK);

    public void setSize(float amount) {
        this.getEntityData().set(SIZE, amount);
    }

    public float getSize() {
        return this.getEntityData().get(SIZE);
    }

    public void setStuck(boolean value) {
        this.getEntityData().set(STUCK, value);
    }

    public boolean isStuck() {
        return this.getEntityData().get(STUCK);
    }

    public void setLifetime(int amount) {
        this.getEntityData().set(LIFETIME, amount);
    }

    public int getLifetime() {
        return this.getEntityData().get(LIFETIME);
    }

    public void setStack(ItemStack stack) {
        this.getEntityData().set(STACK, stack);
    }

    public ItemStack getStack() {
        return this.getEntityData().get(STACK);
    }

    public SporeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        ItemStack stack = getStack();

        if (!(stack.getItem() instanceof IRelicItem relic)) {
            this.discard();

            return;
        }

        if (this.isStuck())
            this.setDeltaMovement(0, 0, 0);

        Level level = level();

        if (!level.isClientSide()) {
            if (isStuck())
                setLifetime(getLifetime() + 1);

            if (getLifetime() > relic.getStatValue(stack, "spore", "duration") * 20) {
                level.playSound(null, this.blockPosition(), SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.MASTER, 1F, 1F + random.nextFloat());

                double inlinedSize = Math.pow(Math.log10(1 + getSize()), 1D / 3D);

                ParticleUtils.createBall(ParticleUtils.constructSimpleSpark(new Color(100 + level.getRandom().nextInt(50), 255, 0),
                                (float) (inlinedSize * 0.35F), 40, 0.9F),
                        this.position().add(0, inlinedSize / 3, 0), level, (int) Math.ceil(1 + inlinedSize), (float) (inlinedSize / 2D));

                if (this.getOwner() instanceof Player player) {
                    RandomSource random = player.getRandom();

                    for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1F + (Math.pow(Math.log10(1 + getSize()), 1D / 3D) / 2F)))) {
                        if (entity.getStringUUID().equals(player.getStringUUID()))
                            continue;

                        if (EntityUtils.hurt(entity, level.damageSources().thrown(this, player), (float) (getSize() * relic.getStatValue(stack, "spore", "damage")))) {
                            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
                            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100));
                            entity.addEffect(new MobEffectInstance(EffectRegistry.ANTI_HEAL, 100));
                        }
                    }

                    if (getSize() >= 1) {
                        int count = (int) Math.ceil(Math.pow(getSize(), relic.getStatValue(stack, "multiplying", "amount")));

                        for (int i = 0; i < count; i++) {
                            if (random.nextFloat() > relic.getStatValue(stack, "multiplying", "chance"))
                                break;

                            float mul = this.getBbHeight() / 1.5F;
                            float speed = 0.1F + random.nextFloat() * 0.2F;
                            Vec3 motion = new Vec3(MathUtils.randomFloat(random) * speed, speed, MathUtils.randomFloat(random) * speed);

                            SporeEntity spore = new SporeEntity(EntityRegistry.SPORE.get(), level);

                            spore.setOwner(player);
                            spore.setStack(stack);
                            spore.setDeltaMovement(motion);
                            spore.setPos(this.position().add(0, mul, 0).add(motion.normalize().scale(mul)));
                            spore.setSize((float) (this.getSize() * relic.getStatValue(stack, "multiplying", "size")));

                            level.addFreshEntity(spore);

                            relic.spreadRelicExperience(player, stack, 1);
                        }
                    }
                }

                this.discard();
            }
        }

        RandomSource random = level.getRandom();

        double inlinedSize = Math.pow(Math.log10(1 + getSize()), 1D / 3D);

        if (isStuck()) {
            ParticleUtils.createBall(ParticleUtils.constructSimpleSpark(new Color(random.nextInt(200), 255, 0), (float) (inlinedSize * 0.25F), 40, 0.95F),
                    this.position().add(0, inlinedSize / 6, 0), level, 0, (float) (inlinedSize * 0.025F));
        } else {
            level.addParticle(ParticleUtils.constructSimpleSpark(new Color(random.nextInt(200), 255, 0), (float) (inlinedSize * 0.25F), 40, 0.9F),
                    this.getX(), this.getY() + (inlinedSize / 6F), this.getZ(), MathUtils.randomFloat(random) * 0.025F,
                    MathUtils.randomFloat(random) * 0.025F, MathUtils.randomFloat(random) * 0.025F);
        }

        if (!(this.getOwner() instanceof Player player))
            return;

        if (isStuck()) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox())) {
                if (entity.getStringUUID().equals(player.getStringUUID()))
                    continue;

                setLifetime((int) Math.max(getLifetime(), Math.round(relic.getStatValue(stack, "spore", "duration") * 20) - 20));

                break;
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.setDeltaMovement(0, 0, 0);

        this.setStuck(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SIZE, 0.5F);
        builder.define(LIFETIME, 0);
        builder.define(STUCK, false);
        builder.define(STACK, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        setSize(compound.getFloat("size"));
        setStuck(compound.getBoolean("stuck"));
        setLifetime(compound.getInt("lifetime"));

        setStack(ItemStack.parseOptional(this.registryAccess(), compound.getCompound("stack")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("size", getSize());
        compound.putBoolean("stuck", isStuck());
        compound.putInt("lifetime", getLifetime());

        getStack().save(this.registryAccess(), compound.getCompound("stack"));
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (SIZE.equals(pKey))
            this.refreshDimensions();

        super.onSyncedDataUpdated(pKey);
    }

//    @Nonnull
//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        float inlinedSize = (float) Math.pow(Math.log10(1 + getSize()), 1D / 3D);

        return EntityDimensions.scalable(inlinedSize / 2F, inlinedSize / 2F);
    }
}