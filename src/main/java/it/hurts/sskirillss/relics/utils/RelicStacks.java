package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import it.hurts.sskirillss.relics.api.relics.data.RelicData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class RelicStacks implements Iterable<RelicStacks.Entry> {
    private final LivingEntity entity;
    private final List<Entry> entries;

    private RelicStacks(LivingEntity entity, List<Entry> entries) {
        this.entity = entity;
        this.entries = List.copyOf(entries);
    }

    public static RelicStacks of(LivingEntity entity, Item item, String ability) {
        return of(entity, item, ability, data -> true);
    }

    public static RelicStacks of(LivingEntity entity, Item item, String ability, Predicate<AbilityData> predicate) {
        var entries = new ArrayList<Entry>();

        for (var stack : EntityUtils.findEquippedCurios(entity, item)) {
            if (!(stack.getItem() instanceof IRelicItem relic) || entries.stream().anyMatch(existing -> existing.stack() == stack))
                continue;

            var data = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData(ability);

            if (data == null || !data.canPlayerUse(entity) || !predicate.test(data))
                continue;

            entries.add(new Entry(entity, stack, relic, data));
        }

        return new RelicStacks(entity, entries);
    }

    public static Predicate<AbilityData> modeIsNot(String mode) {
        return data -> !data.getMode().equals(mode);
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public List<ItemStack> getStacks() {
        return entries.stream().map(Entry::stack).toList();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public Entry getController() {
        return entries.isEmpty() ? null : entries.getFirst();
    }

    public ItemStack getControllerStack() {
        return entries.isEmpty() ? ItemStack.EMPTY : entries.getFirst().stack();
    }

    public boolean isController(ItemStack stack) {
        return !entries.isEmpty() && entries.getFirst().stack() == stack;
    }

    public boolean contains(ItemStack stack) {
        return entries.stream().anyMatch(entry -> entry.stack() == stack);
    }

    public RelicStacks filter(Predicate<Entry> predicate) {
        return new RelicStacks(entity, entries.stream().filter(predicate).toList());
    }

    public RelicStacks withRank(String rankModifier) {
        return filter(entry -> entry.rank(rankModifier));
    }

    public boolean any(String rankModifier) {
        return entries.stream().anyMatch(entry -> entry.rank(rankModifier));
    }

    public int count(String rankModifier) {
        return (int) entries.stream().filter(entry -> entry.rank(rankModifier)).count();
    }

    public double sum(String stat) {
        return sum(entry -> entry.stat(stat));
    }

    public double sum(ToDoubleFunction<Entry> mapper) {
        return entries.stream().mapToDouble(mapper).sum();
    }

    public double max(String stat) {
        return max(entry -> entry.stat(stat));
    }

    public double max(ToDoubleFunction<Entry> mapper) {
        return entries.stream().mapToDouble(mapper).max().orElse(0D);
    }

    public double min(String stat) {
        return min(entry -> entry.stat(stat));
    }

    public double min(ToDoubleFunction<Entry> mapper) {
        return entries.stream().mapToDouble(mapper).min().orElse(0D);
    }

    public Best best(ToDoubleFunction<Entry> mapper) {
        Best best = null;

        for (var entry : entries) {
            var value = mapper.applyAsDouble(entry);

            if (value > 0D && (best == null || value > best.value()))
                best = new Best(entry, value);
        }

        return best;
    }

    @Override
    public Iterator<Entry> iterator() {
        return entries.iterator();
    }

    public record Best(Entry entry, double value) {
    }

    public record Entry(LivingEntity entity, ItemStack stack, IRelicItem relic, AbilityData ability) {
        public RelicData relicData() {
            return relic.getRelicData(entity, stack);
        }

        public double stat(String stat) {
            return ability.getStatData(stat).getValue();
        }

        public boolean rank(String rankModifier) {
            return ability.getRankModifierData(rankModifier).isEnabled();
        }

        public String mode() {
            return ability.getMode();
        }

        public void addExperience(String source, double amount) {
            relicData().getLevelingData().addExperience(ability.getId(), source, amount);
        }

        public void addMetric(String metric, double value) {
            ability.getStatisticData().getMetricData(metric).addValue(value);
        }
    }
}
