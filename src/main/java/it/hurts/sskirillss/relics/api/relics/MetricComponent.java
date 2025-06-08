package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class MetricComponent {
    private final double value;

    public static final MetricComponent EMPTY = new MetricComponent(0D);

    public static final Codec<MetricComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.optionalFieldOf("value", 0D).forGetter(MetricComponent::getValue)
            ).apply(instance, MetricComponent::new)
    );
}