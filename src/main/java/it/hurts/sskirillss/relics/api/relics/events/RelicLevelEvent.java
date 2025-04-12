package it.hurts.sskirillss.relics.api.relics.events;

import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;

public abstract class RelicLevelEvent extends RelicEvent {
    public RelicLevelEvent(LivingEntity bearer, ItemStack stack) {
        super(bearer, stack);
    }

    @Data
    @ApiStatus.Experimental
    public static class Get extends RelicLevelEvent {
        private int level;

        public Get(LivingEntity bearer, ItemStack stack, int level) {
            super(bearer, stack);

            this.level = level;
        }
    }

    @Data
    @ApiStatus.Experimental
    public static class Set extends RelicLevelEvent implements ICancellableEvent {
        private int level;

        public Set(LivingEntity bearer, ItemStack stack, int level) {
            super(bearer, stack);

            this.level = level;
        }
    }

    @Data
    @ApiStatus.Experimental
    public static class Add extends RelicLevelEvent implements ICancellableEvent {
        private int level;

        public Add(LivingEntity bearer, ItemStack stack, int level) {
            super(bearer, stack);

            this.level = level;
        }
    }
}