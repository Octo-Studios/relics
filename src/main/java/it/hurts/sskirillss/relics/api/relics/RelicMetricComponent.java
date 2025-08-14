package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class RelicMetricComponent {
    private final double value;

    public static final RelicMetricComponent EMPTY = new RelicMetricComponent(0D);

    public static final Codec<RelicMetricComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.optionalFieldOf("value", 0D).forGetter(RelicMetricComponent::getValue)
            ).apply(instance, RelicMetricComponent::new)
    );
}