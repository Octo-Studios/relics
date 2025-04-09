package it.hurts.sskirillss.relics.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Builder;

@Builder(toBuilder = true)
public record LevelingComponent(int rank, int level, int experience, int points, int luck) {
    public static final LevelingComponent EMPTY = new LevelingComponent(0, 0, 0, 0, 0);

    public static final Codec<LevelingComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.INT.optionalFieldOf("rank", 0).forGetter(LevelingComponent::rank),
                            Codec.INT.fieldOf("level").forGetter(LevelingComponent::level),
                            Codec.INT.fieldOf("experience").forGetter(LevelingComponent::experience),
                            Codec.INT.fieldOf("points").forGetter(LevelingComponent::points),
                            Codec.INT.fieldOf("luck").forGetter(LevelingComponent::luck))
                    .apply(instance, LevelingComponent::new)
    );
}