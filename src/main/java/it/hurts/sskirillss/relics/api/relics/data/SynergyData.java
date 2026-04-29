package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.LockComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityComponent;
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyComponent;
import it.hurts.sskirillss.relics.api.relics.synergies.SynergyTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.AbilityConditionTemplate;
import it.hurts.sskirillss.relics.api.relics.synergies.conditions.RelicConditionTemplate;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

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

        double progressSum = 0D;

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

            double bestProgress = 0D;

            for (var stack : relicStacks) {
                var relicData = relic.getRelicData(entity, stack);
                var conditionAbilitiesData = relicData.getAbilitiesData();
                double stackProgress = 0D;

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

        if (relicStacks.isEmpty())
            return false;

        for (var stack : relicStacks) {
            var relicData = relic.getRelicData(entity, stack);

            if (!this.isRelicDataSatisfied(relicData, condition))
                continue;

            if (this.areAbilityConditionsSatisfied(relicData, condition.getAbilityConditions()))
                return true;
        }

        return false;
    }

    private boolean isRelicDataSatisfied(RelicData relicData, RelicConditionTemplate condition) {
        var levelingData = relicData.getLevelingData();

        if (levelingData.getLevel() < condition.getLevel())
            return false;

        if (levelingData.getRank() < condition.getRank())
            return false;

        if (relicData.calculateQuality() < condition.getQuality())
            return false;

        return relicData.calculateProgress() >= condition.getProgress();
    }

    private boolean areAbilityConditionsSatisfied(RelicData relicData, List<AbilityConditionTemplate> conditions) {
        var abilitiesData = relicData.getAbilitiesData();

        for (var abilityCondition : conditions) {
            var abilityData = abilitiesData.getAbilityData(abilityCondition.getId());

            if (abilityData == null || !abilityData.isUnlocked())
                return false;

            if (abilityData.getLevel() < abilityCondition.getLevel())
                return false;

            if (abilityData.calculateQuality() < abilityCondition.getQuality())
                return false;
        }

        return true;
    }

    private double calculateAbilityProgress(AbilityData abilityData, AbilityConditionTemplate condition) {
        if (abilityData == null)
            return 0D;

        var template = abilityData.getTemplate();

        if (template == null)
            return 0D;

        var maxLevel = template.getInitialMaxLevel();
        var minLevel = condition.getLevel();

        if (maxLevel <= minLevel)
            return abilityData.getLevel() >= minLevel ? 1D : 0D;

        var currentLevel = abilityData.getLevel();
        var progress = (currentLevel - minLevel) / (double) (maxLevel - minLevel);

        return Math.clamp(progress, 0D, 1D);
    }

    private List<ItemStack> findRelicStacks(IRelicItem relic, List<RelicContainer> containers) {
        return containers.stream()
                .flatMap(container -> container.gatherRelics().apply(this.getAbilitiesData().getRelicData().getEntity()).stream())
                .filter(stack -> stack.getItem() == relic)
                .toList();
    }
}
