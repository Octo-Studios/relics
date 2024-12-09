package it.hurts.sskirillss.relics.client.screen.description.misc;

import it.hurts.sskirillss.relics.client.screen.description.general.misc.DescriptionPage;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import lombok.*;

import java.util.ArrayList;
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

    public static String getSelectedAbility(IRelicItem relic) {
        var cache = getEntry(relic);
        var index = cache.getSelectionIndex(DescriptionPage.ABILITY);
        var abilities = relic.getAbilitiesData().getAbilities().keySet().stream().toList();
        var ability = abilities.get(index);

        if (ability == null) {
            index = 0;

            ability = abilities.get(index);

            setEntry(relic, cache.toBuilder()
                    .selectionIndex(DescriptionPage.ABILITY, index)
                    .build());
        }

        return ability;
    }

    public static void setSelectedAbility(IRelicItem relic, String ability) {
        var cache = getEntry(relic);

        var abilities = relic.getAbilitiesData().getAbilities();

        if (!abilities.containsKey(ability))
            return;

        var index = new ArrayList<>(abilities.keySet()).indexOf(ability);

        setEntry(relic, cache.toBuilder()
                .selectionIndex(DescriptionPage.ABILITY, index)
                .build());
    }

    public static String getSelectedExperienceSource(IRelicItem relic) {
        var cache = getEntry(relic);
        var index = cache.getSelectionIndex(DescriptionPage.EXPERIENCE);
        var sources = relic.getLevelingSourcesData().getSources().keySet().stream().toList();
        var source = sources.get(index);

        if (source == null) {
            index = 0;

            source = sources.get(index);

            setEntry(relic, cache.toBuilder()
                    .selectionIndex(DescriptionPage.EXPERIENCE, index)
                    .build());
        }

        return source;
    }

    public static void setSelectedExperienceSource(IRelicItem relic, String source) {
        var cache = getEntry(relic);

        var sources = relic.getLevelingSourcesData().getSources();

        if (!sources.containsKey(source))
            return;

        var index = new ArrayList<>(sources.keySet()).indexOf(source);

        setEntry(relic, cache.toBuilder()
                .selectionIndex(DescriptionPage.EXPERIENCE, index)
                .build());
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class CacheEntry {
        @Getter
        private DescriptionPage selectedPage = DescriptionPage.RELIC;

        private Map<DescriptionPage, Integer> selectionIndices = new HashMap<>() {{
            for (var page : DescriptionPage.values())
                put(page, 0);
        }};

        public int getSelectionIndex(DescriptionPage page) {
            return selectionIndices.computeIfAbsent(page, entry -> 0);
        }

        public static class CacheEntryBuilder {
            public CacheEntryBuilder selectionIndex(DescriptionPage page, int index) {
                selectionIndices.put(page, index);

                return this;
            }
        }
    }
}