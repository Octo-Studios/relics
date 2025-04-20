package it.hurts.sskirillss.relics.api.relics.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

@Getter
@AllArgsConstructor
public abstract class RelicEvent extends Event {
    private LivingEntity bearer;
    private ItemStack stack;
}