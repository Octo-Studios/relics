package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AbilityMetricComponent {
    private final double value;

    public static final AbilityMetricComponent EMPTY = new AbilityMetricComponent(0D);

    public static final Codec<AbilityMetricComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.optionalFieldOf("value", 0D).forGetter(AbilityMetricComponent::getValue)
            ).apply(instance, AbilityMetricComponent::new)
    );
}