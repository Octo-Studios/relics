package it.hurts.sskirillss.relics.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AbilityComponent {
    private final Map<String, StatComponent> stats;
    private final ResearchComponent research;
    private final LockComponent lock;
    private final AbilityExtenderComponent extender;
    private final int points;

    public static final AbilityComponent EMPTY = new AbilityComponent(Map.of(), ResearchComponent.EMPTY, LockComponent.EMPTY, AbilityExtenderComponent.EMPTY, 0);

    public static final Codec<AbilityComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, StatComponent.CODEC).fieldOf("stats").forGetter(AbilityComponent::getStats),
                    ResearchComponent.CODEC.fieldOf("research").forGetter(AbilityComponent::getResearch),
                    LockComponent.CODEC.fieldOf("lock").forGetter(AbilityComponent::getLock),
                    AbilityExtenderComponent.CODEC.fieldOf("extender").forGetter(AbilityComponent::getExtender),
                    Codec.INT.fieldOf("points").forGetter(AbilityComponent::getPoints)
            ).apply(instance, AbilityComponent::new)
    );
}