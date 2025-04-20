package it.hurts.sskirillss.relics.api.relics.events;

import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

@Data
public class RelicExperienceChangeEvent extends RelicEvent implements ICancellableEvent {
    private double delta;

    public RelicExperienceChangeEvent(LivingEntity bearer, ItemStack stack, double delta) {
        super(bearer, stack);

        this.delta = delta;
    }
}