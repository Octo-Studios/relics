package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesComponent;
import lombok.*;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class RelicComponent {
    private final AbilitiesComponent abilities;
    private final LevelingComponent leveling;
    private final StatisticComponent statistic;

    public static final RelicComponent EMPTY = new RelicComponent(AbilitiesComponent.EMPTY, LevelingComponent.EMPTY, StatisticComponent.EMPTY);

    public static final Codec<RelicComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AbilitiesComponent.CODEC.fieldOf("abilities").forGetter(RelicComponent::getAbilities),
                    LevelingComponent.CODEC.fieldOf("leveling").forGetter(RelicComponent::getLeveling),
                    StatisticComponent.CODEC.fieldOf("statistic").forGetter(RelicComponent::getStatistic)
            ).apply(instance, RelicComponent::new)
    );
}