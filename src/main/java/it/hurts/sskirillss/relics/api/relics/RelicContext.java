package it.hurts.sskirillss.relics.api.relics;

import lombok.Data;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Holds contextual information about a relic being used or carried.
 * <p>
 * This class contains references to the entity carrying the relic, the relic item implementation, and the item stack representing the relic.
 * </p>
 *
 * <p>
 * This class is a data holder and is annotated with Lombok's {@code @Data}, which automatically generates getters, setters, equals/hashCode, and toString methods.
 * </p>
 */
@Data
public class RelicContext {
    /**
     * The entity currently bearing the relic.
     */
    private LivingEntity bearer;

    /**
     * The relic implementation associated with this context.
     */
    private IRelicItem relic;

    /**
     * The {@link ItemStack} instance representing the relic item.
     */
    private ItemStack stack;
}