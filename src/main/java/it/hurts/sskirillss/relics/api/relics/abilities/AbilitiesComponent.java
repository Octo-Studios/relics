package it.hurts.sskirillss.relics.api.relics.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyComponent;
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
public class AbilitiesComponent {
    @Singular
    private final Map<String, AbilityComponent> abilities;
    @Singular
    private final Map<String, SynergyComponent> synergies;

    public static final AbilitiesComponent EMPTY = new AbilitiesComponent(Map.of(), Map.of());

    public static final Codec<AbilitiesComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, AbilityComponent.CODEC)
                            .fieldOf("abilities")
                            .forGetter(AbilitiesComponent::getAbilities),
                    Codec.unboundedMap(Codec.STRING, SynergyComponent.CODEC)
                            .fieldOf("synergies")
                            .forGetter(AbilitiesComponent::getSynergies)
            ).apply(instance, AbilitiesComponent::new)
    );
}
