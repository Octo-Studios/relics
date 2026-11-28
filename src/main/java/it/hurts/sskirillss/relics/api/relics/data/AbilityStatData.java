package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatStacking;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;
import javax.annotation.Nullable;

public class AbilityStatData {
    private final AbilityData abilityData;
    private final String stat;

    public AbilityStatData(AbilityData abilityData, String stat) {
        this.abilityData = abilityData;
        this.stat = stat;
    }

    public AbilityData getAbilityData() {
        return this.abilityData;
    }

    public String getId() {
        return this.stat;
    }

    @Nullable
    public AbilityStatTemplate getTemplate() {
        var abilityTemplate = this.getAbilityData().getTemplate();

        return abilityTemplate == null ? null : abilityTemplate.getStats().get(this.getId());
    }

    public List<RelicContainer> getActiveContainers() {
        var template = getTemplate();

        return template == null || template.getActiveContainers().isEmpty() ? abilityData.getActiveContainers() : template.getActiveContainers();
    }

    public StatComponent getComponent() {
        var statTemplate = getTemplate();

        if (statTemplate == null)
            return null;

        var abilityComponent = this.getAbilityData().getComponent();
        var statComponent = abilityComponent.getStats().get(this.getId());

        if (statComponent != null)
            return statComponent;

        statComponent = StatComponent.EMPTY;

        statComponent = statComponent.toBuilder()
                .initialQuality(new Random().nextInt(11))
                .build();

        this.getAbilityData().setComponent(abilityComponent.toBuilder()
                .stat(this.getId(), statComponent)
                .build());

        return statComponent;
    }

    public void setComponent(StatComponent component) {
        var abilityComponent = this.getAbilityData().getComponent();

        this.getAbilityData().setComponent(abilityComponent.toBuilder()
                .stat(this.getId(), component)
                .build());
    }

    public int getInitialQuality() {
        return getComponent().getInitialQuality();
    }

    public void setInitialQuality(int quality) {
        setComponent(getComponent().toBuilder()
                .initialQuality(Math.clamp(quality, 0, getMaxQuality()))
                .build());
    }

    public void addInitialQuality(int quality) {
        setInitialQuality(getInitialQuality() + quality);
    }

    public Optional<Double> getOverrideValue() {
        return getComponent().getOverrideValue();
    }

    public void setOverrideValue(@Nullable Double value) {
        setComponent(getComponent().toBuilder()
                .overrideValue(value == null ? Optional.empty() : Optional.of(value))
                .build());
    }

    public void addOverrideValue(double value) {
        setOverrideValue(getOverrideValue().orElse(0D) + value);
    }

    public int getMaxQuality() {
        return 10;
    }

    public int getQuality() {
        var statComponent = getComponent();

        if (statComponent == null)
            return 0;

        var statData = getTemplate();

        if (statData == null)
            return 0;

        var initialValue = statData.getInitialValue();
        var min = initialValue.getMinValue();
        var max = initialValue.getMaxValue();
        var maxQuality = getMaxQuality();

        if (min == max)
            return maxQuality;

        var override = statComponent.getOverrideValue();

        if (override.isEmpty())
            return statComponent.getInitialQuality();

        var quality = (int) Math.round((override.get() - min) * maxQuality / (max - min));
        return Mth.clamp(quality, 0, maxQuality);
    }

    public double getUnscaledValue() {
        return getOverrideValue().orElseGet(() -> getValueFromQuality(getInitialQuality()));
    }

    public double getValueFromQuality(int quality) {
        var template = getTemplate();

        if (template == null)
            return 0;

        var initialValue = template.getInitialValue();

        var min = initialValue.getMinValue();
        var max = initialValue.getMaxValue();

        if (min == max)
            return max;

        var value = min + (((max - min) / getMaxQuality()) * quality);

        return MathUtils.round(value, 5);
    }

    private double calculateValue(double progress, AbilityStatTemplate template) {
        var relicData = abilityData.getAbilitiesData().getRelicData();
        var entity = relicData.getEntity();
        var stack = relicData.getStack();
        var target = template.getTargetValue();
        var model = target.getScalingModel();
        var steps = Math.max(1, abilityData.getTemplate().getLevelContribution(relicData.getTemplate().getLeveling().getMaxRank()));
        var modifier = model.calculateModifier(entity, stack, template.getInitialValue().getMaxValue(), target.getTargetValue(), steps);
        var value = model.evaluate(entity, stack, getUnscaledValue(), modifier, Math.clamp(progress, 0D, 1D) * steps);
        var threshold = template.getThresholdValue();

        return MathUtils.round(Mth.clamp(value, threshold.getMinValue(), threshold.getMaxValue()), 5);
    }

    public double getValueForOverallProgress(double progress) {
        var template = getTemplate();

        return template == null ? 0D : template.resolveValue(calculateValue(progress, template));
    }

    public double getRawValue() {
        var template = getTemplate();

        return template == null ? 0D : calculateValue(abilityData.getOverallProgress(), template);
    }

    public double getValue() {
        return getValueForOverallProgress(abilityData.getOverallProgress());
    }

    public boolean isInActiveContainer() {
        var relic = abilityData.getAbilitiesData().getRelicData();

        return RelicContainer.contains(relic.getEntity(), relic.getStack(), getActiveContainers());
    }

    public boolean isAvailable() {
        var template = getTemplate();

        return template != null && isInActiveContainer() && template.getStacking().isAvailable(abilityData);
    }

    public StatStacking getStacking() {
        return StatStacking.of(this);
    }

    public OptionalDouble getActiveStackingEfficiency() {
        return getStacking().efficiency(abilityData.getAbilitiesData().getRelicData().getStack());
    }

    public double getStackingEfficiency() {
        return getActiveStackingEfficiency().orElse(0D);
    }

    public double getStackedValue() {
        var template = getTemplate();

        if (template == null)
            return 0D;

        var stacking = template.getStacking();
        if (!stacking.getType().ranksCopiesByStat())
            return isAvailable() ? getValue() : stacking.getNeutralValue();

        return getStacking().contribution(abilityData.getAbilitiesData().getRelicData().getStack()).orElse(stacking.getNeutralValue());
    }
}
