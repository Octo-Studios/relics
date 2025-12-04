package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@ApiStatus.Experimental
@ApiStatus.NonExtendable
public interface IRelicUtilities {
    default double getRelicExperienceLeftForLevelUp(LivingEntity entity, ItemStack stack, int level) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        return getTotalRelicExperienceBetweenLevels(entity, stack, relic.getRelicLevel(entity, stack), level) - relic.getRelicExperience(entity, stack);
    }

    default double getTotalRelicExperienceBetweenLevels(LivingEntity entity, ItemStack stack, int from, int to) {
        return getTotalRelicExperienceForLevel(entity, stack, to) - getTotalRelicExperienceForLevel(entity, stack, from);
    }

    default double getTotalRelicExperienceForLevel(LivingEntity entity, ItemStack stack, int level) {
        if (!(stack.getItem() instanceof IRelicItem relic) || level <= 0)
            return 0;

        var template = relic.getLevelingTemplate(entity, stack);
        var operation = template.getScalingModel();

        var total = 0D;

        for (int i = 0; i < level; i++)
            total += operation.evaluate(entity, stack, template.getInitialCost(), template.getStep(), i);

        return (int) Math.floor(total);
    }

    default int getRelicLevelFromExperience(LivingEntity entity, ItemStack stack, int experience) {
        int result = 0;
        var amount = 0D;

        do {
            ++result;

            amount = getTotalRelicExperienceForLevel(entity, stack, result);
        } while (amount <= experience);

        return result - 1;
    }

    default int getOrCalculateStatQuality(LivingEntity entity, ItemStack stack, String ability, String stat) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0;

        var statComponent = relic.getStatComponent(entity, stack, ability, stat);

        if (statComponent == null)
            return 0;

        var optional = statComponent.getOverrideValue();

        var value = optional.orElseGet(() -> this.getStatValueFromQuality(entity, stack, ability, stat, statComponent.getInitialQuality()));

        var statData = relic.getStatTemplate(entity, stack, ability, stat);

        var initialValue = statData.getInitialValue();
        var format = statData.getFormatValue();

        var override = format.apply(value).doubleValue();

        var min = format.apply(initialValue.getMinValue()).doubleValue();
        var max = format.apply(initialValue.getMaxValue()).doubleValue();

        if (min == max)
            return relic.getStatMaxQuality(entity, stack, ability, stat);

        if (override <= min)
            return 0;

        if (override >= max)
            return relic.getStatMaxQuality(entity, stack, ability, stat);

        return Mth.clamp((int) Math.round((override - min) / ((max - min) / relic.getStatMaxQuality(entity, stack, ability, stat))), 1, relic.getStatMaxQuality(entity, stack, ability, stat) - 1);
    }

    default double getOrCalculateStatValue(LivingEntity entity, ItemStack stack, String ability, String stat) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        return relic.getStatOverrideValue(entity, stack, ability, stat).orElse(getStatValueFromQuality(entity, stack, ability, stat, relic.getOrCalculateStatQuality(entity, stack, ability, stat)));
    }

    default double getRelativeStatValue(LivingEntity entity, ItemStack stack, String ability, String stat, double value, int points) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        var template = relic.getStatTemplate(entity, stack, ability, stat);

        if (template == null)
            return 0D;

        var threshold = template.getThresholdValue();

        return MathUtils.round(Mth.clamp(template.getUpgradeModifier().getScalingModel().evaluate(entity, stack, value, template.getUpgradeModifier().getModifier(), points), threshold.getMinValue(), threshold.getMaxValue()), 5);
    }

    default double getStatValueFromQuality(LivingEntity entity, ItemStack stack, String ability, String stat, int quality) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        var template = relic.getStatTemplate(entity, stack, ability, stat);

        if (template == null)
            return 0;

        var initialValue = template.getInitialValue();

        var min = initialValue.getMinValue();
        var max = initialValue.getMaxValue();

        if (min == max)
            return max;

        var value = min + (((max - min) / relic.getStatMaxQuality(entity, stack, ability, stat)) * quality);

        return MathUtils.round(value, 5);
    }

    default double getStatValueForLevel(LivingEntity entity, ItemStack stack, String ability, String stat, int level) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        return getRelativeStatValue(entity, stack, ability, stat, getOrCalculateStatValue(entity, stack, ability, stat), level);
    }

    @Deprecated(forRemoval = true)
    default boolean isSomethingWrongWithLevelingPoints(LivingEntity entity, ItemStack stack) {
        if (!(this instanceof IRelicItem relic))
            return false;

        int current = relic.getRelicLevelingPoints(entity, stack);

        for (var data : relic.getAbilitiesTemplate(entity, stack).getAbilities().values())
            current += relic.getAbilityComponent(entity, stack, data.getId()).getPoints() * data.getRequiredPoints();

        return current != relic.getRelicLevel(entity, stack);
    }

    default boolean isRelicMaxRank(LivingEntity entity, ItemStack stack) {
        if (!(this instanceof IRelicItem relic))
            return false;

        return relic.getRelicRank(entity, stack) >= relic.getLevelingTemplate(entity, stack).getMaxRank();
    }

    default boolean isRelicMaxLevel(LivingEntity entity, ItemStack stack) {
        if (!(this instanceof IRelicItem relic))
            return false;

        return relic.getRelicLevel(entity, stack) >= relic.calculateRelicMaxLevel(entity, stack);
    }

    default boolean isRelicMaxQuality(LivingEntity entity, ItemStack stack) {
        if (!(this instanceof IRelicItem relic))
            return false;

        return relic.calculateRelicQuality(entity, stack) >= relic.getRelicMaxQuality(entity, stack);
    }

    default boolean isRelicFlawless(LivingEntity entity, ItemStack stack) {
        if (!(this instanceof IRelicItem relic))
            return false;

        return relic.calculateRelicProgress(entity, stack) >= 1F;
    }

    default boolean isAbilityMaxLevel(LivingEntity entity, ItemStack stack, String ability) {
        if (!(this instanceof IRelicItem relic))
            return false;

        return relic.getAbilityLevel(entity, stack, ability) >= relic.getAbilityTemplate(entity, stack, ability).getInitialMaxLevel();
    }

    default boolean isAbilityMaxQuality(LivingEntity entity, ItemStack stack, String ability) {
        if (!(this instanceof IRelicItem relic))
            return false;

        return relic.calculateAbilityQuality(entity, stack, ability) >= relic.getAbilityMaxQuality(entity, stack, ability);
    }

    default boolean addRelicExperience(LivingEntity entity, ItemStack stack, double amount) {
        if (!(this instanceof IRelicItem relic))
            return false;

        var xp = relic.getRelicExperience(entity, stack);
        var level = relic.getRelicLevel(entity, stack);
        var oldLevel = level;
        var maxLevel = relic.calculateRelicMaxLevel(entity, stack);

        while ((amount > 0 && level < maxLevel) || (amount < 0 && level > 0)) {
            if (amount > 0) {
                var requirement = getTotalRelicExperienceBetweenLevels(entity, stack, level, level + 1) - xp;

                if (amount >= requirement) {
                    amount -= requirement;

                    level++;

                    xp = 0;
                } else {
                    xp += amount;

                    amount = 0;
                }
            } else {
                if (xp + amount >= 0) {
                    xp += amount;

                    amount = 0;
                } else {
                    amount += xp;

                    level--;

                    xp = getTotalRelicExperienceBetweenLevels(entity, stack, level, level + 1);
                }
            }
        }

        if (amount < 0)
            xp = 0;

        relic.setRelicExperience(entity, stack, xp);

        if (level != oldLevel)
            relic.addRelicLevel(entity, stack, level - oldLevel);

        return true;
    }
}