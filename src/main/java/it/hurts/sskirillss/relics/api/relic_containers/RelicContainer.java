package it.hurts.sskirillss.relics.api.relic_containers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public abstract class RelicContainer {
    public abstract Function<LivingEntity, List<ItemStack>> gatherRelics();
}