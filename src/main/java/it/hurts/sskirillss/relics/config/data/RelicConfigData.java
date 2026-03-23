package it.hurts.sskirillss.relics.config.data;

import it.hurts.octostudios.octolib.module.config.annotation.IgnoreProp;
import it.hurts.octostudios.octolib.module.config.impl.OctoConfig;
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

        this.setAbilitiesData(relic.getDefaultAbilitiesTemplate().toConfigData());
        this.setLevelingData(relic.getDefaultLevelingTemplate().toConfigData());
        this.setLootData(relic.getDefaultLootTemplate().toConfigData());
    }

    public RelicTemplate toData(IRelicItem relic) {
        return relic.getDefaultRelicTemplate().toBuilder()
                .abilities(abilitiesData.toData(relic))
                .leveling(levelingData.toData(relic))
                .loot(lootData.toData(relic))
                .build();
    }

    private AbilitiesConfigData abilitiesData;

    private LevelingConfigData levelingData;

    private LootConfigData lootData;

    @Override
    public void onLoadObject(Object object) {
        relic.setDefaultRelicTemplate(((RelicConfigData) object).toData(relic));

        RelicLootModifier.processRelicCache(relic);
    }
}