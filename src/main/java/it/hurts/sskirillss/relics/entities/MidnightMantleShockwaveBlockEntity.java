package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.feet.SpringyBootItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class MidnightMantleShockwaveBlockEntity extends ShockwaveBlockEntity {
    public MidnightMantleShockwaveBlockEntity(EntityType<? extends ShockwaveBlockEntity> pEntityType, Level level) {
        super(pEntityType, level);
    }

    @Override
    public void processTargets() {
        var level = this.level();
        var owner = this.getOwner();

        for (var entity : level.getEntitiesOfClass(Entity.class, this.getBoundingBox(), entity -> !(entity instanceof ShockwaveBlockEntity) && (owner == null || !owner.getStringUUID().equals(entity.getStringUUID())))) {
            if (entity instanceof LivingEntity livingEntity) {
                var damage = this.getDamage();

                if (livingEntity.hurt(level.damageSources().explosion(owner, this), damage)) {
                    var stun = this.getStun();

                    if (stun > 0)
                        livingEntity.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, stun, 0, false, false));

//                    if (this.getStack().getItem() instanceof MidnightMantleItem relic) {
//                        relic.addAbilityMetricValue(livingEntity, this.getStack(), "bounce", "shockwave_targets", 1);
//
//                        relic.addAbilityMetricValue(livingEntity, this.getStack(), "bounce", "shockwave_damage", damage);
//
//                        if (stun > 0)
//                            relic.addAbilityMetricValue(livingEntity, this.getStack(), "bounce", "shockwave_stun", stun);
//                    }
                }
            }
        }
    }
}