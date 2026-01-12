package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.feet.SpringyBootItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

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
                        relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getStatisticData().getMetricData("shockwave_targets").addValue(1);

                        relic.getRelicData(livingEntity, stack).getLevelingData().addExperience("bounce", "shockwave_hit", 1);

                        relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getStatisticData().getMetricData("shockwave_damage").addValue(damage);

                        if (stun > 0)
                            relic.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("bounce").getStatisticData().getMetricData("shockwave_stun").addValue(stun / 20D);
                    }
                }
            }
        }
    }
}