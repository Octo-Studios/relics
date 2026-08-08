package it.hurts.sskirillss.relics.api.relics.abilities.activation;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.Predicate;

@Data
@AllArgsConstructor
public class AbilityActivationPredicateTemplate {
    private final String id;
    private final AbilityActivationPredicateType type;
    private final Predicate<AbilityActivationPredicateContext> predicate;

    public boolean test(AbilityActivationPredicateContext context) {
        return this.predicate.test(context);
    }
}
