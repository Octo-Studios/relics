package it.hurts.sskirillss.relics.api.events.relic.leveling;

import it.hurts.sskirillss.relics.api.events.relic.abilities.ability.base.AbilityEvent;
import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nullable;

@Data
public class RelicExperienceChangeEvent extends AbilityEvent implements ICancellableEvent {
    @Nullable
    private String experienceSource;

    private double delta;

    public RelicExperienceChangeEvent(LivingEntity bearer, ItemStack stack, @Nullable String ability, @Nullable String experienceSource, double delta) {
        super(bearer, stack, ability);

        this.experienceSource = experienceSource;
        this.delta = delta;
    }
}