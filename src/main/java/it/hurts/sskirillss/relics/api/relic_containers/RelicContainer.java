package it.hurts.sskirillss.relics.api.relic_containers;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class RelicContainer {
    public abstract Function<LivingEntity, List<ItemStack>> gatherRelics();

    protected void visitRelics(LivingEntity entity, Consumer<ItemStack> consumer) {
        gatherRelics().apply(entity).forEach(consumer);
    }

    public static List<LocatedStack> locate(LivingEntity entity, Iterable<RelicContainer> containers, Predicate<ItemStack> filter) {
        if (entity == null)
            return List.of();

        var locations = new IdentityHashMap<ItemStack, List<RelicContainer>>();
        var result = new ArrayList<LocatedStack>();

        for (var container : containers) {
            container.visitRelics(entity, stack -> {
                var sources = locations.get(stack);

                if (sources == null) {
                    if (!filter.test(stack))
                        return;

                    sources = new ArrayList<>();

                    locations.put(stack, sources);

                    result.add(new LocatedStack(stack, sources));
                }
                sources.add(container);
            });
        }
        return result;
    }

    public static List<ItemStack> find(LivingEntity entity, Iterable<RelicContainer> containers, Predicate<ItemStack> filter) {
        return locate(entity, containers, filter).stream().map(LocatedStack::stack).toList();
    }

    public static boolean contains(LivingEntity entity, ItemStack stack, List<RelicContainer> containers) {
        return entity != null && containers.stream().anyMatch(container -> container.gatherRelics().apply(entity).stream().anyMatch(candidate -> candidate == stack));
    }

    public record LocatedStack(ItemStack stack, List<RelicContainer> containers) {
    }

    public Function<Player, List<RelicStackReference>> gatherRelicReferences() {
        return player -> List.of();
    }

    public Function<Player, List<AbilityReference>> gatherAbilityReferences() {
        return player -> {
            var references = new ArrayList<AbilityReference>();

            for (var reference : this.gatherRelicReferences().apply(player)) {
                var stack = reference.getStack(player);

                if (!(stack.getItem() instanceof IRelicItem relic))
                    continue;

                var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();

                for (var abilityData : abilitiesData.getAbilities().values()) {
                    if (abilityData == null || !abilityData.isActive())
                        continue;

                    if (!abilityData.getActivationContainers().contains(this))
                        continue;

                    references.add(new AbilityReference(abilityData.getId(), reference));
                }

                if (!abilitiesData.getRelicData().getActiveContainers().contains(this))
                    continue;

                for (var synergyData : abilitiesData.getSynergies().values()) {
                    if (!synergyData.isUnlocked() || synergyData.getTemplate().getModes().isEmpty())
                        continue;

                    references.add(new AbilityReference(synergyData.getId(), reference, AbilityReference.Type.SYNERGY));
                }
            }

            return references;
        };
    }

    public static List<AbilityReference> findAbilities(Player player) {
        var references = new ArrayList<AbilityReference>();
        var seen = new IdentityHashMap<ItemStack, Set<AbilityKey>>();

        for (var container : RelicsRegistries.RELIC_CONTAINER_REGISTRY) {
            for (var reference : container.gatherAbilityReferences().apply(player)) {
                if (reference.stackReference().getContainer() == null)
                    continue;

                var stack = reference.stackReference().getStack(player);

                if (seen.computeIfAbsent(stack, ignored -> new HashSet<>()).add(new AbilityKey(reference.ability(), reference.type())))
                    references.add(reference);
            }
        }
        return references;
    }

    private record AbilityKey(String id, AbilityReference.Type type) {

    }
}
