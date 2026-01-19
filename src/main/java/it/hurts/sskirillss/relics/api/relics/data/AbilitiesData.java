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

    public RelicData getRelicData() {
        return relicData;
    }

    public AbilitiesComponent getComponent() {
        return this.relicData.getComponent().getAbilities();
    }

    public void setComponent(AbilitiesComponent component) {
        this.relicData.setComponent(this.relicData.getComponent().toBuilder()
                .abilities(component)
                .build());
    }

    public AbilitiesTemplate getTemplate() {
        return this.relicData.getTemplate().getAbilities();
    }

    public Set<String> getAbilityIds() {
        return this.getTemplate().getAbilities().keySet();
    }

    public AbilityData getAbilityData(String ability) {
        if (!this.getTemplate().getAbilities().containsKey(ability))
            return null;

        return new AbilityData(this, ability);
    }

    public Map<String, AbilityData> getAbilityDataMap() {
        return this.getTemplate().getAbilities().keySet().stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> new AbilityData(this, id),
                        (o1, o2) -> o1,
                        LinkedHashMap::new
                ));
    }

    public Collection<AbilityData> getAbilities() {
        return this.getAbilityDataMap().values();
    }

    public Set<String> getSynergyIds() {
        return this.getTemplate().getSynergies().keySet();
    }

    public SynergyData getSynergyData(String synergy) {
        if (!this.getTemplate().getSynergies().containsKey(synergy))
            return null;

        return new SynergyData(this, synergy);
    }

    public Map<String, SynergyData> getSynergyDataMap() {
        return this.getTemplate().getSynergies().keySet().stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> new SynergyData(this, id),
                        (o1, o2) -> o1,
                        LinkedHashMap::new
                ));
    }

    public Collection<SynergyData> getSynergies() {
        return this.getSynergyDataMap().values();
    }

    public boolean hasUnlockedUpgradeableAbility() {
        return this.getAbilityIds().stream().anyMatch(ability -> {
            var abilityData = getAbilityData(ability);

            return abilityData != null && abilityData.canBeUpgraded() && abilityData.isUnlocked();
        });
    }

    public boolean hasUnlockedAbility() {
        return this.getAbilityIds().stream().anyMatch(ability -> {
            var abilityData = getAbilityData(ability);

            return abilityData != null && abilityData.isUnlocked();
        });
    }
}
