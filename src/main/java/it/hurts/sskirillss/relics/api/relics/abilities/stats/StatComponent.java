package it.hurts.sskirillss.relics.api.relics.abilities.stats;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class StatComponent {
    private final Optional<Double> overrideValue;
    private final int initialQuality;

    public static final StatComponent EMPTY = new StatComponent(Optional.empty(), 0);

    public static final Codec<StatComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.lenientOptionalFieldOf("initialValue").forGetter(StatComponent::getOverrideValue), // TODO: Rename to "overrideValue" while porting to 1.22
                    Codec.INT.optionalFieldOf("initialQuality", 0).forGetter(StatComponent::getInitialQuality)
            ).apply(instance, StatComponent::new)
    );
}