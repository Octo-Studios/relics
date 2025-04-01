package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.EntityRegistry;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ShadowGlaiveEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> BOUNCES = SynchedEntityData.defineId(ShadowGlaiveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> TARGET = SynchedEntityData.defineId(ShadowGlaiveEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> BOUNCED_ENTITIES = SynchedEntityData.defineId(ShadowGlaiveEntity.class, EntityDataSerializers.STRING);

    private static final String TAG_BOUNCES_AMOUNT = "bounces";
    private static final String TAG_TARGET_UUID = "target";
    private static final String TAG_BOUNCED_ENTITIES = "entities";

    private boolean isBounced = false;
    private LivingEntity target;

    @Getter
    @Setter
    private ItemStack stack = ItemStack.EMPTY;

    public ShadowGlaiveEntity(EntityType<? extends ShadowGlaiveEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    public ShadowGlaiveEntity(Level world, LivingEntity throwerIn) {
        super(EntityRegistry.SHADOW_GLAIVE.get(), throwerIn, world);
    }

    public void setTarget(LivingEntity target) {
        this.target = target;

        if (target != null)
            entityData.set(TARGET, target.getUUID().toString());
    }

    private void locateNearestTarget() {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        if (entityData.get(BOUNCES) >= relic.getAbilityValue(stack, "glaive", "bounces")) {
            this.discard();

            return;
        }

        List<String> bouncedEntities = Arrays.asList(entityData.get(BOUNCED_ENTITIES).split(","));
        List<LivingEntity> entitiesAround = getLevel().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(relic.getAbilityValue(stack, "glaive", "radius")));

        entitiesAround = entitiesAround.stream()
                .filter(entity -> !bouncedEntities.contains(entity.getUUID().toString()))
                .filter(EntitySelector.NO_CREATIVE_OR_SPECTATOR)
                .filter(entity -> {
                    if (!(this.getOwner() instanceof Player player))
                        return false;

                    return !entity.getStringUUID().equals(player.getStringUUID())
                            && !EntityUtils.isAlliedTo(player, entity);
                })
                .filter(entity -> entity.hasLineOfSight(this))
                .sorted(Comparator.comparing(entity -> entity.position().distanceTo(this.position())))
                .collect(Collectors.toList());

        if (entitiesAround.isEmpty()) {
            if (isBounced)
                this.discard();

            return;
        }

        LivingEntity target = null;

        for (LivingEntity entity : entitiesAround) {
            if (entity == null || !entity.isAlive())
                continue;

            target = entity;

            break;
        }

        if (target == null || !target.isAlive()) {
            this.discard();

            return;
        }

        this.setTarget(target);
    }

    @Override
    public void tick() {
        super.tick();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        for (int i = 0; i < 3; i++)
            getLevel().addParticle(ParticleUtils.constructSimpleSpark(new Color(255, random.nextInt(100), 255), 0.2F, 30, 0.99F),
                    this.xo, this.yo, this.zo, MathUtils.randomFloat(random) * 0.01F, 0, MathUtils.randomFloat(random) * 0.01F);

        if (level.isClientSide())
            return;

        if (!isBounced && target == null && this.tickCount > 30)
            this.discard();

        if (this.tickCount > 300)
            this.discard();

        if (target == null && this.tickCount > 10 && this.tickCount % 2 == 0) {
            this.locateNearestTarget();

            return;
        }

        if (target != null && target.isAlive()) {
            EntityUtils.moveTowardsPosition(this, target.position()
                    .add(0D, target.getBbHeight() * 0.5D, 0D), 0.75F);

            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(0.3D, 3D, 0.3D))) {
                if (this.getOwner() instanceof Player player && entity.getStringUUID().equals(player.getStringUUID()))
                    continue;

                String bouncedEntitiesString = entityData.get(BOUNCED_ENTITIES);
                List<String> bouncedEntities = Arrays.asList(bouncedEntitiesString.split(","));

                float damage = (float) relic.getAbilityValue(stack, "glaive", "damage");

                if (this.getOwner() instanceof Player player) {
                    EntityUtils.hurt(entity, DamageSource.thrown(this, player), damage);
                    relic.addExperience(entity, stack, 1);
                } else
                    entity.hurt(DamageSource.MAGIC, damage);

                if (!bouncedEntities.contains(entity.getUUID().toString())) {
                    entityData.set(BOUNCED_ENTITIES, bouncedEntitiesString + "," + entity.getUUID());

                    entityData.set(BOUNCES, entityData.get(BOUNCES) + 1);

                    isBounced = true;
                }

                this.locateNearestTarget();

                break;
            }
        } else
            this.locateNearestTarget();
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(BOUNCES, 0);
        entityData.define(TARGET, "");
        entityData.define(BOUNCED_ENTITIES, "");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(TAG_BOUNCES_AMOUNT, entityData.get(BOUNCES));
        tag.putString(TAG_TARGET_UUID, entityData.get(TARGET));
        tag.putString(TAG_BOUNCED_ENTITIES, entityData.get(BOUNCED_ENTITIES));

        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(BOUNCES, tag.getInt(TAG_BOUNCES_AMOUNT));
        entityData.set(TARGET, tag.getString(TAG_TARGET_UUID));
        entityData.set(BOUNCED_ENTITIES, tag.getString(TAG_BOUNCED_ENTITIES));

        super.readAdditionalSaveData(tag);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected float getGravity() {
        return 0F;
    }

    @Nonnull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}