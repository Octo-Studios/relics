package it.hurts.sskirillss.relics.api.relics.abilities.activation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

@Getter
@AllArgsConstructor
public enum AbilityActivationStage {
    START(0),
    TICK(1),
    END(2);

    public static final IntFunction<AbilityActivationStage> BY_ID = ByIdMap.continuous(AbilityActivationStage::getId, AbilityActivationStage.values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    private final int id;
}
