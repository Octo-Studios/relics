package it.hurts.sskirillss.relics.api.events.utility;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class LivingSlippingEvent extends LivingEvent {
    @Getter
    private final BlockState state;

    @Getter
    @Setter
    private float friction;

    public LivingSlippingEvent(LivingEntity entity, BlockState state, float friction) {
        super(entity);

        this.state = state;
        this.friction = friction;
    }
}