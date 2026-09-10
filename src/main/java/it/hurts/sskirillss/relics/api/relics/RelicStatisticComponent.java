package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.utils.RelicsCodecs;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class RelicStatisticComponent {
    @Singular
    private final Map<String, RelicMetricComponent> metrics;

    public static final RelicStatisticComponent EMPTY = new RelicStatisticComponent(new HashMap<>());

    public static final Codec<RelicStatisticComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RelicsCodecs.unboundedMap(Codec.STRING, RelicMetricComponent.CODEC)
                            .optionalFieldOf("metrics", new HashMap<>())
                            .forGetter(RelicStatisticComponent::getMetrics)
            ).apply(instance, RelicStatisticComponent::new)
    );
}
