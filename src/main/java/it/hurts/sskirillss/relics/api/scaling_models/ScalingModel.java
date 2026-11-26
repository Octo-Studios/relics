package it.hurts.sskirillss.relics.api.scaling_models;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public abstract class ScalingModel {
    /**
     * Evaluates the scaled value at a given point in the progression.
     * <p>
     * Fractional iterations represent progress between whole steps on the model's curve.
     *
     * @param entity     The bearer of the relic.
     * @param stack      The relic stack being evaluated.
     * @param baseValue  The initial value at zero iterations.
     * @param modifier   The parameter that controls how the value changes along the curve.
     * @param iterations The number of progression steps reached, including any fractional step.
     * @return The scaled value at the given number of iterations.
     */
    public abstract double evaluate(LivingEntity entity, ItemStack stack, double baseValue, double modifier, double iterations);

    /**
     * Calculates the modifier needed to reach the target value from the base value after the given number of iterations.
     *
     * @param entity      The bearer of the relic.
     * @param stack       The relic stack being evaluated.
     * @param baseValue   The initial value at zero iterations.
     * @param targetValue The desired value at the end of the progression.
     * @param iterations  The total number of whole progression steps.
     * @return The calculated modifier, or zero if the iteration count is nonpositive or the target equals the base value.
     * @implNote The default implementation uses binary search and assumes that the result of {@link #evaluate} increases
     * with the modifier over the search interval. Models with a known inverse can calculate the modifier directly.
     */
    public double calculateModifier(LivingEntity entity, ItemStack stack, double baseValue, double targetValue, int iterations) {
        if (iterations <= 0 || baseValue == targetValue)
            return 0D;

        var direction = targetValue > baseValue ? 1D : -1D;
        var low = 0D;
        var high = 1D;

        for (var i = 0; i < 64; i++) {
            var value = evaluate(entity, stack, baseValue, direction * high, iterations);

            if (direction * (value - targetValue) >= 0D)
                break;

            high *= 2D;
        }

        for (var i = 0; i < 96; i++) {
            var mid = (low + high) / 2D;
            var value = evaluate(entity, stack, baseValue, direction * mid, iterations);

            if (direction * (value - targetValue) < 0D)
                low = mid;
            else
                high = mid;
        }

        return direction * ((low + high) / 2D);
    }
}
