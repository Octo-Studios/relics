package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class OptionsComponent {
    private final boolean visuallyFlawless;

    public static final OptionsComponent EMPTY = new OptionsComponent(true);

    public static final Codec<OptionsComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("visually_flawless", true).forGetter(OptionsComponent::isVisuallyFlawless)
            ).apply(instance, OptionsComponent::new)
    );
}
