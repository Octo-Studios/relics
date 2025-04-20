package it.hurts.sskirillss.relics.api.relics.events;

import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

@Data
public class RelicLevelingPointsChangeEvent extends RelicEvent  implements ICancellableEvent {
    private int delta;

    public RelicLevelingPointsChangeEvent(LivingEntity bearer, ItemStack stack, int delta) {
        super(bearer, stack);

        this.delta = delta;
    }
}