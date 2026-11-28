package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyComponent;
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.AbilityConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.RelicConditionTemplate;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SynergyData {
    private final AbilitiesData abilitiesData;
    private final String synergy;

    public SynergyData(AbilitiesData abilitiesData, String synergy) {
        this.abilitiesData = abilitiesData;
        this.synergy = synergy;
    }

    public AbilitiesData getAbilitiesData() {
        return this.abilitiesData;
    }

    public String getId() {
        return this.synergy;
    }

    public SynergyTemplate getTemplate() {
        return this.getAbilitiesData().getTemplate().getSynergies().get(this.getId());
    }

    public SynergyStatData getStatData(String stat) {
        return new SynergyStatData(this, stat);
    }

    public SynergyTargetingData getTargetingData() {
        return new SynergyTargetingData(this);
    }

    public double getProgress() {
        var template = getTemplate();

        if (template == null)
            return 0D;

        var conditions = template.getRelicConditions();

        if (conditions.isEmpty())
            return 0D;

        var entity = this.getAbilitiesData().getRelicData().getEntity();

        if (entity == null)
            return 0D;

        var totalAbilities = conditions.stream()
                .mapToInt(condition -> condition.getAbilityConditions().size())
                .sum();

        if (totalAbilities == 0)
            return 0D;

        var progressSum = 0D;

        for (var condition : conditions) {
            var abilityConditions = condition.getAbilityConditions();

            if (abilityConditions.isEmpty())
                continue;

            var relic = condition.getRelic().get();

            if (relic == null)
                continue;

            var relicStacks = this.findRelicStacks(relic, condition.getRelicContainers());

            if (relicStacks.isEmpty())
                continue;

            var bestProgress = 0D;

            for (var stack : relicStacks) {
                var relicData = relic.getRelicData(entity, stack);
                if (!meetsConditions(relicData, condition))
                    continue;

                var conditionAbilitiesData = relicData.getAbilitiesData();
                var stackProgress = 0D;

                for (var abilityCondition : abilityConditions) {
                    var abilityData = conditionAbilitiesData.getAbilityData(abilityCondition.getId());

                    stackProgress += this.calculateAbilityProgress(abilityData, abilityCondition);
                }

                if (stackProgress > bestProgress)
                    bestProgress = stackProgress;
            }

            progressSum += bestProgress;
        }

        return Math.clamp(progressSum / totalAbilities, 0D, 1D);
    }

    public boolean canBeUpgraded() {
        var template = getTemplate();

        return template != null && !template.getStats().isEmpty();
    }

    public SynergyComponent getComponent() {
        var template = getTemplate();

        if (template == null)
            return null;

        var abilitiesComponent = this.getAbilitiesData().getComponent();
        var synergyComponent = abilitiesComponent.getSynergies().get(this.getId());

        if (synergyComponent != null)
            return synergyComponent;

        synergyComponent = SynergyComponent.EMPTY;

        this.getAbilitiesData().setComponent(abilitiesComponent.toBuilder()
                .synergy(this.getId(), synergyComponent)
                .build());

        return synergyComponent;
    }

    public void setComponent(SynergyComponent component) {
        this.getAbilitiesData().setComponent(this.getAbilitiesData().getComponent().toBuilder()
                .synergy(this.getId(), component)
                .build());
    }

    public String getMode() {
        var template = getTemplate();

        if (template == null)
            return "";

        var modes = template.getModes();

        if (modes.isEmpty())
            return "";

        var mode = getComponent().getMode();

        return mode.isEmpty() ? modes.getFirst() : mode;
    }

    public void setMode(String mode) {
        setComponent(getComponent().toBuilder()
                .mode(mode)
                .build());
    }

    public SynergyRankModifierData getRankModifierData(String rankModifier) {
        return new SynergyRankModifierData(this, rankModifier);
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isUnlocked() {
        var template = getTemplate();

        if (template == null)
            return false;

        var conditions = template.getRelicConditions();

        if (conditions.isEmpty())
            return true;

        return conditions.stream().allMatch(this::isRelicConditionSatisfied);
    }

    private boolean isRelicConditionSatisfied(RelicConditionTemplate condition) {
        var entity = this.getAbilitiesData().getRelicData().getEntity();
        var relic = condition.getRelic().get();

        if (relic == null)
            return false;

        var relicStacks = this.findRelicStacks(relic, condition.getRelicContainers());

        for (var stack : relicStacks) {
            if (meetsConditions(relic.getRelicData(entity, stack), condition))
                return true;
        }

        return false;
    }

    private boolean meetsConditions(RelicData relicData, RelicConditionTemplate condition) {
        var levelingData = relicData.getLevelingData();

        if (levelingData.getLevel() < condition.getLevel())
            return false;

        if (levelingData.getRank() < condition.getRank())
            return false;

        if (condition.getQuality() > 0 && relicData.calculateQuality() < condition.getQuality())
            return false;

        return (condition.getProgress() <= 0D || relicData.calculateProgress() >= condition.getProgress())
                && areAbilityConditionsSatisfied(relicData, condition.getAbilityConditions());
    }

    private boolean areAbilityConditionsSatisfied(RelicData relicData, List<AbilityConditionTemplate> conditions) {
        var abilitiesData = relicData.getAbilitiesData();

        for (var abilityCondition : conditions) {
            var abilityData = abilitiesData.getAbilityData(abilityCondition.getId());

            if (abilityData == null || !abilityData.isUnlocked())
                return false;

            if (abilityCondition.getProgress() > 0D && abilityData.getOverallProgress() < abilityCondition.getProgress())
                return false;

            if (abilityCondition.getQuality() > 0 && abilityData.calculateQuality() < abilityCondition.getQuality())
                return false;
        }

        return true;
    }

    private double calculateAbilityProgress(AbilityData abilityData, AbilityConditionTemplate condition) {
        var minimum = condition.getProgress();
        var progress = abilityData.getOverallProgress();

        if (minimum >= 1D)
            return progress >= minimum ? 1D : 0D;

        return Math.clamp((progress - minimum) / (1D - minimum), 0D, 1D);
    }

    private List<ItemStack> findRelicStacks(IRelicItem relic, List<RelicContainer> containers) {
        return RelicContainer.find(abilitiesData.getRelicData().getEntity(), containers, stack -> stack.getItem() == relic);
    }
}
