package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ResearchComponent {
    private final Map<String, List<Integer>> links;
    private final boolean researched;
    private final boolean lockOpen;

    public static final ResearchComponent EMPTY = new ResearchComponent(Map.of(), false, false);

    public static final Codec<ResearchComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.list(Codec.INT))
                            .optionalFieldOf("links", Map.of())
                            .forGetter(ResearchComponent::getLinks),
                    Codec.BOOL
                            .optionalFieldOf("researched", false)
                            .forGetter(ResearchComponent::isResearched),
                    Codec.BOOL.optionalFieldOf("lock_open", false).forGetter(ResearchComponent::isLockOpen)
            ).apply(instance, ResearchComponent::new)
    );
}
