package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

public class StatData {
    private final AbilityData abilityData;
    private final String stat;

    public StatData(AbilityData abilityData, String stat) {
        this.abilityData = abilityData;
        this.stat = stat;
    }

    public String getId() {
        return stat;
    }

    @Nullable
    public StatTemplate getTemplate() {
        var abilityTemplate = abilityData.getTemplate();

        return abilityTemplate == null ? null : abilityTemplate.getStats().get(stat);
    }

    public StatComponent getComponent() {
        var statTemplate = getTemplate();

        if (statTemplate == null)
            return null;

        var abilityComponent = abilityData.getComponent();
        var statComponent = abilityComponent.getStats().get(stat);

        if (statComponent != null)
            return statComponent;

        statComponent = StatComponent.EMPTY;

        statComponent = statComponent.toBuilder()
                .initialQuality(new Random().nextInt(11))
                .build();

        abilityData.setComponent(abilityComponent.toBuilder()
                .stat(stat, statComponent)
                .build());

        return statComponent;
    }

    public void setComponent(StatComponent component) {
        var abilityComponent = abilityData.getComponent();

        abilityData.setComponent(abilityComponent.toBuilder()
                .stat(stat, component)
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

        return MathUtils.round(Mth.clamp(template.getUpgradeModifier().getScalingModel()
                .evaluate(abilityData.getAbilitiesData().getRelicData().getEntity(), abilityData.getAbilitiesData().getRelicData().getStack(), value, template.getUpgradeModifier().getModifier(), points), threshold.getMinValue(), threshold.getMaxValue()), 5);
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
        return getValueForLevel(abilityData.getLevel());
    }
}
