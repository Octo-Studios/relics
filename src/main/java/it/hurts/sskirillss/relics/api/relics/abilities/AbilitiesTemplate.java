package it.hurts.sskirillss.relics.api.relics.abilities;

import it.hurts.sskirillss.relics.config.data.AbilitiesConfigData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilitiesTemplate {
    private final Map<String, AbilityTemplate> abilities;

    public static AbilitiesTemplateBuilder builder() {
        return new AbilitiesTemplateBuilder();
    }

    public AbilitiesTemplateBuilder toBuilder() {
        return new AbilitiesTemplateBuilder(this);
    }

    public AbilitiesConfigData toConfigData() {
        return new AbilitiesConfigData(abilities.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toConfigData(), (o1, o2) -> o1, LinkedHashMap::new)));
    }

    @NoArgsConstructor
    public static class AbilitiesTemplateBuilder {
        private Map<String, AbilityTemplate> abilities = new LinkedHashMap<>();

        private AbilitiesTemplateBuilder(AbilitiesTemplate base) {
            this.abilities = new LinkedHashMap<>(base.getAbilities());
        }

        public AbilitiesTemplateBuilder abilities(Map<String, AbilityTemplate> abilities) {
            this.abilities = abilities;

            return this;
        }

        public AbilitiesTemplateBuilder ability(AbilityTemplate ability) {
            abilities.put(ability.getId(), ability);

            return this;
        }

        public AbilitiesTemplate build() {
            return new AbilitiesTemplate(abilities);
        }
    }
}