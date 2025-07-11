package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import it.hurts.sskirillss.relics.scaling_models.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ScalingModelRegistry {
    public static final DeferredRegister<ScalingModel> UPGRADE_OPERATIONS = DeferredRegister.create(RelicsRegistries.SCALING_MODEL_REGISTRY, Relics.MODID);

    /**
     * Adds a constant value per upgrade point: value + (modifier * points). Growth is linear and consistent across all levels. Best for simple, predictable upgrades that scale evenly.
     *
     * @implNote This implementation results in flat, evenly spaced increases, without scaling from the base value.
     * @see AdditiveScalingModel#evaluate
     */
    public static final Supplier<ScalingModel> ADDITIVE = UPGRADE_OPERATIONS.register("additive", AdditiveScalingModel::new);

    /**
     * Applies fast early growth that slows down as points increase: value + (1 - e^(-points)) * modifier. Excellent for upgrades that should have strong initial impact but diminish over time.
     *
     * @implNote This function asymptotically approaches value + modifier. The curve rises quickly at first, then gradually flattens out.
     * @see ExponentialDecayScalingModel#evaluate
     */
    public static final Supplier<ScalingModel> EXPONENTIAL_DECAY = UPGRADE_OPERATIONS.register("exponential_decay", ExponentialDecayScalingModel::new);

    /**
     * Scales using exponential saturation controlled by the modifier: value + modifier * (1 - e^(-points * modifier)). Grows very quickly at first, then saturates earlier or later depending on the modifier.
     *
     * @implNote Unlike standard exponential decay, this version uses the modifier in the exponent itself, allowing fine control over how early saturation occurs.
     * @see ExponentialSaturationScalingModel#evaluate
     */
    public static final Supplier<ScalingModel> EXPONENTIAL_SATURATION = UPGRADE_OPERATIONS.register("exponential_saturation", ExponentialSaturationScalingModel::new);

    /**
     * Applies full exponential scaling to the total value: value * (1 + modifier)^points. Growth is slow at first, but increases rapidly with each point. Ideal for high-impact upgrades or long-term investment.
     *
     * @implNote This is true exponential scaling — each additional point multiplies the result by a fixed growth factor (1 + modifier).
     * @see ExponentialScalingModel#evaluate
     */
    public static final Supplier<ScalingModel> EXPONENTIAL = UPGRADE_OPERATIONS.register("exponential", ExponentialScalingModel::new);

    /**
     * Applies slow, diminishing growth over time: value + log(points + 1) * modifier. Provides subtle, stable scaling that feels almost flat in the long term.
     *
     * @implNote Uses Math.log(points + 1) to prevent issues with log(0), and ensure consistent behavior from point 0 upward.
     * @see LogarithmicScalingModel#evaluate
     */
    public static final Supplier<ScalingModel> LOGARITHMIC = UPGRADE_OPERATIONS.register("logarithmic", LogarithmicScalingModel::new);

    /**
     * Adds a percentage of the base value for each point: value * (1 + modifier * points). Growth is linear, but scales proportionally to the starting value. Often used when upgrades should feel stronger for larger base values.
     *
     * @implNote Equivalent to adding (value * modifier) per point, but expressed as a single multiplicative formula.
     * @see MultiplicativeBaseScalingModel#evaluate
     */
    public static final Supplier<ScalingModel> MULTIPLICATIVE_BASE = UPGRADE_OPERATIONS.register("multiplicative_base", MultiplicativeBaseScalingModel::new);

    /**
     * Applies square root scaling: value + sqrt(points) * modifier. Fast growth at low point counts, but slows down steadily over time. Useful for upgrades that should give an early boost without runaway effects.
     *
     * @implNote Uses Math.sqrt to apply a sublinear growth curve that becomes less impactful with each additional point.
     * @see RadicalScalingModel#evaluate
     */
    public static final Supplier<ScalingModel> RADICAL = UPGRADE_OPERATIONS.register("radical", RadicalScalingModel::new);

    public static void register(IEventBus bus) {
        UPGRADE_OPERATIONS.register(bus);
    }
}