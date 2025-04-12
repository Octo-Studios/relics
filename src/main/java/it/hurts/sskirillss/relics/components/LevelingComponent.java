package it.hurts.sskirillss.relics.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public final class LevelingComponent {
    private final int rank;
    private final int level;
    private final double experience;
    private final int points;
    private final int luck;

    public static final LevelingComponent EMPTY = new LevelingComponent(0, 0, 0, 0, 0);

    public static final Codec<LevelingComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("rank", 0).forGetter(LevelingComponent::getRank),
                    Codec.INT.optionalFieldOf("level", 0).forGetter(LevelingComponent::getLevel),
                    Codec.DOUBLE.optionalFieldOf("experience", 0D).forGetter(LevelingComponent::getExperience),
                    Codec.INT.optionalFieldOf("points", 0).forGetter(LevelingComponent::getPoints),
                    Codec.INT.optionalFieldOf("luck", 0).forGetter(LevelingComponent::getLuck)
            ).apply(instance, LevelingComponent::new)
    );
}