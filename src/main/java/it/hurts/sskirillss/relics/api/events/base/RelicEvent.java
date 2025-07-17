package it.hurts.sskirillss.relics.api.events.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

@AllArgsConstructor
public class RelicEvent extends Event {
    @Getter
    LivingEntity entity;

    @Getter
    ItemStack stack;
}