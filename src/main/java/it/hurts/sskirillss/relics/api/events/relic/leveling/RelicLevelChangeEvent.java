package it.hurts.sskirillss.relics.api.events.relic.leveling;

import it.hurts.sskirillss.relics.api.events.relic.base.RelicEvent;
import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

@Data
public class RelicLevelChangeEvent extends RelicEvent implements ICancellableEvent {
    private int delta;

    public RelicLevelChangeEvent(LivingEntity bearer, ItemStack stack, int delta) {
        super(bearer, stack);

        this.delta = delta;
    }
}