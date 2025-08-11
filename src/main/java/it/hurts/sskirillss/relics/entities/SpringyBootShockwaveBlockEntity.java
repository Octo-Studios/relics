package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.feet.SpringyBootItem;
import lombok.Getter;
import lombok.Setter;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpringyBootShockwaveBlockEntity extends ShockwaveBlockEntity {
    public SpringyBootShockwaveBlockEntity(EntityType<? extends ShockwaveBlockEntity> pEntityType, Level level) {
        super(pEntityType, level);
    }

    @Override
    public void processTargets() {
        var level = this.level();
        var center = this.getCenter().getCenter();
        var owner = this.getOwner();

        for (var entity : level.getEntitiesOfClass(Entity.class, this.getBoundingBox(), entity -> !(entity instanceof ShockwaveBlockEntity) && (owner == null || !owner.getStringUUID().equals(entity.getStringUUID())))) {
            var motion = entity.position().add(0F, 1F, 0F).subtract(center).normalize().scale(this.getKnockback());

            entity.setDeltaMovement(motion);

            if (entity instanceof LivingEntity livingEntity) {
                var damage = this.getDamage();

                if (livingEntity.hurt(level.damageSources().explosion(owner, this), damage)) {
                    var stun = this.getStun();

                    if (stun > 0)
                        livingEntity.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, stun, 0, false, false));

                    var stack = this.getStack();

                    if (stack.getItem() instanceof SpringyBootItem relic) {
                        relic.addAbilityMetricValue(livingEntity, stack, "bounce", "shockwave_targets", 1);

                        if (relic.canAddRelicExperience(livingEntity, stack, "bounce", "shockwave_hit"))
                            relic.addRelicExperience(livingEntity, stack, "bounce", "shockwave_hit", 1);

                        relic.addAbilityMetricValue(livingEntity, stack, "bounce", "shockwave_damage", damage);

                        if (stun > 0)
                            relic.addAbilityMetricValue(livingEntity, stack, "bounce", "shockwave_stun", stun / 20D);
                    }
                }
            }
        }
    }
}