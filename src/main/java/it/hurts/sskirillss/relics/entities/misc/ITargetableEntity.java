package it.hurts.sskirillss.relics.entities.misc;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public interface ITargetableEntity {
    @Nullable
    LivingEntity getTarget();

    void setTarget(LivingEntity target);
}
