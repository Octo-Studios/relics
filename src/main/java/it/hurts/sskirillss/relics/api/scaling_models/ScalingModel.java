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

    public double calculateModifier(LivingEntity entity, ItemStack stack, double baseValue, double targetValue, int iterations) {
        if (iterations <= 0 || baseValue == targetValue)
            return 0D;

        var direction = targetValue > baseValue ? 1D : -1D;
        var low = direction > 0 ? 0D : -1D;
        var high = direction > 0 ? 1D : 0D;

        for (int i = 0; i < 64; i++) {
            var value = evaluate(entity, stack, baseValue, high, iterations);

            if ((direction > 0 && value >= targetValue) || (direction < 0 && value <= targetValue))
                break;

            if (direction > 0)
                high *= 2D;
            else
                low = low * 2D - 1D;
        }

        for (int i = 0; i < 96; i++) {
            var mid = (low + high) / 2D;
            var value = evaluate(entity, stack, baseValue, mid, iterations);

            if (direction > 0) {
                if (value < targetValue)
                    low = mid;
                else
                    high = mid;
            } else {
                if (value > targetValue)
                    high = mid;
                else
                    low = mid;
            }
        }

        return (low + high) / 2D;
    }
}
