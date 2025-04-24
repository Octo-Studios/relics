package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
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

        for (int i = 1; i < level; i++) {
            double value = operation.evaluate(entity, stack, template.getInitialCost(), template.getStep(), i - 1);

            total += value;
        }

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

    default double getRelativeStatValue(LivingEntity entity, ItemStack stack, String ability, String stat, double value, int points) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        var template = relic.getStatTemplate(entity, stack, ability, stat);

        if (template == null)
            return 0D;

        var threshold = template.getThresholdValue();

        return MathUtils.round(Mth.clamp(template.getUpgradeModifier().getKey().evaluate(entity, stack, value, template.getUpgradeModifier().getValue(), points), threshold.getKey(), threshold.getValue()), 5);
    }

    default double getStatValueByQuality(LivingEntity entity, ItemStack stack, String ability, String stat, int quality) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        StatTemplate template = relic.getStatTemplate(entity, stack, ability, stat);

        if (template == null)
            return 0;

        double min = template.getInitialValue().getKey();
        double max = template.getInitialValue().getValue();

        if (min == max)
            return max;

        return MathUtils.round(min + (((max - min) / relic.getStatMaxQuality(entity, stack, ability, stat)) * quality), 5);
    }

    default double getStatValueForLevel(LivingEntity entity, ItemStack stack, String ability, String stat, int level) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        return getRelativeStatValue(entity, stack, ability, stat, relic.getStatOverrideValue(entity, stack, ability, stat).orElse(getStatValueByQuality(entity, stack, ability, stat, relic.getStatQuality(entity, stack, ability, stat))), level);
    }
}