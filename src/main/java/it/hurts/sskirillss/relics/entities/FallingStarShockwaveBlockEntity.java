package it.hurts.sskirillss.relics.entities;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.utils.TargetingUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class FallingStarShockwaveBlockEntity extends ShockwaveBlockEntity {
    public FallingStarShockwaveBlockEntity(EntityType<? extends ShockwaveBlockEntity> pEntityType, Level level) {
        super(pEntityType, level);
    }

    @Override
    public void processTargets() {
        var level = this.level();
        var owner = this.getOwner();

        for (var entity : level.getEntitiesOfClass(Entity.class, this.getBoundingBox(), entity -> !(entity instanceof ShockwaveBlockEntity) && (owner == null || !owner.getStringUUID().equals(entity.getStringUUID())))) {
            if (entity instanceof LivingEntity livingEntity) {
                if (!TargetingUtils.canHarm(owner, livingEntity, this.getStack(), "starfall"))
                    continue;

                var damage = this.getDamage();

                if (TargetingUtils.hurtEnemy(livingEntity, level.damageSources().explosion(owner, this), damage, this.getStack(), "starfall")) {
                    var stun = this.getStun();

                    if (stun > 0)
                        TargetingUtils.addHarmfulEffect(livingEntity, new MobEffectInstance(RelicsMobEffects.STUN, stun, 0, false, false), owner, this.getStack(), "starfall");

                    var stack = this.getStack();

                    if (stack.getItem() instanceof MidnightMantleItem relic && owner instanceof LivingEntity livingOwner) {
                        relic.getRelicData(livingOwner, stack).getAbilitiesData().getAbilityData("starfall").getStatisticData().getMetricData("shockwave_targets").addValue(1);

                        relic.getRelicData(livingOwner, stack).getLevelingData().addExperience("starfall", "shockwave_hit", 1);

                        relic.getRelicData(livingOwner, stack).getAbilitiesData().getAbilityData("starfall").getStatisticData().getMetricData("shockwave_damage").addValue(damage);

                        if (stun > 0)
                            relic.getRelicData(livingOwner, stack).getAbilitiesData().getAbilityData("starfall").getStatisticData().getMetricData("shockwave_stun").addValue(stun / 20F);
                    }
                }
            }
        }
    }
}
