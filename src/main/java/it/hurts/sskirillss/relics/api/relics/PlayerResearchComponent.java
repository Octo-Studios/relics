package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.utils.RelicsCodecs;
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
public class PlayerResearchComponent {
    private final Map<String, ResearchComponent> research;

    public static final PlayerResearchComponent EMPTY = new PlayerResearchComponent(new HashMap<>());

    public static final Codec<PlayerResearchComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RelicsCodecs.unboundedMap(Codec.STRING, ResearchComponent.CODEC)
                            .optionalFieldOf("research", Map.of())
                            .forGetter(PlayerResearchComponent::getResearch)
            ).apply(instance, PlayerResearchComponent::new)
    );
}
