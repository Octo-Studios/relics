package it.hurts.sskirillss.relics.entities;

import it.hurts.octostudios.octolib.module.particle.OctoRenderManager;
import it.hurts.octostudios.octolib.module.particle.trail.EntityTrailProvider;
import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.sync.S2CEntityTargetPacket;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class SporeEntity extends ThrowableProjectile implements ITargetableEntity {
    private static final EntityDataAccessor<ItemStack> RELIC_STACK = SynchedEntityData.defineId(SporeEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(SporeEntity.class, EntityDataSerializers.FLOAT);

    public void setRelicStack(ItemStack stack) {
        this.getEntityData().set(RELIC_STACK, stack);
    }

    public ItemStack getRelicStack() {
        return this.getEntityData().get(RELIC_STACK);
    }

    public void setDamage(float damage) {
        this.getEntityData().set(DAMAGE, damage);
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    private LivingEntity target;

    public SporeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        var level = getCommandSenderWorld();
        var particleCenter = this.position().add(this.getDeltaMovement().scale(-1F));

        if (tickCount > 3)
            for (int i = 0; i < 3; i++)
                level.addParticle(ParticleUtils.constructSimpleSpark(new Color(50 + random.nextInt(100), 150 + random.nextInt(100), 0), 0.01F + random.nextFloat() * Math.min(tickCount * 0.01F, 0.1F), 5 + random.nextInt(3), 0.9F),
                        particleCenter.x() + MathUtils.randomFloat(random) * 0.05F, particleCenter.y() + MathUtils.randomFloat(random) * 0.05F, particleCenter.z() + MathUtils.randomFloat(random) * 0.05F, 0F, 0F, 0F);

        if (this.tickCount >= 250) {
            if (!level.isClientSide())
                this.discard();

            return;
        }

        if (target == null || target.isDeadOrDying()) {
            if (level.isClientSide())
                return;

            var targets = locateNearestTargets();

            if (targets.isEmpty())
                return;

            setTarget(targets.get(random.nextInt(targets.size())));

            NetworkHandler.sendToClientsTrackingEntity(new S2CEntityTargetPacket(this.getId(), target.getId()), this);

            return;
        }

        var direction = target.position().subtract(this.position()).normalize();
        var motion = this.getDeltaMovement();

        var factor = Math.clamp(tickCount * 0.05F, 0F, 1F);

        this.setDeltaMovement(motion.x + (direction.x * factor - motion.x) * factor, motion.y, motion.z + (direction.z * factor - motion.z) * factor);
    }

    public List<LivingEntity> locateNearestTargets() {
        return EntityUtils.gatherPotentialTargets(this, LivingEntity.class, 32)
                .filter(entity -> (!(this.getOwner() instanceof Player player) || !EntityUtils.isAlliedTo(player, entity)))
                .toList();
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();

        var level = getCommandSenderWorld();
        var vec = this.position();

        level.playSound(null, this.blockPosition(), SoundEvents.PUFFER_FISH_BLOW_UP, SoundSource.MASTER, 0.5F, 1.5F + random.nextFloat() * 0.5F);

        for (int i = 0; i < 3; i++)
            ParticleUtils.createBall(ParticleUtils.constructSimpleSpark(new Color(50 + random.nextInt(100), 150 + random.nextInt(100), 0), 0.1F + random.nextFloat() * 0.2F, 20 + random.nextInt(10), 0.95F),
                    vec, level, 1, 0.025F + random.nextFloat() * 0.15F);

    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (this.getOwner() instanceof Player player && result instanceof EntityHitResult entityResult && entityResult.getEntity() instanceof LivingEntity entity && !entity.getStringUUID().equals(player.getStringUUID())) {
            entity.invulnerableTime = 0;

            var stack = getRelicStack();

            if (entity.hurt(getCommandSenderWorld().damageSources().thrown(this, player), getDamage())) {
                if (stack.getItem() instanceof IRelicItem relic)
                    relic.spreadRelicExperience(player, stack, 1);

                if (this.isOnFire())
                    entity.igniteForTicks(this.getRemainingFireTicks());

                entity.addEffect(new MobEffectInstance(EffectRegistry.ANTI_HEAL, 0, 20 * 5, false, false), player);
            }
        }

        this.discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RELIC_STACK, ItemStack.EMPTY);
        builder.define(DAMAGE, 1F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.put("relic_stack", getRelicStack().save(this.registryAccess()));
        tag.putFloat("damage", getDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        setRelicStack(ItemStack.parseOptional(this.registryAccess(), tag.getCompound("relic_stack")));
        setDamage(tag.getFloat("damage"));
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return target;
    }

    @Override
    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    @OnlyIn(Dist.CLIENT)
    public static class TrailProvider extends EntityTrailProvider<SporeEntity> {
        public TrailProvider(SporeEntity entity) {
            super(entity);
        }

        @Override
        public Vec3 getTrailPosition(float partialTicks) {
            return entity.getPosition(partialTicks).add(entity.getDeltaMovement().scale(-1));
        }

        @Override
        public int getTrailUpdateFrequency() {
            return 1;
        }

        @Override
        public boolean isTrailAlive() {
            return entity.isAlive();
        }

        @Override
        public boolean isTrailGrowing() {
            return entity.tickCount > 2;
        }

        @Override
        public int getTrailMaxLength() {
            return 3;
        }

        @Override
        public int getTrailFadeInColor() {
            return 0xFF55FF00;
        }

        @Override
        public int getTrailFadeOutColor() {
            return 0x8080FF00;
        }

        @Override
        public double getTrailScale() {
            return 0.075F;
        }
    }
}