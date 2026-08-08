package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class PlayerAbilityTargetingComponent {
    @Singular
    private final Map<String, AbilityTargetingComponent> abilities;

    public static final PlayerAbilityTargetingComponent EMPTY = new PlayerAbilityTargetingComponent(new HashMap<>());

    public static final Codec<PlayerAbilityTargetingComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, AbilityTargetingComponent.CODEC)
                            .optionalFieldOf("abilities", Map.of())
                            .forGetter(PlayerAbilityTargetingComponent::getAbilities)
            ).apply(instance, PlayerAbilityTargetingComponent::new)
    );
}
