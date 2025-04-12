package it.hurts.sskirillss.relics.api.relics.events;

import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

public abstract class RelicMaxLevelEvent extends RelicEvent {
    public RelicMaxLevelEvent(LivingEntity bearer, ItemStack stack) {
        super(bearer, stack);
    }

    @Data
    @ApiStatus.Experimental
    public static class Get extends RelicMaxLevelEvent {
        private int maxLevel;

        public Get(LivingEntity bearer, ItemStack stack, int maxLevel) {
            super(bearer, stack);

            this.maxLevel = maxLevel;
        }
    }
}