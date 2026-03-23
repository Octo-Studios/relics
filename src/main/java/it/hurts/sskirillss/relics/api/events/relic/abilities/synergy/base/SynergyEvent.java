package it.hurts.sskirillss.relics.api.events.relic.abilities.synergy.base;

import it.hurts.sskirillss.relics.api.events.relic.base.RelicEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Data
public abstract class SynergyEvent extends RelicEvent {
    private String synergy;

    public SynergyEvent(LivingEntity bearer, ItemStack stack, String synergy) {
        super(bearer, stack);

        this.synergy = synergy;
    }
}