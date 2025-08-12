package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.module.config.annotation.IgnoreProp;
import it.hurts.octostudios.octolib.module.config.impl.OctoConfig;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.level.RelicLootModifier;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RelicConfigData implements OctoConfig {
    @IgnoreProp
    private IRelicItem relic;

    public RelicConfigData(IRelicItem relic) {
        this.relic = relic;

        this.setAbilitiesData(relic.getAbilitiesData().toConfigData());
        this.setLevelingData(relic.getLevelingData().toConfigData());
        this.setLootData(relic.getLootData().toConfigData());
    }

    public RelicData toData(IRelicItem relic) {
        RelicData data = relic.getRelicData();

        data.setAbilities(abilitiesData.toData(relic));
        data.setLeveling(levelingData.toData(relic));
        data.setLoot(lootData.toData(relic));

        return data;
    }

    private AbilitiesConfigData abilitiesData;

    private LevelingConfigData levelingData;

    private LootConfigData lootData;

    @Override
    public void onLoadObject(Object object) {
        relic.setRelicData(((RelicConfigData) object).toData(relic));

        RelicLootModifier.processRelicCache(relic);
    }
}