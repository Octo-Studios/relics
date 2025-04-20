package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.modules.config.annotations.IgnoreProp;
import it.hurts.octostudios.octolib.modules.config.impl.OctoConfig;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
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

    public RelicTemplate toData(IRelicItem relic) {
        RelicTemplate data = relic.getRelicTemplate();

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
        relic.setRelicTemplate(((RelicConfigData) object).toData(relic));

        RelicLootModifier.processRelicCache(relic);
    }
}