package it.hurts.sskirillss.relics.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class StatComponent {
    private final double initialValue;

    public static final StatComponent EMPTY = new StatComponent(0D);

    public static final Codec<StatComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("initialValue").forGetter(StatComponent::getInitialValue)
            ).apply(instance, StatComponent::new)
    );
}