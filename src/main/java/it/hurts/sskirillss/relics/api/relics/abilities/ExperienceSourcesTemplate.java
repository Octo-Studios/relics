package it.hurts.sskirillss.relics.api.relics.abilities;

import io.netty.util.internal.UnstableApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExperienceSourcesTemplate {
    private final LinkedHashMap<String, ExperienceSourceTemplate> sources;

    public static StatisticTemplateBuilder builder() {
        return new StatisticTemplateBuilder();
    }

    public StatisticTemplateBuilder toBuilder() {
        return new StatisticTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class StatisticTemplateBuilder {
        private LinkedHashMap<String, ExperienceSourceTemplate> sources = new LinkedHashMap<>();

        private StatisticTemplateBuilder(ExperienceSourcesTemplate base) {
            this.sources = base.getSources();
        }

        public StatisticTemplateBuilder source(ExperienceSourceTemplate source) {
            this.sources.put(source.getId(), source);

            return this;
        }

        @UnstableApi
        public StatisticTemplateBuilder source(String source) {
            return this.source(ExperienceSourceTemplate.builder(source).build());
        }

        public ExperienceSourcesTemplate build() {
            return new ExperienceSourcesTemplate(this.sources);
        }
    }
}