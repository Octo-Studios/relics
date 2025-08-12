package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.module.config.annotation.Prop;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LootConfigData {
    @Prop(comment = "List of conditions for obtaining the relic. Supports both direct ID specification and regular expressions.")
    private List<LootEntryConfigData> entries;

    public LootData toData(IRelicItem relic) {
        LootData data = relic.getLootData();

        data.setEntries(entries.stream().map(entry -> new LootEntry(entry.getDimensions(), entry.getBiomes(), entry.getTables(), entry.getWeight())).toList());

        return data;
    }
}