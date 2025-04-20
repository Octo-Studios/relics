package it.hurts.sskirillss.relics.api.relics;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class RelicTemplate {
    @Builder.Default
    private AbilitiesTemplate abilities = AbilitiesTemplate.builder().build();

    @Builder.Default
    private LevelingTemplate leveling = LevelingTemplate.builder().build();

    @Builder.Default
    private StyleTemplate style = StyleTemplate.builder().build();

    @Builder.Default
    private LootTemplate loot = LootTemplate.builder().build();
}