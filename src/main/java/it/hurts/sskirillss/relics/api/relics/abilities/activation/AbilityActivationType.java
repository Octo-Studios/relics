package it.hurts.sskirillss.relics.api.relics.abilities.activation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

@Getter
@AllArgsConstructor
public enum AbilityActivationType {
    NONE(0),
    INSTANTANEOUS(1),
    INTERRUPTIBLE(2),
    CYCLICAL(3),
    TOGGLEABLE(4),
    CHARGEABLE(5),
    CYCLE_MODE(6);

    public static final IntFunction<AbilityActivationType> BY_ID = ByIdMap.continuous(AbilityActivationType::getId, AbilityActivationType.values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    private final int id;
}
