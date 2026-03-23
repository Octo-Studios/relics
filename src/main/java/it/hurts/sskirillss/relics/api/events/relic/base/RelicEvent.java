package it.hurts.sskirillss.relics.api.events.relic.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

@Data
@AllArgsConstructor
public abstract class RelicEvent extends Event {
    private LivingEntity bearer;
    private ItemStack stack;
}