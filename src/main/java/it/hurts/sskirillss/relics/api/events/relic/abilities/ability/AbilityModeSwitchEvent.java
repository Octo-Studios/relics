package it.hurts.sskirillss.relics.api.events.relic.abilities.ability;

import it.hurts.sskirillss.relics.api.events.relic.abilities.ability.base.AbilityEvent;
import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

@Data
public class AbilityModeSwitchEvent extends AbilityEvent implements ICancellableEvent {
    private final String fromMode;
    private final String toMode;

    public AbilityModeSwitchEvent(LivingEntity bearer, ItemStack stack, String ability, String fromMode, String toMode) {
        super(bearer, stack, ability);

        this.fromMode = fromMode;
        this.toMode = toMode;
    }
}