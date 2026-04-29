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
public class RankModifierComponent {
    private final boolean enabled;

    public static final RankModifierComponent EMPTY = new RankModifierComponent(true);

    public static final Codec<RankModifierComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(RankModifierComponent::isEnabled)
            ).apply(instance, RankModifierComponent::new)
    );
}
