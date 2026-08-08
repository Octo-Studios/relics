package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatComponent;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

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

        var optional = statComponent.getOverrideValue();
        var value = optional.orElseGet(() -> getValueFromQuality(statComponent.getInitialQuality()));

        var statData = getTemplate();

        if (statData == null)
            return 0;

        var initialValue = statData.getInitialValue();
        var format = statData.getFormatValue();

        var override = format.apply(value).doubleValue();

        var min = format.apply(initialValue.getMinValue()).doubleValue();
        var max = format.apply(initialValue.getMaxValue()).doubleValue();

        var maxQuality = getMaxQuality();

        if (min == max)
            return maxQuality;

        var quality = (int) Math.round((override - min) * maxQuality / (max - min));

        return Mth.clamp(quality, 0, maxQuality);
    }

    public double getUnscaledValue() {
        return getOverrideValue().orElse(getValueFromQuality(getQuality()));
    }

    public double getRelativeValue(double value, int points) {
        var template = getTemplate();

        if (template == null)
            return 0D;

        var threshold = template.getThresholdValue();

        var targetValue = template.getTargetValue();
        var relicData = this.getAbilityData().getAbilitiesData().getRelicData();
        var entity = relicData.getEntity();
        var stack = relicData.getStack();
        var baseValue = template.getInitialValue().getMaxValue();
        var maxPoints = calculateMaxTargetPoints();
        var modifier = targetValue.getScalingModel().calculateModifier(entity, stack, baseValue, targetValue.getTargetValue(), maxPoints);

        return MathUtils.round(Mth.clamp(targetValue.getScalingModel()
                .evaluate(entity, stack, value, modifier, points), threshold.getMinValue(), threshold.getMaxValue()), 5);
    }

    private int calculateMaxTargetPoints() {
        var relicData = this.getAbilityData().getAbilitiesData().getRelicData();
        var levelingData = relicData.getLevelingData();
        var abilityTemplate = this.getAbilityData().getTemplate();
        var maxLevel = abilityTemplate.getInitialMaxLevel();

        for (var rank = levelingData.getRank(); rank < relicData.getTemplate().getLeveling().getMaxRank(); rank++)
            maxLevel += (int) Math.ceil(maxLevel * abilityTemplate.getMaxLevelRankModifier());

        return maxLevel;
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

    public double getValueForLevel(int level) {
        return getRelativeValue(getUnscaledValue(), level);
    }

    public double getValue() {
        return this.getValueForLevel(this.getAbilityData().getLevel());
    }
}
