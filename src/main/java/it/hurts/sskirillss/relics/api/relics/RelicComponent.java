package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesComponent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class RelicComponent {
    private final AbilitiesComponent abilities;
    private final LevelingComponent leveling;
    private final RelicStatisticComponent statistic;
    private final OptionsComponent options;

    public static final RelicComponent EMPTY = new RelicComponent(AbilitiesComponent.EMPTY, LevelingComponent.EMPTY, RelicStatisticComponent.EMPTY, OptionsComponent.EMPTY);

    public static final Codec<RelicComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AbilitiesComponent.CODEC.fieldOf("abilities").forGetter(RelicComponent::getAbilities),
                    LevelingComponent.CODEC.fieldOf("leveling").forGetter(RelicComponent::getLeveling),
                    RelicStatisticComponent.CODEC.fieldOf("statistic").forGetter(RelicComponent::getStatistic),
                    OptionsComponent.CODEC.optionalFieldOf("options", OptionsComponent.EMPTY).forGetter(RelicComponent::getOptions)
            ).apply(instance, RelicComponent::new)
    );
}
