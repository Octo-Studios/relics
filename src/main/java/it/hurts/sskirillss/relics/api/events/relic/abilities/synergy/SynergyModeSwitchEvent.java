package it.hurts.sskirillss.relics.api.events.relic.abilities.synergy;

import it.hurts.sskirillss.relics.api.events.relic.abilities.synergy.base.SynergyEvent;
import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

@Data
public class SynergyModeSwitchEvent extends SynergyEvent implements ICancellableEvent {
    private final String fromMode;
    private final String toMode;

    public SynergyModeSwitchEvent(LivingEntity bearer, ItemStack stack, String synergy, String fromMode, String toMode) {
        super(bearer, stack, synergy);

        this.fromMode = fromMode;
        this.toMode = toMode;
    }
}