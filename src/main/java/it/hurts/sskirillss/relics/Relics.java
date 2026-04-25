package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.events.relic.GatherRelicTemplateEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicComponent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.Arrays;

public class RelicData {
    private final IRelicItem relic;
    @Nullable
    private final LivingEntity entity;
    private final ItemStack stack;
    @Nullable
    private RelicTemplate cachedTemplate;

    public RelicData(IRelicItem relic, @Nullable LivingEntity entity, ItemStack stack) {
        this.relic = relic;
        this.entity = entity;
        this.stack = stack;
    }

    public IRelicItem getRelic() {
        return this.relic;
    }

    @Nullable
    public LivingEntity getEntity() {
        return this.entity;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public RelicTemplate getTemplate() {
        if (this.cachedTemplate != null)
            return this.cachedTemplate;

        var template = this.getRelic().getDefaultRelicTemplate();
        var event = new GatherRelicTemplateEvent(this.getEntity(), this.getStack(), template);

        NeoForge.EVENT_BUS.post(event);

        this.cachedTemplate = event.getTemplate();

        return this.cachedTemplate;
    }

    public void invalidateTemplateCache() {
        this.cachedTemplate = null;
    }

    public RelicComponent getComponent() {
        return this.getStack().getOrDefault(RelicsDataComponents.RELIC, RelicComponent.EMPTY);
    }

    public void setComponent(RelicComponent component) {
        this.getStack().set(RelicsDataComponents.RELIC, component);
    }

    public AbilitiesData getAbilitiesData() {
        return new AbilitiesData(this);
    }

    public LevelingData getLevelingData() {
        return new LevelingData(this);
    }

    public RelicStatisticData getStatisticData() {
        return new RelicStatisticData(this);
    }

    public int calculateMaxLevel() {
        return this.getTemplate().getAbilities().getAbilities().values().stream()
                .mapToInt(template -> template.getInitialMaxLevel() * template.getRequiredPoints())
                .sum();
    }

    public int getMaxQuality() {
        return 10;
    }

    public int calculateQuality() {
        var abilities = this.getTemplate().getAbilities().getAbilities();

        if (abilities.isEmpty())
            return 0;

        var abilitiesData = this.getAbilitiesData();

        var filtered = abilities.keySet().stream()
                .filter(ability -> {
                    var abilityData = abilitiesData.getAbilityData(ability);
                    return abilityData != null && abilityData.canBeUpgraded() && abilityData.isUnlocked();
                })
                .mapToInt(ability -> abilitiesData.getAbilityData(ability).calculateQuality())
                .toArray();

        if (filtered.length == 0)
            return 0;

        var avg = Arrays.stream(filtered).average().orElse(0);

        var min = 0;
        var max = getMaxQuality();

        if (avg == min)
            return min;

        if (avg == max)
            return max;

        return (int) Mth.clamp(Math.floor(avg), min + 1, max - 1);
    }

    public double calculateProgress() {
        var levelingData = this.getLevelingData();
        var unspentPoints = levelingData.getPoints();
        var template = this.getTemplate().getLeveling();
        var rank = levelingData.getRank();
        var maxRank = template.getMaxRank();
        var level = levelingData.getLevel();
        var maxLevel = this.calculateMaxLevel();
        var quality = this.calculateQuality();
        var maxQuality = this.getMaxQuality();

        var adjustedUnits = Math.max(0.0, Math.min(level - (unspentPoints * 0.5), maxLevel));

        var levelFraction = (maxLevel > 0) ? (adjustedUnits / maxLevel) : 0.0;
        var totalSegments = Math.max(1, maxRank + 1);
        var completedSegments = Math.max(0, Math.min(rank, maxRank));

        var baseProgress = (completedSegments + levelFraction) / totalSegments;
        var qualityRatio = (maxQuality > 0) ? (quality / (double) maxQuality) : 0.0;
        var maxQualityWeight = 0.2;
        var qualityContribution = qualityRatio * maxQualityWeight * (1 - baseProgress);

        var progress = baseProgress + qualityContribution;

        return Math.min(1.0, Math.max(0.0, progress));
    }

    public boolean isMaxRank() {
        return this.getLevelingData().getRank() >= this.getTemplate().getLeveling().getMaxRank();
    }

    public boolean isMaxLevel() {
        return this.getLevelingData().getLevel() >= this.calculateMaxLevel();
    }

    public boolean isMaxQuality() {
        return this.calculateQuality() >= this.getMaxQuality();
    }

    public boolean isFlawless() {
        return this.calculateProgress() >= 1F;
    }
}package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.events.relic.GatherRelicTemplateEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicComponent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.Arrays;

public class RelicData {
    private final IRelicItem relic;
    @Nullable
    private final LivingEntity entity;
    private final ItemStack stack;
    @Nullable
    private RelicTemplate cachedTemplate;

