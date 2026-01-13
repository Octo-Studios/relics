package it.hurts.sskirillss.relics.api.relics.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.LockComponent;
import it.hurts.sskirillss.relics.api.relics.ResearchComponent;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatComponent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class AbilityComponent {
    @Singular
    private final Map<String, StatComponent> stats;
    private final ResearchComponent research;
    private final LockComponent lock;
    private final AbilityStatisticComponent statistic;
    private final String mode;
    private final int points;

    public static final AbilityComponent EMPTY = new AbilityComponent(Map.of(), ResearchComponent.EMPTY, LockComponent.EMPTY, AbilityStatisticComponent.EMPTY, "", 0);

    public static final Codec<AbilityComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, StatComponent.CODEC).fieldOf("stats").forGetter(AbilityComponent::getStats),
                    ResearchComponent.CODEC.fieldOf("research").forGetter(AbilityComponent::getResearch),
                    LockComponent.CODEC.fieldOf("lock").forGetter(AbilityComponent::getLock),
                    AbilityStatisticComponent.CODEC.optionalFieldOf("statistic", AbilityStatisticComponent.EMPTY).forGetter(AbilityComponent::getStatistic),
                    Codec.STRING.optionalFieldOf("mode", "").forGetter(AbilityComponent::getMode),
                    Codec.INT.fieldOf("points").forGetter(AbilityComponent::getPoints)
            ).apply(instance, AbilityComponent::new)
    );
}