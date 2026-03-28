package it.hurts.sskirillss.relics.api.relics.synergies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticComponent;
import it.hurts.sskirillss.relics.api.relics.LockComponent;
import it.hurts.sskirillss.relics.api.relics.ResearchComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatComponent;
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
public class SynergyComponent {
    private final String mode;

    public static final SynergyComponent EMPTY = new SynergyComponent("");

    public static final Codec<SynergyComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("mode", "").forGetter(SynergyComponent::getMode)
            ).apply(instance, SynergyComponent::new)
    );
}
