package it.hurts.sskirillss.relics.api.relics.abilities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.LockComponent;
import it.hurts.sskirillss.relics.api.relics.RankModifierComponent;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticComponent;
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
public class AbilityComponent {
    @Singular
    private final Map<String, StatComponent> stats;
    private final LockComponent lock;
    private final AbilityStatisticComponent statistic;
    private final String mode;
    @Singular
    private final Map<String, RankModifierComponent> rankModifiers;
    private final int points;

    public static final AbilityComponent EMPTY = new AbilityComponent(Map.of(), LockComponent.EMPTY, AbilityStatisticComponent.EMPTY, "", Map.of(), 0);

    public static final Codec<AbilityComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, StatComponent.CODEC).fieldOf("stats").forGetter(AbilityComponent::getStats),
                    LockComponent.CODEC.fieldOf("lock").forGetter(AbilityComponent::getLock),
                    AbilityStatisticComponent.CODEC.optionalFieldOf("statistic", AbilityStatisticComponent.EMPTY).forGetter(AbilityComponent::getStatistic),
                    Codec.STRING.optionalFieldOf("mode", "").forGetter(AbilityComponent::getMode),
                    Codec.unboundedMap(Codec.STRING, RankModifierComponent.CODEC).optionalFieldOf("rank_modifiers", Map.of()).forGetter(AbilityComponent::getRankModifiers),
                    Codec.INT.fieldOf("points").forGetter(AbilityComponent::getPoints)
            ).apply(instance, AbilityComponent::new)
    );
}