    public RelicData(IRelicItem relic, @Nullable LivingEntity entity, ItemStack stack) {
        this.relic = relic;
        this.entity = entity;
        this.stack = stack;
    }

    public IRelicItem getRelic() {
        return this.relic;
    }

    @Nullable
    public LivingEntity getEntity() {
        return this.entity;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public RelicTemplate getTemplate() {
        if (this.cachedTemplate != null)
            return this.cachedTemplate;

        var template = this.getRelic().getDefaultRelicTemplate();
        var event = new GatherRelicTemplateEvent(this.getEntity(), this.getStack(), template);

        NeoForge.EVENT_BUS.post(event);

        this.cachedTemplate = event.getTemplate();

        return this.cachedTemplate;
    }

    public void invalidateTemplateCache() {
        this.cachedTemplate = null;
    }

    public RelicComponent getComponent() {
        return this.getStack().getOrDefault(RelicsDataComponents.RELIC, RelicComponent.EMPTY);
    }

    public void setComponent(RelicComponent component) {
        this.getStack().set(RelicsDataComponents.RELIC, component);
    }

    public AbilitiesData getAbilitiesData() {
        return new AbilitiesData(this);
    }

    public LevelingData getLevelingData() {
        return new LevelingData(this);
    }

    public RelicStatisticData getStatisticData() {
        return new RelicStatisticData(this);
    }

    public int calculateMaxLevel() {
        return this.getTemplate().getAbilities().getAbilities().values().stream()
                .mapToInt(template -> template.getInitialMaxLevel() * template.getRequiredPoints())
                .sum();
    }

    public int getMaxQuality() {
        return 10;
    }

    public int calculateQuality() {
        var abilities = this.getTemplate().getAbilities().getAbilities();

        if (abilities.isEmpty())
            return 0;

        var abilitiesData = this.getAbilitiesData();

        var filtered = abilities.keySet().stream()
                .filter(ability -> {
                    var abilityData = abilitiesData.getAbilityData(ability);
                    return abilityData != null && abilityData.canBeUpgraded() && abilityData.isUnlocked();
                })
                .mapToInt(ability -> abilitiesData.getAbilityData(ability).calculateQuality())
                .toArray();

        if (filtered.length == 0)
            return 0;

        var avg = Arrays.stream(filtered).average().orElse(0);

        var min = 0;
        var max = getMaxQuality();

        if (avg == min)
            return min;

        if (avg == max)
            return max;

        return (int) Mth.clamp(Math.floor(avg), min + 1, max - 1);
    }

    public double calculateProgress() {
        var levelingData = this.getLevelingData();
        var unspentPoints = levelingData.getPoints();
        var template = this.getTemplate().getLeveling();
        var rank = levelingData.getRank();
        var maxRank = template.getMaxRank();
        var level = levelingData.getLevel();
        var maxLevel = this.calculateMaxLevel();
        var quality = this.calculateQuality();
        var maxQuality = this.getMaxQuality();

        var adjustedUnits = Math.max(0.0, Math.min(level - (unspentPoints * 0.5), maxLevel));

        var levelFraction = (maxLevel > 0) ? (adjustedUnits / maxLevel) : 0.0;
        var totalSegments = Math.max(1, maxRank + 1);
        var completedSegments = Math.max(0, Math.min(rank, maxRank));

        var baseProgress = (completedSegments + levelFraction) / totalSegments;
        var qualityRatio = (maxQuality > 0) ? (quality / (double) maxQuality) : 0.0;
        var maxQualityWeight = 0.2;
        var qualityContribution = qualityRatio * maxQualityWeight * (1 - baseProgress);

        var progress = baseProgress + qualityContribution;

        return Math.min(1.0, Math.max(0.0, progress));
    }

    public boolean isMaxRank() {
        return this.getLevelingData().getRank() >= this.getTemplate().getLeveling().getMaxRank();
    }

    public boolean isMaxLevel() {
        return this.getLevelingData().getLevel() >= this.calculateMaxLevel();
    }

    public boolean isMaxQuality() {
        return this.calculateQuality() >= this.getMaxQuality();
    }

    public boolean isFlawless() {
        return this.calculateProgress() >= 1F;
    }
}
