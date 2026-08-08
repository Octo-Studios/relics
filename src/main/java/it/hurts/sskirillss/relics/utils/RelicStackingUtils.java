package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public final class RelicStackingUtils {
    public static List<ItemStack> findActiveStacks(LivingEntity entity, Item item, Predicate<ItemStack> predicate) {
        return EntityUtils.findEquippedCurios(entity, item).stream()
                .filter(predicate)
                .toList();
    }

    public static ItemStack getControllerStack(List<ItemStack> stacks) {
        return stacks.isEmpty() ? ItemStack.EMPTY : stacks.getFirst();
    }

    public static boolean isControllerStack(ItemStack stack, List<ItemStack> stacks) {
        return !stacks.isEmpty() && stacks.getFirst() == stack;
    }

    private static double clampByThreshold(AbilityData abilityData, String stat, double value) {
        if (abilityData == null)
            return value;

        var template = abilityData.getTemplate();
        var statTemplate = template == null ? null : template.getStats().get(stat);

        if (statTemplate == null)
            return value;

        var threshold = statTemplate.getThresholdValue();

        return Math.clamp(value, threshold.getMinValue(), threshold.getMaxValue());
    }

    private static boolean isLowerBetter(AbilityData abilityData, String stat) {
        var template = abilityData.getTemplate();
        var statTemplate = template == null ? null : template.getStats().get(stat);

        return statTemplate != null && statTemplate.getTargetValue().getTargetValue() < statTemplate.getInitialValue().getMaxValue();
    }

    private static List<AbilityData> getActiveAbilityData(LivingEntity entity, ItemStack stack, String ability, Predicate<AbilityData> predicate) {
        if (!(stack.getItem() instanceof IRelicItem))
            return List.of();

        return EntityUtils.findEquippedCurios(entity, stack.getItem()).stream()
                .map(equippedStack -> equippedStack.getItem() instanceof IRelicItem equippedRelic
                        ? equippedRelic.getRelicData(entity, equippedStack).getAbilitiesData().getAbilityData(ability)
                        : null)
                .filter(abilityData -> abilityData != null
                        && abilityData.getTemplate() != null
                        && abilityData.canPlayerUse(entity)
                        && predicate.test(abilityData))
                .toList();
    }

    private static AbilityData getFallbackAbilityData(LivingEntity entity, ItemStack stack, String ability) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return null;

        return relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);
    }

    public static double sumValue(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return sumValue(entity, stack, ability, stat, abilityData -> true);
    }

    public static double sumValue(LivingEntity entity, ItemStack stack, String ability, String stat, Predicate<AbilityData> predicate) {
        var fallback = getFallbackAbilityData(entity, stack, ability);
        var abilities = getActiveAbilityData(entity, stack, ability, predicate);

        if (abilities.isEmpty())
            return fallback == null ? 0D : clampByThreshold(fallback, stat, fallback.getStatData(stat).getValue());

        var reference = abilities.getFirst();
        var sum = abilities.stream().mapToDouble(abilityData -> abilityData.getStatData(stat).getValue()).sum();

        return clampByThreshold(reference, stat, sum);
    }

    public static double maxValue(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return maxValue(entity, stack, ability, stat, abilityData -> true);
    }

    public static double maxValue(LivingEntity entity, ItemStack stack, String ability, String stat, Predicate<AbilityData> predicate) {
        var fallback = getFallbackAbilityData(entity, stack, ability);
        var abilities = getActiveAbilityData(entity, stack, ability, predicate);

        if (abilities.isEmpty())
            return fallback == null ? 0D : clampByThreshold(fallback, stat, fallback.getStatData(stat).getValue());

        var reference = abilities.getFirst();
        var max = abilities.stream().mapToDouble(abilityData -> abilityData.getStatData(stat).getValue()).max().orElse(reference.getStatData(stat).getValue());

        return clampByThreshold(reference, stat, max);
    }

    public static double minValue(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return minValue(entity, stack, ability, stat, abilityData -> true);
    }

    public static double minValue(LivingEntity entity, ItemStack stack, String ability, String stat, Predicate<AbilityData> predicate) {
        var fallback = getFallbackAbilityData(entity, stack, ability);
        var abilities = getActiveAbilityData(entity, stack, ability, predicate);

        if (abilities.isEmpty())
            return fallback == null ? 0D : clampByThreshold(fallback, stat, fallback.getStatData(stat).getValue());

        var reference = abilities.getFirst();
        var min = abilities.stream().mapToDouble(abilityData -> abilityData.getStatData(stat).getValue()).min().orElse(reference.getStatData(stat).getValue());

        return clampByThreshold(reference, stat, min);
    }

    public static double bestValue(LivingEntity entity, ItemStack stack, String ability, String stat) {
        return bestValue(entity, stack, ability, stat, abilityData -> true);
    }

    public static double bestValue(LivingEntity entity, ItemStack stack, String ability, String stat, Predicate<AbilityData> predicate) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return 0D;

        var fallbackAbilityData = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);
        var abilities = getActiveAbilityData(entity, stack, ability, predicate);

        if (abilities.isEmpty())
            return clampByThreshold(fallbackAbilityData, stat, fallbackAbilityData.getStatData(stat).getValue());

        var reference = abilities.getFirst();
        var chosen = isLowerBetter(reference, stat)
                ? minValue(entity, stack, ability, stat, predicate)
                : maxValue(entity, stack, ability, stat, predicate);

        return clampByThreshold(reference, stat, chosen);
    }

}
