package it.hurts.sskirillss.relics.items.relics.base.data.leveling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class LevelingSourcesData {
    @Builder.Default
    private Map<String, LevelingSourceData> sources;

    public static class LevelingSourcesDataBuilder {
        private Map<String, LevelingSourceData> sources = new LinkedHashMap<>();

        public LevelingSourcesDataBuilder source(LevelingSourceData source) {
            sources.put(source.getId(), source);

            return this;
        }
    }
}