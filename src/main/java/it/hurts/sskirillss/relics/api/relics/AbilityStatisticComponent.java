package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AbilityStatisticComponent {
    @Singular
    private final Map<String, AbilityMetricComponent> metrics;

    public static final AbilityStatisticComponent EMPTY = new AbilityStatisticComponent(new HashMap<>());

    public static final Codec<AbilityStatisticComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, AbilityMetricComponent.CODEC)
                            .optionalFieldOf("metrics", new HashMap<>())
                            .forGetter(AbilityStatisticComponent::getMetrics)
            ).apply(instance, AbilityStatisticComponent::new)
    );
}