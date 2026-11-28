package it.hurts.sskirillss.relics.api.relics.abilities.stats;

import it.hurts.sskirillss.relics.api.relics.data.AbilityStatData;
import it.hurts.sskirillss.relics.utils.RelicStacks;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalDouble;

public final class StatStacking {
    private final AbilityStatTemplate template;
    private final List<Entry> entries;

    private StatStacking(AbilityStatTemplate template, List<Entry> entries) {
        this.template = template;
        this.entries = List.copyOf(entries);
    }

    public static StatStacking of(AbilityStatData stat) {
        var template = stat.getTemplate();

        if (template == null || !template.getStacking().getType().ranksCopiesByStat())
            return new StatStacking(template, List.of());

        var ability = stat.getAbilityData();
        var relic = ability.getAbilitiesData().getRelicData();
        var copies = RelicStacks.forStat(relic.getEntity(), relic.getStack().getItem(), ability.getId(), stat.getId());

        return of(copies, stat.getId(), template);
    }

    public static StatStacking fromParticipants(RelicStacks copies, String id) {
        var participants = copies.filter(entry -> entry.ability().getStatData(id).getTemplate() != null);
        var template = participants.isEmpty() ? null : participants.getController().ability().getStatData(id).getTemplate();

        return of(participants, id, template);
    }

    private static StatStacking of(RelicStacks copies, String id, AbilityStatTemplate template) {
        if (template == null || !template.getStacking().getType().ranksCopiesByStat())
            return new StatStacking(template, List.of());

        var requested = template.getStacking();
        var values = new ArrayList<Value>();
        var groupTemplate = template;

        for (var entry : copies) {
            var candidate = entry.ability().getStatData(id);
            var candidateTemplate = candidate.getTemplate();
            var rule = candidateTemplate.getStacking();

            if (rule.getType() != requested.getType() || rule.getNeutralValue() != requested.getNeutralValue()
                    || candidateTemplate.isLowerBetter() != template.isLowerBetter()
                    || (candidateTemplate.getRounding() == null) != (template.getRounding() == null))
                continue;

            if (values.isEmpty())
                groupTemplate = candidateTemplate;

            values.add(new Value(entry, candidate.getRawValue()));
        }

        var order = Comparator.comparingDouble(Value::value);

        values.sort(template.isLowerBetter() ? order : order.reversed());

        var entries = new ArrayList<Entry>();
        var counts = new HashMap<Item, Integer>();
        var stacking = groupTemplate.getStacking();
        var runningSum = groupTemplate.getRounding() != null && stacking.getType() == StatStackingType.ADDITIVE && stacking.getReducer() == null
                ? new DoubleSummaryStatistics() : null;
        var allocated = 0D;

        for (var value : values) {
            var position = counts.merge(value.source().stack().getItem(), 1, Integer::sum);
            var efficiency = Math.scalb(1D, 1 - position);
            var contribution = weightedValue(value.value(), efficiency, stacking.getNeutralValue());

            if (runningSum != null) {
                runningSum.accept(contribution);
                var rounded = groupTemplate.resolveValue(runningSum.getSum());
                contribution = rounded - allocated;
                allocated = rounded;
            } else
                contribution = groupTemplate.resolveValue(contribution);

            entries.add(new Entry(value.source(), value.value(), efficiency, contribution));
        }
        return new StatStacking(groupTemplate, entries);
    }

    private static double weightedValue(double value, double efficiency, double neutralValue) {
        return efficiency == 1D ? value : Mth.lerp(efficiency, neutralValue, value);
    }

    public List<Entry> entries() {
        return entries;
    }

    public OptionalDouble efficiency(ItemStack stack) {
        return entries.stream().filter(entry -> entry.stack() == stack).mapToDouble(Entry::efficiency).findFirst();
    }

    public OptionalDouble contribution(ItemStack stack) {
        return entries.stream().filter(entry -> entry.stack() == stack).mapToDouble(Entry::contribution).findFirst();
    }

    private OptionalDouble rawTotal() {
        if (entries.isEmpty())
            return OptionalDouble.empty();

        var stacking = template.getStacking();
        var values = entries.stream().mapToDouble(entry -> weightedValue(entry.value(), entry.efficiency(), stacking.getNeutralValue()));
        var reducer = stacking.getReducer();

        if (reducer != null)
            return values.reduce(reducer);

        return switch (stacking.getType()) {
            case ADDITIVE -> OptionalDouble.of(values.sum());
            case MULTIPLICATIVE -> {
                var neutralValue = stacking.getNeutralValue();

                yield values.reduce((a, b) -> a + (b - neutralValue) + (a - neutralValue) * (b - neutralValue));
            }
            case SEQUENTIAL -> values.reduce(StatStackingType::combineReductions);
            case BEST -> values.findFirst();
            default -> OptionalDouble.empty();
        };
    }

    public OptionalDouble total() {
        var rawTotal = rawTotal();

        return rawTotal.isPresent() ? OptionalDouble.of(template.resolveValue(rawTotal.getAsDouble())) : OptionalDouble.empty();
    }

    public double clampedTotal(double fallback) {
        var rawTotal = rawTotal();

        if (rawTotal.isEmpty())
            return fallback;

        var threshold = template.getThresholdValue();

        return template.resolveValue(Math.clamp(rawTotal.getAsDouble(), threshold.getMinValue(), threshold.getMaxValue()));
    }

    private record Value(RelicStacks.Entry source, double value) {

    }

    public record Entry(RelicStacks.Entry source, double value, double efficiency, double contribution) {
        public ItemStack stack() {
            return source.stack();
        }
    }
}
