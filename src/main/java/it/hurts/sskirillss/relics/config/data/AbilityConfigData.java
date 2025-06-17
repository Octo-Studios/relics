package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.module.config.annotation.Prop;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbilityConfigData {
    @Prop(comment = "Number of leveling points needed to increase the ability level")
    private int requiredPoints;
    @Prop(comment = "Relic level at which the ability becomes unlocked")
    private int requiredLevel;
    @Prop(comment = "Highest level to which the ability can be upgraded")
    private int maxLevel;

    private Map<String, StatConfigData> stats = new LinkedHashMap<>();

    public AbilityTemplate toData(IRelicItem relic, String ability) {
        var template = relic.getDefaultAbilityTemplate(ability);

        return template.toBuilder()
                .requiredPoints(requiredPoints)
                .requiredLevel(requiredLevel)
                .maxLevel(maxLevel)
                .stats(template.getStats().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> stats.get(entry.getKey()).toData(relic, ability, entry.getKey()), (o1, o2) -> o1, LinkedHashMap::new)))
                .build();
    }
}