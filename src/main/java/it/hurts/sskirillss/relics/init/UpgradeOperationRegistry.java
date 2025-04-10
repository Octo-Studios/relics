package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.UpgradeOperation;
import it.hurts.sskirillss.relics.upgrade_operations.*;
import it.hurts.sskirillss.relics.utils.Reference;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class UpgradeOperationRegistry {
    public static final DeferredRegister<UpgradeOperation> UPGRADE_OPERATIONS = DeferredRegister.create(RegistryRegistry.UPGRADE_OPERATION_REGISTRY, Reference.MODID);

    /**
     * Adds a constant value per upgrade point: value + (modifier * points). Growth is linear and consistent across all levels. Best for simple, predictable upgrades that scale evenly.
     *
     * @implNote This implementation results in flat, evenly spaced increases, without scaling from the base value.
     * @see AdditiveUpgradeOperation#apply
     */
    public static final Supplier<UpgradeOperation> ADDITIVE = UPGRADE_OPERATIONS.register("additive", AdditiveUpgradeOperation::new);

    /**
     * Applies fast early growth that slows down as points increase: value + (1 - e^(-points)) * modifier. Excellent for upgrades that should have strong initial impact but diminish over time.
     *
     * @implNote This function asymptotically approaches value + modifier. The curve rises quickly at first, then gradually flattens out.
     * @see ExponentialDecayUpgradeOperation#apply
     */
    public static final Supplier<UpgradeOperation> EXPONENTIAL_DECAY = UPGRADE_OPERATIONS.register("exponential_decay", ExponentialDecayUpgradeOperation::new);

    /**
     * Scales using exponential saturation controlled by the modifier: value + modifier * (1 - e^(-points * modifier)). Grows very quickly at first, then saturates earlier or later depending on the modifier.
     *
     * @implNote Unlike standard exponential decay, this version uses the modifier in the exponent itself, allowing fine control over how early saturation occurs.
     * @see ExponentialSaturationUpgradeOperation#apply
     */
    public static final Supplier<UpgradeOperation> EXPONENTIAL_SATURATION = UPGRADE_OPERATIONS.register("exponential_saturation", ExponentialSaturationUpgradeOperation::new);

    /**
     * Applies full exponential scaling to the total value: value * (1 + modifier)^points. Growth is slow at first, but increases rapidly with each point. Ideal for high-impact upgrades or long-term investment.
     *
     * @implNote This is true exponential scaling — each additional point multiplies the result by a fixed growth factor (1 + modifier).
     * @see ExponentialUpgradeOperation#apply
     */
    public static final Supplier<UpgradeOperation> EXPONENTIAL = UPGRADE_OPERATIONS.register("exponential", ExponentialUpgradeOperation::new);

    /**
     * Applies slow, diminishing growth over time: value + log(points + 1) * modifier. Provides subtle, stable scaling that feels almost flat in the long term.
     *
     * @implNote Uses Math.log(points + 1) to prevent issues with log(0), and ensure consistent behavior from point 0 upward.
     * @see LogarithmicUpgradeOperation#apply
     */
    public static final Supplier<UpgradeOperation> LOGARITHMIC = UPGRADE_OPERATIONS.register("logarithmic", LogarithmicUpgradeOperation::new);

    /**
     * Adds a percentage of the base value for each point: value * (1 + modifier * points). Growth is linear, but scales proportionally to the starting value. Often used when upgrades should feel stronger for larger base values.
     *
     * @implNote Equivalent to adding (value * modifier) per point, but expressed as a single multiplicative formula.
     * @see MultiplicativeBaseUpgradeOperation#apply 
     */
    public static final Supplier<UpgradeOperation> MULTIPLICATIVE_BASE = UPGRADE_OPERATIONS.register("multiplicative_base", MultiplicativeBaseUpgradeOperation::new);

    /**
     * Applies square root scaling: value + sqrt(points) * modifier. Fast growth at low point counts, but slows down steadily over time. Useful for upgrades that should give an early boost without runaway effects.
     *
     * @implNote Uses Math.sqrt to apply a sublinear growth curve that becomes less impactful with each additional point.
     * @see RadicalUpgradeOperation#apply 
     */
    public static final Supplier<UpgradeOperation> RADICAL = UPGRADE_OPERATIONS.register("radical", RadicalUpgradeOperation::new);

    public static void register(IEventBus bus) {
        UPGRADE_OPERATIONS.register(bus);
    }
}