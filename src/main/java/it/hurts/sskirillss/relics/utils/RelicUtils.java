package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RelicUtils {
    public static double getRelicExperienceLeftForLevelUp(LivingEntity entity, ItemStack stack, int level) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        return getTotalRelicExperienceBetweenLevels(entity, stack, relic.getRelicLevel(entity, stack), level) - relic.getRelicExperience(entity, stack);
    }

    public static double getTotalRelicExperienceBetweenLevels(LivingEntity entity, ItemStack stack, int from, int to) {
        return getTotalRelicExperienceForLevel(entity, stack, to) - getTotalRelicExperienceForLevel(entity, stack, from);
    }

    public static double getTotalRelicExperienceForLevel(LivingEntity entity, ItemStack stack, int level) {
        if (!(stack.getItem() instanceof IRelicItem relic) || level <= 0)
            return 0;

        var template = relic.getLevelingTemplate(entity, stack);
        var operation = template.getScalingModel();

        var total = 0D;

        for (int i = 1; i < level; i++) {
            double value = operation.evaluate(template.getInitialCost(), template.getStep(), i - 1);

            total += value;
        }

        return (int) Math.floor(total);
    }

    public static int getRelicLevelFromExperience(LivingEntity entity, ItemStack stack, int experience) {
        int result = 0;
        var amount = 0D;

        do {
            ++result;

            amount = getTotalRelicExperienceForLevel(entity, stack, result);
        } while (amount <= experience);

        return result - 1;
    }
}