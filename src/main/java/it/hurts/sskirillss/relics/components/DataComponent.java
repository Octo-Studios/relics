package it.hurts.sskirillss.relics.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class DataComponent {
    private final AbilitiesComponent abilities;
    private final LevelingComponent leveling;

    public static final DataComponent EMPTY = new DataComponent(AbilitiesComponent.EMPTY, LevelingComponent.EMPTY);

    public static final Codec<DataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AbilitiesComponent.CODEC.fieldOf("abilities").forGetter(DataComponent::getAbilities),
                    LevelingComponent.CODEC.fieldOf("leveling").forGetter(DataComponent::getLeveling)
            ).apply(instance, DataComponent::new)
    );
}