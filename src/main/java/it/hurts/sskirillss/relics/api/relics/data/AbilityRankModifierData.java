package it.hurts.sskirillss.relics.api.relics.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import it.hurts.sskirillss.relics.api.relics.RankModifierComponent;

import java.util.Collections;

public class AbilityRankModifierData {
    private final AbilityData abilityData;
    private final String rankModifier;

    public AbilityRankModifierData(AbilityData abilityData, String rankModifier) {
        this.abilityData = abilityData;
        this.rankModifier = rankModifier;
    }

    public String getId() {
        return this.rankModifier;
    }

    public RankModifierComponent getComponent() {
        var component = this.abilityData.getComponent();

        if (component == null)
            return RankModifierComponent.EMPTY;

        return component.getRankModifiers().getOrDefault(this.getId(), RankModifierComponent.EMPTY);
    }

    public void setComponent(RankModifierComponent component) {
        this.abilityData.setComponent(this.abilityData.getComponent().toBuilder()
                .rankModifier(this.getId(), component)
                .build());
    }

    public boolean isEnabled() {
        return this.isUnlocked() && this.getComponent().isEnabled();
    }

    public void setEnabled(boolean enabled) {
        this.setComponent(this.getComponent().toBuilder()
                .enabled(enabled)
                .build());
    }

    public boolean isUnlocked() {
        var template = this.abilityData.getTemplate();

        if (template == null)
            return false;

        var ranks = Multimaps.invertFrom(template.getRankModifiers(), HashMultimap.create()).get(this.getId());

        if (ranks.isEmpty())
            return false;

        return this.abilityData.getAbilitiesData().getRelicData().getLevelingData().getRank() >= Collections.max(ranks);
    }
}
