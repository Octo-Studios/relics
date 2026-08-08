package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public final class LevelingComponent {
    private final int rank;
    private final int level;
    private final double experience;
    private final int points;
    private final Map<String, Double> sourceExperience;

    public static final LevelingComponent EMPTY = new LevelingComponent(0, 0, 0, 0, new HashMap<>());

    public static final Codec<LevelingComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("rank", 0).forGetter(LevelingComponent::getRank),
                    Codec.INT.optionalFieldOf("level", 0).forGetter(LevelingComponent::getLevel),
                    Codec.DOUBLE.optionalFieldOf("experience", 0D).forGetter(LevelingComponent::getExperience),
                    Codec.INT.optionalFieldOf("points", 0).forGetter(LevelingComponent::getPoints),
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                            .optionalFieldOf("source_experience", new HashMap<>())
                            .forGetter(LevelingComponent::getSourceExperience)
            ).apply(instance, LevelingComponent::new)
    );
}
