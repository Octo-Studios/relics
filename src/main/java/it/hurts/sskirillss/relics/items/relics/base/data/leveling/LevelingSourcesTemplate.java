package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemColor;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.GemShape;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class LevelingSourcesTemplate {
    @Builder.Default
    private Map<String, LevelingSourceTemplate> sources;

    public static class LevelingSourcesTemplateBuilder {
        private Map<String, LevelingSourceTemplate> sources = new LinkedHashMap<>();

        // TODO: Replace static init with registry entry
        {
            var entry = LevelingSourceTemplate.genericBuilder("spreading")
                    .initialValue(25)
                    .gem(GemShape.OVAL, GemColor.YELLOW)
                    .build();

            sources.put(entry.getId(), entry);
        }

        public LevelingSourcesTemplateBuilder source(LevelingSourceTemplate source) {
            sources.put(source.getId(), source);

            return this;
        }
    }
}