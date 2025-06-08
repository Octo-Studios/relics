package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class StatisticComponent {
    private final Map<String, MetricComponent> metrics;

    public static final StatisticComponent EMPTY = new StatisticComponent(new HashMap<>());

    public static final Codec<StatisticComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, MetricComponent.CODEC)
                            .fieldOf("metrics")
                            .forGetter(StatisticComponent::getMetrics)
            ).apply(instance, StatisticComponent::new)
    );
}