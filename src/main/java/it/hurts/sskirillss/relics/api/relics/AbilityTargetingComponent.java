package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class AbilityTargetingComponent {
    @Singular
    private final Map<String, Map<String, Boolean>> modes;

    public static final AbilityTargetingComponent EMPTY = new AbilityTargetingComponent(Map.of());

    public static final Codec<AbilityTargetingComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.unboundedMap(Codec.STRING, Codec.BOOL))
                            .optionalFieldOf("modes", Map.of())
                            .forGetter(AbilityTargetingComponent::getModes)
            ).apply(instance, AbilityTargetingComponent::new)
    );
}
