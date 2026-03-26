package it.hurts.sskirillss.relics.api.events.utility;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class EntityBlockSpeedFactorEvent extends EntityEvent {
    @Getter
    private final BlockState state;

    @Getter
    @Setter
    private float speedFactor;

    public EntityBlockSpeedFactorEvent(Entity entity, BlockState state, float speedFactor) {
        super(entity);

        this.state = state;
        this.speedFactor = speedFactor;
    }
}