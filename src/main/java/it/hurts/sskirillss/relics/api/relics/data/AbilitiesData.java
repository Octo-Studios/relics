package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AbilitiesData {
    private final RelicData relicData;

    public AbilitiesData(RelicData relicData) {
        this.relicData = relicData;
    }

    public AbilitiesComponent getComponent() {
        return relicData.getComponent().getAbilities();
    }

    public void setComponent(AbilitiesComponent component) {
        relicData.setComponent(relicData.getComponent().toBuilder()
                .abilities(component)
                .build());
    }

    public AbilitiesTemplate getTemplate() {
        return relicData.getTemplate().getAbilities();
    }

    public Set<String> getAbilityIds() {
        return getTemplate().getAbilities().keySet();
    }

    public AbilityData getAbilityData(String ability) {
        if (!getTemplate().getAbilities().containsKey(ability))
            return null;

        return new AbilityData(relicData, ability);
    }

    public Map<String, AbilityData> getAbilityDataMap() {
        return getTemplate().getAbilities().keySet().stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> new AbilityData(relicData, id),
                        (o1, o2) -> o1,
                        LinkedHashMap::new
                ));
    }

    public Collection<AbilityData> getAbilities() {
        return getAbilityDataMap().values();
    }

    public boolean hasUnlockedUpgradeableAbility() {
        return getAbilityIds().stream().anyMatch(ability -> {
            var abilityData = getAbilityData(ability);
            return abilityData != null && abilityData.canBeUpgraded() && abilityData.isUnlocked();
        });
    }

    public boolean hasUnlockedAbility() {
        return getAbilityIds().stream().anyMatch(ability -> {
            var abilityData = getAbilityData(ability);
            return abilityData != null && abilityData.isUnlocked();
        });
    }
}
