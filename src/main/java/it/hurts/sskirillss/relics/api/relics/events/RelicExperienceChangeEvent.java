package it.hurts.sskirillss.relics.api.relics.events;

import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Data
public abstract class RelicExperienceChangeEvent extends RelicEvent {
    private double delta;

    public RelicExperienceChangeEvent(LivingEntity bearer, ItemStack stack, double delta) {
        super(bearer, stack);

        this.delta = delta;
    }
}