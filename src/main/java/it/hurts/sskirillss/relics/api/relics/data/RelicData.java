package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicComponent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;

public class RelicData {
    private final IRelicItem relic;
    @Nullable
    private final LivingEntity entity;
    private final ItemStack stack;

    public RelicData(IRelicItem relic, @Nullable LivingEntity entity, ItemStack stack) {
        this.relic = relic;
        this.entity = entity;
        this.stack = stack;
    }

    public IRelicItem getRelic() {
        return relic;
    }

    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    public ItemStack getStack() {
        return stack;
    }

    public RelicTemplate getTemplate() {
        return relic.getRelicTemplate(entity, stack);
    }

    public RelicComponent getComponent() {
        return stack.getOrDefault(RelicsDataComponents.DATA, RelicComponent.EMPTY);
    }

    public void setComponent(RelicComponent component) {
        stack.set(RelicsDataComponents.DATA, component);
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

    public LootTemplate getLootTemplate() {
        return getTemplate().getLoot();
    }

    public int calculateMaxLevel() {
        return getTemplate().getAbilities().getAbilities().values().stream()
                .mapToInt(template -> template.getInitialMaxLevel() * template.getRequiredPoints())
                .sum();
    }

    public int getMaxQuality() {
        return 10;
    }

    public int calculateQuality() {
        var abilities = getTemplate().getAbilities().getAbilities();

        if (abilities.isEmpty())
            return 0;

        var abilitiesData = getAbilitiesData();

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
        var levelingData = getLevelingData();
        var unspentPoints = levelingData.getPoints();
        var template = getTemplate().getLeveling();
        var rank = levelingData.getRank();
        var maxRank = template.getMaxRank();
        var level = levelingData.getLevel();
        var maxLevel = calculateMaxLevel();
        var quality = calculateQuality();
        var maxQuality = getMaxQuality();

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

    public void spreadExperience(int experience) {
        spreadExperience(experience, 0.25D);
    }

    public void spreadExperience(int experience, double percentage) {
        var isMaxLevel = isMaxLevel();

        var toSpread = isMaxLevel ? 0 : experience * percentage;

        if (!isMaxLevel)
            getLevelingData().addExperience(experience);

        if (toSpread <= 0 || entity == null)
            return;

        var relics = RelicsRegistries.RELIC_CONTAINER_REGISTRY.entrySet().stream()
                .map(entry -> entry.getValue())
                .flatMap(source -> source.gatherRelics().apply(entity).stream())
                .filter(entry -> entry.getItem() instanceof IRelicItem relicItem
                        && !relicItem.getRelicData(entity, entry).isMaxLevel()
                        && !stack.equals(entry))
                .toList();

        if (relics.isEmpty())
            return;

        var relicStack = relics.get(entity.level().getRandom().nextInt(relics.size()));

        if (relicStack.getItem() instanceof IRelicItem relicItem)
            relicItem.getRelicData(entity, relicStack).getLevelingData().addExperience(toSpread);
    }

    public boolean isMaxRank() {
        return getLevelingData().getRank() >= getTemplate().getLeveling().getMaxRank();
    }

    public boolean isMaxLevel() {
        return getLevelingData().getLevel() >= calculateMaxLevel();
    }

    public boolean isMaxQuality() {
        return calculateQuality() >= getMaxQuality();
    }

    public boolean isFlawless() {
        return calculateProgress() >= 1F;
    }
}
