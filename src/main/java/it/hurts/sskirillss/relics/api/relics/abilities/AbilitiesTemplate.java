package it.hurts.sskirillss.relics.api.relics.abilities;

import it.hurts.sskirillss.relics.config.data.AbilitiesConfigData;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class AbilitiesTemplate {
    @Builder.Default
    private Map<String, AbilityTemplate> abilities;

    public AbilitiesConfigData toConfigData() {
        return new AbilitiesConfigData(abilities.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toConfigData(), (o1, o2) -> o1, LinkedHashMap::new)));
    }

    public static class AbilitiesTemplateBuilder {
        private Map<String, AbilityTemplate> abilities = new LinkedHashMap<>();

        public AbilitiesTemplateBuilder ability(AbilityTemplate ability) {
            abilities.put(ability.getId(), ability);

            return this;
        }
    }
}