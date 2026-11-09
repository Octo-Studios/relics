package it.hurts.sskirillss.relics.api.relics.synergies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.RankModifierComponent;
import it.hurts.sskirillss.relics.utils.RelicsCodecs;
import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class SynergyComponent {
    private final String mode;
    @Singular
    private final Map<String, RankModifierComponent> rankModifiers;

    public static final SynergyComponent EMPTY = new SynergyComponent("", Map.of());

    public static final Codec<SynergyComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("mode", "").forGetter(SynergyComponent::getMode),
                    RelicsCodecs.unboundedMap(Codec.STRING, RankModifierComponent.CODEC).optionalFieldOf("rank_modifiers", Map.of()).forGetter(SynergyComponent::getRankModifiers)
            ).apply(instance, SynergyComponent::new)
    );
}
