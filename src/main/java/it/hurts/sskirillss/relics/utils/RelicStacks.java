package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatStacking;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import it.hurts.sskirillss.relics.api.relics.data.RelicData;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public final class RelicStacks implements Iterable<RelicStacks.Entry> {
    private final LivingEntity entity;
    private final List<Entry> entries;

    private RelicStacks(LivingEntity entity, List<Entry> entries) {
        this.entity = entity;
        this.entries = entries;
    }

    public static RelicStacks of(LivingEntity entity, Item item, String ability) {
        return of(entity, item, ability, data -> true);
    }

    public static RelicStacks of(LivingEntity entity, Item item, String ability, Predicate<AbilityData> predicate) {
        return lookup(entity, item, ability).active(predicate);
    }

    public static RelicStacks forStat(LivingEntity entity, Item item, String ability, String stat) {
        return lookup(entity, item, ability).forStat(stat);
    }

    public static Lookup lookup(LivingEntity entity, Item item, String ability) {
        return lookup(entity, stack -> stack.is(item), ability);
    }

    public static Lookup lookup(LivingEntity entity, Predicate<ItemStack> filter, String ability) {
        var sources = new ArrayList<Source>();

        for (var located : RelicContainer.locate(entity, RelicsRegistries.RELIC_CONTAINER_REGISTRY, filter)) {
            if (!(located.stack().getItem() instanceof IRelicItem relic))
                continue;

            var data = relic.getRelicData(entity, located.stack()).getAbilitiesData().getAbilityData(ability);

            if (data != null)
                sources.add(new Source(new Entry(entity, located.stack(), relic, data), located.containers()));
        }
        return new Lookup(entity, sources);
    }

    private record Source(Entry entry, List<RelicContainer> containers) {
        boolean isInAbilityContainer() {
            return !Collections.disjoint(containers, entry.ability().getActiveContainers());
        }
    }

    public static final class Lookup {
        private final LivingEntity entity;
        private final List<Source> sources;

        private Lookup(LivingEntity entity, List<Source> sources) {
            this.entity = entity;
            this.sources = sources;
        }

        public RelicStacks located() {
            return select(Source::isInAbilityContainer);
        }

        public RelicStacks located(String stat) {
            return forStat(stat, false);
        }

        public RelicStacks active(Predicate<AbilityData> predicate) {
            return select(source -> {
                var ability = source.entry().ability();

                return source.isInAbilityContainer() && ability.canPlayerUse(entity) && predicate.test(ability);
            });
        }

        public RelicStacks forStat(String id) {
            return forStat(id, true);
        }

        private RelicStacks forStat(String id, boolean requireAvailable) {
            return select(source -> {
                var ability = source.entry().ability();
                var stat = ability.getStatData(id);
                var template = stat.getTemplate();

                return template != null && !Collections.disjoint(source.containers(), stat.getActiveContainers())
                        && (!requireAvailable || template.getStacking().isAvailable(ability));
            });
        }

        private RelicStacks select(Predicate<Source> predicate) {
            return new RelicStacks(entity, sources.stream().filter(predicate).map(Source::entry).toList());
        }

        public StatStacking stacking(String stat) {
            return StatStacking.fromParticipants(forStat(stat), stat);
        }
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
            return ability.getAbilitiesData().getRelicData();
        }

        public double stat(String stat) {
            return ability.getStatData(stat).getValue();
        }

        public double stackedStat(String stat) {
            return ability.getStatData(stat).getStackedValue();
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
