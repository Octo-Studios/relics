package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityExtenderComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatComponent;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Optional;

@ApiStatus.Internal
public interface IRelicDataHolder {
    default RelicComponent getRelicComponent(LivingEntity entity, ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.DATA, RelicComponent.EMPTY);
    }

    default void setRelicComponent(LivingEntity entity, ItemStack stack, RelicComponent data) {
        stack.set(DataComponentRegistry.DATA, data);
    }

    default LevelingComponent getLevelingData(LivingEntity entity, ItemStack stack) {
        return getRelicComponent(entity, stack).getLeveling();
    }

    default void setLevelingData(LivingEntity entity, ItemStack stack, LevelingComponent data) {
        setRelicComponent(entity, stack, getRelicComponent(entity, stack).toBuilder().leveling(data).build());
    }

    default AbilitiesComponent getAbilitiesComponent(LivingEntity entity, ItemStack stack) {
        return getRelicComponent(entity, stack).getAbilities();
    }

    default void setAbilitiesComponent(LivingEntity entity, ItemStack stack, AbilitiesComponent data) {
        setRelicComponent(entity, stack, getRelicComponent(entity, stack).toBuilder().abilities(data).build());
    }

    @Nullable
    default AbilityComponent getAbilityComponent(LivingEntity entity, ItemStack stack, String ability) {
        if (!(stack.getItem() instanceof IRelicTemplateHolder templateHolder))
            return null;

        var abilitiesComponent = getAbilitiesComponent(entity, stack);
        var abilityComponent = abilitiesComponent.getAbilities().get(ability);
        var abilityTemplate = templateHolder.getAbilityTemplate(entity, stack, ability);

        if (abilityComponent != null)
            return abilityComponent;
        else if (abilityTemplate != null) {
            abilityComponent = AbilityComponent.EMPTY;

            if (abilityTemplate.getRequiredLevel() <= 0)
                abilityComponent = abilityComponent.toBuilder()
                        .lock(LockComponent.builder()
                                .unlocks(this.getMaxLockUnlocks())
                                .build())
                        .build();

            setAbilitiesComponent(entity, stack, abilitiesComponent.toBuilder()
                    .ability(ability, abilityComponent)
                    .build());

            return abilityComponent;
        } else
            return null;
    }

    default void setAbilityComponent(LivingEntity entity, ItemStack stack, String ability, AbilityComponent component) {
        setAbilitiesComponent(entity, stack, getAbilitiesComponent(entity, stack).toBuilder().ability(ability, component).build());
    }

    @Nullable
    default StatComponent getStatComponent(LivingEntity entity, ItemStack stack, String ability, String stat) {
        if (!(stack.getItem() instanceof IRelicTemplateHolder templateHolder))
            return null;

        var abilityComponent = getAbilityComponent(entity, stack, ability);
        var statComponent = abilityComponent.getStats().get(stat);
        var statData = templateHolder.getStatTemplate(entity, stack, ability, stat);

        if (statComponent != null)
            return statComponent;
        else if (statData != null) {
            statComponent = StatComponent.EMPTY;

            setAbilityComponent(entity, stack, ability, abilityComponent.toBuilder()
                    .stat(stat, statComponent)
                    .build());

            return statComponent;
        } else
            return null;
    }

    default void setStatComponent(LivingEntity entity, ItemStack stack, String ability, String stat, StatComponent component) {
        setAbilityComponent(entity, stack, ability, getAbilityComponent(entity, stack, ability).toBuilder()
                .stat(stat, component)
                .build());
    }

    default int getStatInitialQuality(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return getStatComponent(entity, stack, ability, stat).getInitialQuality();
    }

    default void setStatInitialQuality(LivingEntity entity, ItemStack stack, String ability, String stat, int quality) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        setStatComponent(entity, stack, ability, stat, getStatComponent(entity, stack, ability, stat).toBuilder()
                .initialQuality(Math.clamp(quality, 0, relic.getStatMaxQuality(entity, stack, ability, stat)))
                .build());
    }

    default void addStatInitialQuality(LivingEntity entity, ItemStack stack, String ability, String stat, int quality) {
        setStatOverrideValue(entity, stack, ability, stat, getStatInitialQuality(entity, stack, ability, stat) + quality);
    }

    default Optional<Double> getStatOverrideValue(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return getStatComponent(entity, stack, ability, stat).getOverrideValue();
    }

    default void setStatOverrideValue(LivingEntity entity, ItemStack stack, String ability, String stat, double value) {
        setStatComponent(entity, stack, ability, stat, getStatComponent(entity, stack, ability, stat).toBuilder()
                .overrideValue(Optional.of(value))
                .build());
    }

    default void addStatOverrideValue(LivingEntity entity, ItemStack stack, String ability, String stat, double value) {
        setStatOverrideValue(entity, stack, ability, stat, getStatOverrideValue(entity, stack, ability, stat).orElse(0D) + value);
    }

    default AbilityExtenderComponent getAbilityExtenderComponent(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityComponent(entity, stack, ability).getExtender();
    }

    default void setAbilityExtenderComponent(LivingEntity entity, ItemStack stack, String ability, AbilityExtenderComponent component) {
        setAbilityComponent(entity, stack, ability, getAbilityComponent(entity, stack, ability).toBuilder()
                .extender(component)
                .build());
    }

    default LockComponent getLockComponent(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityComponent(entity, stack, ability).getLock();
    }

    default void setLockComponent(LivingEntity entity, ItemStack stack, String ability, LockComponent component) {
        setAbilityComponent(entity, stack, ability, getAbilityComponent(entity, stack, ability).toBuilder()
                .lock(component)
                .build());
    }

    default int getMaxLockUnlocks() {
        return 5;
    }

    default int getLockUnlocks(LivingEntity entity, ItemStack stack, String ability) {
        return getLockComponent(entity, stack, ability).getUnlocks();
    }

    default void setLockUnlocks(LivingEntity entity, ItemStack stack, String ability, int unlocks) {
        setLockComponent(entity, stack, ability, getLockComponent(entity, stack, ability).toBuilder()
                .unlocks(Mth.clamp(unlocks, 0, getMaxLockUnlocks()))
                .build());
    }

    default void addLockUnlocks(LivingEntity entity, ItemStack stack, String ability, int unlocks) {
        setLockUnlocks(entity, stack, ability, getLockUnlocks(entity, stack, ability) + unlocks);
    }

    default boolean isLockUnlocked(LivingEntity entity, ItemStack stack, String ability) {
        return getLockUnlocks(entity, stack, ability) >= getMaxLockUnlocks();
    }

    default ResearchComponent getResearchComponent(LivingEntity entity, ItemStack stack, String ability) {
        return getAbilityComponent(entity, stack, ability).getResearch();
    }

    default StatisticComponent getRelicStatisticComponent(LivingEntity entity, ItemStack stack) {
        return this.getRelicComponent(entity, stack).getStatistic();
    }

    default void setRelicStatisticComponent(LivingEntity entity, ItemStack stack, StatisticComponent component) {
        this.setRelicComponent(entity, stack, this.getRelicComponent(entity, stack).toBuilder()
                .statistic(component)
                .build());
    }

    default MetricComponent getRelicMetricComponent(LivingEntity entity, ItemStack stack, String metric) {
        if (!(stack.getItem() instanceof IRelicTemplateHolder templateHolder))
            return null;

        var statisticComponent = this.getRelicStatisticComponent(entity, stack);
        var metricComponent = statisticComponent.getMetrics().get(metric);
        var metricTemplate = templateHolder.getRelicMetricTemplate(entity, stack, metric);

        if (metricComponent != null)
            return metricComponent;
        else if (metricTemplate != null) {
            metricComponent = MetricComponent.EMPTY;

            this.setRelicStatisticComponent(entity, stack, statisticComponent.toBuilder()
                    .metric(metric, metricComponent)
                    .build());

            return metricComponent;
        } else
            return null;
    }

    default void setRelicMetricComponent(LivingEntity entity, ItemStack stack, String metric, MetricComponent component) {
        this.setRelicStatisticComponent(entity, stack, this.getRelicStatisticComponent(entity, stack).toBuilder()
                .metric(metric, component)
                .build());
    }

    default double getRelicMetricValue(LivingEntity entity, ItemStack stack, String metric) {
        return this.getRelicMetricComponent(entity, stack, metric).getValue();
    }

    default void setRelicMetricValue(LivingEntity entity, ItemStack stack, String metric, double value) {
        this.setRelicMetricComponent(entity, stack, metric, this.getRelicMetricComponent(entity, stack, metric).toBuilder().value(value).build());
    }

    default void addRelicMetricValue(LivingEntity entity, ItemStack stack, String metric, double value) {
        this.setRelicMetricValue(entity, stack, metric, this.getRelicMetricValue(entity, stack, metric) + value);
    }

    default StatisticComponent getAbilityStatisticComponent(LivingEntity entity, ItemStack stack, String ability) {
        return this.getAbilityComponent(entity, stack, ability).getStatistic();
    }

    default void setAbilityStatisticComponent(LivingEntity entity, ItemStack stack, String ability, StatisticComponent component) {
        this.setAbilityComponent(entity, stack, ability, this.getAbilityComponent(entity, stack, ability).toBuilder()
                .statistic(component)
                .build());
    }

    default MetricComponent getAbilityMetricComponent(LivingEntity entity, ItemStack stack, String ability, String metric) {
        if (!(stack.getItem() instanceof IRelicTemplateHolder templateHolder))
            return null;

        var statisticComponent = this.getAbilityStatisticComponent(entity, stack, ability);
        var metricComponent = statisticComponent.getMetrics().get(metric);
        var metricTemplate = templateHolder.getAbilityMetricTemplate(entity, stack, ability, metric);

        if (metricComponent != null)
            return metricComponent;
        else if (metricTemplate != null) {
            metricComponent = MetricComponent.EMPTY;

            this.setAbilityStatisticComponent(entity, stack, ability, statisticComponent.toBuilder()
                    .metric(metric, metricComponent)
                    .build());

            return metricComponent;
        } else
            return null;
    }

    default void setAbilityMetricComponent(LivingEntity entity, ItemStack stack, String ability, String metric, MetricComponent component) {
        this.setAbilityStatisticComponent(entity, stack, ability, this.getAbilityStatisticComponent(entity, stack, ability).toBuilder()
                .metric(metric, component)
                .build());
    }

    default double getAbilityMetricValue(LivingEntity entity, ItemStack stack, String ability, String metric) {
        return this.getAbilityMetricComponent(entity, stack, ability, metric).getValue();
    }

    default void setAbilityMetricValue(LivingEntity entity, ItemStack stack, String ability, String metric, double value) {
        this.setAbilityMetricComponent(entity, stack, ability, metric, this.getAbilityMetricComponent(entity, stack, ability, metric).toBuilder().value(value).build());
    }

    default void addAbilityMetricValue(LivingEntity entity, ItemStack stack, String ability, String metric, double value) {
        this.setAbilityMetricValue(entity, stack, ability, metric, this.getAbilityMetricValue(entity, stack, ability, metric) + value);
    }
}