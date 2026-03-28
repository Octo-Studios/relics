package it.hurts.sskirillss.relics.api.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ResearchComponent {
    private final Map<String, List<Integer>> links;
    private final boolean researched;

    public static final ResearchComponent EMPTY = new ResearchComponent(new HashMap<>(), false);

    public static final Codec<ResearchComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.list(Codec.INT))
                            .fieldOf("links")
                            .forGetter(ResearchComponent::getLinks),
                    Codec.BOOL
                            .fieldOf("researched")
                            .forGetter(ResearchComponent::isResearched)
            ).apply(instance, ResearchComponent::new)
    );
}
