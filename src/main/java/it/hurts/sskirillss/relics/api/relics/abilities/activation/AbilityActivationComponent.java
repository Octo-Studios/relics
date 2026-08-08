package it.hurts.sskirillss.relics.api.relics.abilities.activation;

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
public class AbilityActivationComponent {
    private final int cooldown;
    private final int cooldownCap;
    private final boolean ticking;
    private final int charge;

    public static final AbilityActivationComponent EMPTY = new AbilityActivationComponent(0, 0, false, 0);

    public static final Codec<AbilityActivationComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("cooldown", 0).forGetter(AbilityActivationComponent::getCooldown),
                    Codec.INT.optionalFieldOf("cooldown_cap", 0).forGetter(AbilityActivationComponent::getCooldownCap),
                    Codec.BOOL.optionalFieldOf("ticking", false).forGetter(AbilityActivationComponent::isTicking),
                    Codec.INT.optionalFieldOf("charge", 0).forGetter(AbilityActivationComponent::getCharge)
            ).apply(instance, AbilityActivationComponent::new)
    );
}
