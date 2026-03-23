package it.hurts.sskirillss.relics.api.events.relic.abilities.ability.base;

import it.hurts.sskirillss.relics.api.events.relic.base.RelicEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Data
public abstract class AbilityEvent extends RelicEvent {
    private String ability;

    public AbilityEvent(LivingEntity bearer, ItemStack stack, String ability) {
        super(bearer, stack);

        this.ability = ability;
    }
}