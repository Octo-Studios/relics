package it.hurts.sskirillss.relics.api.relics.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AbilitiesComponent {
    @Singular
    private final Map<String, AbilityComponent> abilities;

    public static final AbilitiesComponent EMPTY = new AbilitiesComponent(Map.of());

    public static final Codec<AbilitiesComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, AbilityComponent.CODEC)
                            .fieldOf("abilities")
                            .forGetter(AbilitiesComponent::getAbilities)
            ).apply(instance, AbilitiesComponent::new)
    );
}