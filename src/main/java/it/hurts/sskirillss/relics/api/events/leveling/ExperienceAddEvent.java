package it.hurts.sskirillss.relics.api.events.leveling;

import it.hurts.sskirillss.relics.api.events.base.RelicEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nullable;

public class ExperienceAddEvent extends RelicEvent implements ICancellableEvent {
    @Getter
    @Setter
    private double amount;

    public ExperienceAddEvent(@Nullable LivingEntity entity, ItemStack stack, double amount) {
        super(entity, stack);

        this.amount = amount;
    }
}