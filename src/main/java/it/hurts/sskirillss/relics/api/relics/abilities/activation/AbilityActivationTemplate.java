package it.hurts.sskirillss.relics.api.relics.abilities.activation;

import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AbilityActivationTemplate {
    public static final AbilityActivationTemplate EMPTY = AbilityActivationTemplate.builder().build();

    private final AbilityActivationType type;
    private final List<RelicContainer> containers;
    private final Map<String, AbilityActivationPredicateTemplate> predicates;

    public boolean isActive() {
        return this.type != AbilityActivationType.NONE;
    }

    public Map<String, AbilityActivationPredicateTemplate> getPredicates(AbilityActivationPredicateType type) {
        return this.predicates.values().stream()
                .filter(predicate -> predicate.getType() == type)
                .collect(Collectors.toMap(AbilityActivationPredicateTemplate::getId, predicate -> predicate, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public static AbilityActivationTemplateBuilder builder() {
        return new AbilityActivationTemplateBuilder();
    }

    public static AbilityActivationTemplateBuilder builder(AbilityActivationType type) {
        return builder().type(type);
    }

    public static class AbilityActivationTemplateBuilder {
        private AbilityActivationType type = AbilityActivationType.NONE;
        private List<RelicContainer> containers = new ArrayList<>();
        private Map<String, AbilityActivationPredicateTemplate> predicates = new LinkedHashMap<>();

        public AbilityActivationTemplateBuilder type(AbilityActivationType type) {
            this.type = type;

            return this;
        }

        public AbilityActivationTemplateBuilder containers(List<RelicContainer> containers) {
            this.containers = containers;

            return this;
        }

        public AbilityActivationTemplateBuilder container(RelicContainer... containers) {
            this.containers.addAll(Arrays.asList(containers));

            return this;
        }

        public AbilityActivationTemplateBuilder predicates(Map<String, AbilityActivationPredicateTemplate> predicates) {
            this.predicates = predicates;

            return this;
        }

        public AbilityActivationTemplateBuilder predicate(String id, AbilityActivationPredicateType type, Predicate<AbilityActivationPredicateContext> predicate) {
            this.predicates.put(id, new AbilityActivationPredicateTemplate(id, type, predicate));

            return this;
        }

        public AbilityActivationTemplate build() {
            return new AbilityActivationTemplate(this.type, this.containers, this.predicates);
        }
    }
}
