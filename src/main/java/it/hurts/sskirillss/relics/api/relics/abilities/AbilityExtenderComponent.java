package it.hurts.sskirillss.relics.api.relics.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AbilityExtenderComponent {
    private final int cooldownCap;
    private final int cooldown;
    private final boolean ticking;

    public static final AbilityExtenderComponent EMPTY = new AbilityExtenderComponent(0, 0, false);

    public static final Codec<AbilityExtenderComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("cooldownCap").forGetter(AbilityExtenderComponent::getCooldownCap),
                    Codec.INT.fieldOf("cooldown").forGetter(AbilityExtenderComponent::getCooldown),
                    Codec.BOOL.fieldOf("ticking").forGetter(AbilityExtenderComponent::isTicking)
            ).apply(instance, AbilityExtenderComponent::new)
    );
}