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

    public static ExperienceSourcesTemplateBuilder builder() {
        return new ExperienceSourcesTemplateBuilder();
    }

    public ExperienceSourcesTemplateBuilder toBuilder() {
        return new ExperienceSourcesTemplateBuilder(this);
    }

    @NoArgsConstructor
    public static class ExperienceSourcesTemplateBuilder {
        private LinkedHashMap<String, ExperienceSourceTemplate> sources = new LinkedHashMap<>();

        private ExperienceSourcesTemplateBuilder(ExperienceSourcesTemplate base) {
            this.sources = base.getSources();
        }

        public ExperienceSourcesTemplateBuilder source(ExperienceSourceTemplate source) {
            this.sources.put(source.getId(), source);

            return this;
        }

        @UnstableApi
        public ExperienceSourcesTemplateBuilder source(String source) {
            return this.source(ExperienceSourceTemplate.builder(source).build());
        }

        public ExperienceSourcesTemplate build() {
            return new ExperienceSourcesTemplate(this.sources);
        }
    }
}