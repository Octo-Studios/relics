package it.hurts.sskirillss.relics.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;

@Builder(toBuilder = true)
public record LevelingComponent(int rank, int level, double experience, int points, int luck) {
    public static final LevelingComponent EMPTY = new LevelingComponent(0, 0, 0, 0, 0);

    public static final Codec<LevelingComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.INT.optionalFieldOf("rank", 0).forGetter(LevelingComponent::rank),
                            Codec.INT.optionalFieldOf("level", 0).forGetter(LevelingComponent::level),
                            Codec.DOUBLE.optionalFieldOf("experience", 0D).forGetter(LevelingComponent::experience),
                            Codec.INT.optionalFieldOf("points", 0).forGetter(LevelingComponent::points),
                            Codec.INT.optionalFieldOf("luck", 0).forGetter(LevelingComponent::luck))
                    .apply(instance, LevelingComponent::new)
    );
}