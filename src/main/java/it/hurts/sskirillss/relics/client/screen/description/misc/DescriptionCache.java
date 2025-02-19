package it.hurts.sskirillss.relics.client.screen.description.misc;

import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionTab;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import lombok.*;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DescriptionCache {
    private static final Map<IRelicItem, CacheEntry> CACHE = new HashMap<>();

    public static CacheEntry getEntry(IRelicItem relic) {
        return CACHE.computeIfAbsent(relic, entry -> new CacheEntry());
    }

    public static void setEntry(IRelicItem relic, CacheEntry cache) {
        CACHE.put(relic, cache);
    }

    public static String getSelectedAbility(ItemStack stack) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return "";

        var cache = getEntry(relic);
        var index = cache.getSelectionIndex(DescriptionTab.ABILITY);
        var abilities = relic.getAbilitiesData().getAbilities().keySet().stream().filter(entry -> relic.isAbilityEnabled(stack, entry)).toList();
        var size = abilities.size();

        if (size == 0)
            return "";

        if (index >= size)
            index = size - 1;

        var ability = abilities.get(index);

        if (ability == null) {
            index = 0;

            ability = abilities.get(index);

            setEntry(relic, cache.toBuilder()
                    .selectionIndex(DescriptionTab.ABILITY, index)
                    .build());
        }

        return ability;
    }

    public static void setSelectedAbility(ItemStack stack, String ability) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var cache = getEntry(relic);

        var abilities = relic.getAbilitiesData().getAbilities().keySet().stream().filter(entry -> relic.isAbilityEnabled(stack, entry)).toList();

        if (!abilities.contains(ability))
            return;

        var index = abilities.indexOf(ability);

        setEntry(relic, cache.toBuilder()
                .selectionIndex(DescriptionTab.ABILITY, index)
                .build());
    }

    public static String getSelectedExperienceSource(ItemStack stack) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return "";

        var cache = getEntry(relic);
        var index = cache.getSelectionIndex(DescriptionTab.EXPERIENCE);
        var sources = relic.getLevelingSourcesData().getSources().keySet().stream().filter(entry -> relic.isLevelingSourceEnabled(stack, entry)).toList();
        var size = sources.size();

        if (size == 0)
            return "";

        if (index >= size)
            index = size - 1;

        var source = sources.get(index);

        if (source == null) {
            index = 0;

            source = sources.get(index);

            setEntry(relic, cache.toBuilder()
                    .selectionIndex(DescriptionTab.EXPERIENCE, index)
                    .build());
        }

        return source;
    }

    public static void setSelectedExperienceSource(ItemStack stack, String source) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var cache = getEntry(relic);

        var sources = relic.getLevelingSourcesData().getSources().keySet().stream().filter(entry -> relic.isLevelingSourceEnabled(stack, entry)).toList();

        if (!sources.contains(source))
            return;

        var index = sources.indexOf(source);

        setEntry(relic, cache.toBuilder()
                .selectionIndex(DescriptionTab.EXPERIENCE, index)
                .build());
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class CacheEntry {
        @Getter
        private DescriptionTab selectedPage = DescriptionTab.RELIC;

        private Map<DescriptionTab, Integer> selectionIndices = new HashMap<>() {{
            for (var page : DescriptionTab.values())
                put(page, 0);
        }};

        public int getSelectionIndex(DescriptionTab page) {
            return selectionIndices.computeIfAbsent(page, entry -> 0);
        }

        public static class CacheEntryBuilder {
            public CacheEntryBuilder selectionIndex(DescriptionTab page, int index) {
                selectionIndices.put(page, index);

                return this;
            }
        }
    }
}