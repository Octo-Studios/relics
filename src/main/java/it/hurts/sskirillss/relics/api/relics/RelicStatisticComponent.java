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
public class RelicStatisticComponent {
    @Singular
    private final Map<String, RelicMetricComponent> metrics;

    public static final RelicStatisticComponent EMPTY = new RelicStatisticComponent(new HashMap<>());

    public static final Codec<RelicStatisticComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, RelicMetricComponent.CODEC)
                            .optionalFieldOf("metrics", new HashMap<>())
                            .forGetter(RelicStatisticComponent::getMetrics)
            ).apply(instance, RelicStatisticComponent::new)
    );
}