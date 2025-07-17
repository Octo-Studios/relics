package it.hurts.sskirillss.relics.api.events.leveling;

import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

@Data
public class AbilityModeSwitchEvent extends Event implements ICancellableEvent {
    private final LivingEntity entity;

    private final ItemStack stack;

    private final String ability;

    private final String fromMode;
    private final String toMode;

    public AbilityModeSwitchEvent(LivingEntity entity, ItemStack stack, String ability, String fromMode, String toMode) {
        this.entity = entity;
        this.stack = stack;

        this.ability = ability;

        this.fromMode = fromMode;
        this.toMode = toMode;
    }
}