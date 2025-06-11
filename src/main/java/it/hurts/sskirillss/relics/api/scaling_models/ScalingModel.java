package it.hurts.sskirillss.relics.api.scaling_models;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public abstract class ScalingModel {
    /**
     * Evaluates the result of a progression or transformation operation based on an initial value, a modifier, and a number of iterations.
     * <p>
     * This method defines how a value evolves through repeated application of a modifier (e.g. for stat scaling, experience growth, cost increases, etc.), according to a specific mathematical model such as additive, exponential, logarithmic, etc.
     *
     * @param entity     The bearer of the relic. Used to provide context for the evaluation.
     * @param stack      The stack instance associated with the {@link IRelicItem}. Used to provide context for the evaluation.
     * @param baseValue  The initial value before any operations are applied.
     * @param modifier   The change applied per iteration, representing the strength or weight of each step.
     * @param iterations The number of times the operation should be applied.
     * @return The resulting value after applying the operation the specified number of times.
     */
    public abstract double evaluate(LivingEntity entity, ItemStack stack, double baseValue, double modifier, int iterations);
}