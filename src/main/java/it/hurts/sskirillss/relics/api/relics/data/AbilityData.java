package it.hurts.sskirillss.relics.api.relics.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import it.hurts.sskirillss.relics.api.relics.LockComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AbilityData {
    private final AbilitiesData abilitiesData;
    private final String ability;

    public AbilityData(AbilitiesData abilitiesData, String ability) {
        this.abilitiesData = abilitiesData;
        this.ability = ability;
    }

    public AbilitiesData getAbilitiesData() {
        return abilitiesData;
    }

    public String getId() {
        return ability;
    }

    public AbilityTemplate getTemplate() {
        return abilitiesData.getTemplate().getAbilities().get(ability);
    }

    public AbilityComponent getComponent() {
        var template = getTemplate();

        if (template == null)
            return null;

        var abilitiesComponent = abilitiesData.getComponent();
        var abilityComponent = abilitiesComponent.getAbilities().get(ability);

        if (abilityComponent != null)
            return abilityComponent;

        abilityComponent = AbilityComponent.EMPTY;

        if (template.getRequiredLevel() <= 0)
            abilityComponent = abilityComponent.toBuilder()
                    .lock(LockComponent.builder()
                            .unlocks(new LockData(this).getMaxUnlocks())
                            .build())
                    .build();

        abilitiesData.setComponent(abilitiesComponent.toBuilder()
                .ability(ability, abilityComponent)
                .build());

        return abilityComponent;
    }

    public void setComponent(AbilityComponent component) {
        abilitiesData.setComponent(abilitiesData.getComponent().toBuilder()
                .ability(ability, component)
                .build());
    }

    public AbilityStatData getStatData(String stat) {
        return new AbilityStatData(this, stat);
    }

    public AbilityStatisticData getStatisticData() {
        return new AbilityStatisticData(this);
    }

    public ResearchData getResearchData() {
        return new ResearchData(this);
    }

    public LockData getLockData() {
        return new LockData(this);
    }

    public int getLevel() {
        return getComponent().getPoints();
    }

    public void setLevel(int points) {
        setComponent(getComponent().toBuilder()
                .points(points)
                .build());
    }

    public void addLevel(int points) {
        setLevel(getLevel() + points);
    }

    public String getMode() {
        var template = getTemplate();

        if (template == null)
            return "";

        var mode = getComponent().getMode();

        return mode.isEmpty() ? template.getModes().getFirst() : mode;
    }

    public void setMode(String mode) {
        setComponent(getComponent().toBuilder()
                .mode(mode)
                .build());
    }

    public boolean isRankModifierUnlocked(String rankModifier) {
        var template = getTemplate();

        if (template == null)
            return false;

        var modifiers = Multimaps.invertFrom(template.getRankModifiers(), HashMultimap.create());

        return abilitiesData.getRelicData().getLevelingData().getRank() >= Collections.max(modifiers.get(rankModifier));
    }

    public void randomizeStats() {
        var template = getTemplate();

        if (template == null)
            return;

        var stats = template.getStats();

        if (stats.isEmpty())
            return;

        var entity = abilitiesData.getRelicData().getEntity();
        var random = entity == null ? RandomSource.create() : entity.getRandom();

        double targetQuality;

        do {
            targetQuality = random.nextInt(getMaxQuality() + 1);
        } while (targetQuality == calculateQuality());

        var sumQuality = 0D;

        var generatedQualities = new HashMap<String, Double>();

        for (var stat : stats.keySet()) {
            var statData = getStatData(stat);
            var randomQuality = MathUtils.randomBetween(random, 0D, statData.getMaxQuality());

            generatedQualities.put(stat, randomQuality);

            sumQuality += randomQuality;
        }

        var currentAverageQuality = sumQuality / stats.size();

        while (Math.abs(currentAverageQuality - targetQuality) > 0.01) {
            if (currentAverageQuality < targetQuality) {
                var minStat = generatedQualities.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
                var statData = getStatData(minStat);

                var increment = Math.min((targetQuality - currentAverageQuality) * stats.size(), statData.getMaxQuality() - generatedQualities.get(minStat));

                generatedQualities.put(minStat, generatedQualities.get(minStat) + increment);
            } else if (currentAverageQuality > targetQuality) {
                var maxStat = generatedQualities.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();

                var decrement = Math.min((currentAverageQuality - targetQuality) * stats.size(), generatedQualities.get(maxStat));

                generatedQualities.put(maxStat, generatedQualities.get(maxStat) - decrement);
            }

            sumQuality = generatedQualities.values().stream().mapToDouble(Double::doubleValue).sum();

            currentAverageQuality = sumQuality / stats.size();
        }

        for (var entry : generatedQualities.entrySet()) {
            var stat = entry.getKey();
            var statData = getStatData(stat);

            if (statData.getOverrideValue().isPresent())
                statData.setOverrideValue(null);

            statData.setInitialQuality((int) Math.round(entry.getValue()));
        }
    }

    public void randomizeStat(String stat) {
        var entity = abilitiesData.getRelicData().getEntity();
        var random = entity == null ? RandomSource.create() : entity.getRandom();

        getStatData(stat).setInitialQuality(random.nextInt(getStatData(stat).getMaxQuality() + 1));
    }

    public boolean isEnoughLevel() {
        var template = getTemplate();

        return template != null && abilitiesData.getRelicData().getLevelingData().getLevel() >= template.getRequiredLevel();
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isUnlocked() {
        return isEnabled() && isEnoughLevel() && getLockData().isUnlocked() && getResearchData().isResearched();
    }

    public boolean canPlayerUse(LivingEntity entity) {
        return isUnlocked();
    }

    public boolean mayUnlock() {
        return isEnoughLevel() && !getLockData().isUnlocked();
    }

    public boolean mayResearch() {
        return isEnoughLevel() && getLockData().isUnlocked() && !getResearchData().isResearched();
    }

    public int getUpgradePlayerExperienceCost() {
        return (getLevel() + 1) * 50;
    }

    public boolean canBeUpgraded() {
        var template = getTemplate();

        return template.getInitialMaxLevel() > 0 && !template.getStats().isEmpty();
    }

    public boolean mayUpgrade() {
        var template = getTemplate();

        return template != null
                && template.getInitialMaxLevel() > 0 && !template.getStats().isEmpty()
                && !isMaxLevel()
                && abilitiesData.getRelicData().getLevelingData().getPoints() >= template.getRequiredPoints()
                && isUnlocked();
    }

    public boolean mayPlayerUpgrade(Player player) {
        return mayUpgrade() && EntityUtils.getPlayerTotalExperience(player) >= getUpgradePlayerExperienceCost();
    }

    public boolean upgrade(Player player) {
        if (!mayPlayerUpgrade(player))
            return false;

        player.giveExperiencePoints(-getUpgradePlayerExperienceCost());

        setLevel(getLevel() + 1);
        abilitiesData.getRelicData().getLevelingData().addPoints(-getTemplate().getRequiredPoints());

        return true;
    }

    public int getRerollPlayerExperienceCost() {
        return 150;
    }

    public boolean mayReroll() {
        var template = getTemplate();

        return template != null && !template.getStats().isEmpty() && isUnlocked();
    }

    public boolean mayPlayerReroll(Player player) {
        return mayReroll() && EntityUtils.getPlayerTotalExperience(player) >= getRerollPlayerExperienceCost();
    }

    public boolean reroll(Player player) {
        if (!mayPlayerReroll(player))
            return false;

        player.giveExperiencePoints(-getRerollPlayerExperienceCost());

        randomizeStats();

        return true;
    }

    public int getResetPlayerExperienceCost() {
        return getLevel() * 250;
    }

    public boolean mayReset() {
        return getLevel() > 0 && isUnlocked();
    }

    public boolean mayPlayerReset(Player player) {
        var template = getTemplate();

        return template != null && !template.getStats().isEmpty()
                && mayReset()
                && EntityUtils.getPlayerTotalExperience(player) >= getResetPlayerExperienceCost();
    }

    public boolean reset(Player player) {
        if (!mayPlayerReset(player))
            return false;

        player.giveExperiencePoints(-getResetPlayerExperienceCost());

        abilitiesData.getRelicData().getLevelingData().addPoints(getLevel() * getTemplate().getRequiredPoints());
        setLevel(0);

        return true;
    }

    public int getMaxQuality() {
        return 10;
    }

    public int calculateQuality() {
        var template = getTemplate();

        if (template == null)
            return 0;

        var stats = template.getStats();

        if (stats.isEmpty())
            return getMaxQuality();

        var avg = stats.keySet().stream()
                .mapToInt(stat -> getStatData(stat).getQuality())
                .average()
                .orElse(0);

        var min = 0;
        var max = getMaxQuality();

        if (avg == min)
            return min;

        if (avg == max)
            return max;

        return (int) Mth.clamp(Math.floor(avg), min + 1, max - 1);
    }

    public boolean isMaxLevel() {
        var template = getTemplate();

        return template != null && getLevel() >= template.getInitialMaxLevel();
    }

    public boolean isMaxQuality() {
        return calculateQuality() >= getMaxQuality();
    }

    public boolean isUpgradeEnabled() {
        var template = getTemplate();

        return template != null && isUnlocked() && !template.getStats().isEmpty();
    }

    public boolean isRerollEnabled() {
        var template = getTemplate();

        return template != null && isUnlocked() && !template.getStats().isEmpty();
    }

    public boolean isResetEnabled() {
        var template = getTemplate();

        return template != null && isUnlocked() && !template.getStats().isEmpty();
    }
}
