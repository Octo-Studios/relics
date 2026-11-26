package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.events.relic.leveling.RelicExperienceChangeEvent;
import it.hurts.sskirillss.relics.api.events.relic.leveling.RelicLevelChangeEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.LevelingComponent;
import it.hurts.sskirillss.relics.handlers.RankHandler;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;

public class LevelingData {
    private final RelicData relicData;

    public LevelingData(RelicData relicData) {
        this.relicData = relicData;
    }

    public LevelingTemplate getTemplate() {
        return this.getRelicData().getTemplate().getLeveling();
    }

    public LevelingComponent getComponent() {
        return this.getRelicData().getComponent().getLeveling();
    }

    public void setComponent(LevelingComponent component) {
        this.getRelicData().setComponent(this.getRelicData().getComponent().toBuilder()
                .leveling(component)
                .build());
    }

    public RelicData getRelicData() {
        return this.relicData;
    }

    public int getLevel() {
        return getComponent().getLevel();
    }

    public double getLevelProgress(int rank) {
        return getLevelProgress(rank, 0);
    }

    public double getLevelProgress(int rank, int startLevel) {
        var maxLevel = relicData.calculateMaxLevel(rank);

        return maxLevel > startLevel ? Math.clamp((double) (getLevel() - startLevel) / (maxLevel - startLevel), 0D, 1D) : 0D;
    }

    public void setLevel(int level) {
        setComponent(getComponent().toBuilder()
                .level(Math.max(0, level))
                .build());
    }

    public boolean addLevel(int level) {
        var currentLevel = getLevel();
        var maxLevel = this.getRelicData().calculateMaxLevel();

        var allowedDelta = level > 0
                ? Math.min(level, maxLevel - currentLevel)
                : (level < 0 ? Math.max(level, -currentLevel) : 0);

        if (allowedDelta == 0)
            return false;

        var event = new RelicLevelChangeEvent(this.getRelicData().getEntity(), this.getRelicData().getStack(), allowedDelta);

        if (NeoForge.EVENT_BUS.post(event).isCanceled() || event.getDelta() == 0)
            return false;

        setLevel(currentLevel + event.getDelta());

        return true;
    }

    public double getExperience() {
        return getComponent().getExperience();
    }

    public void setExperience(double experience) {
        var level = getLevel();

        setComponent(getComponent().toBuilder()
                .experience(Math.clamp(experience, 0D, getTotalExperienceBetweenLevels(level, level + 1)))
                .build());
    }

    public boolean addExperience(String ability, String experienceSource, double amount) {
        var event = NeoForge.EVENT_BUS.post(new RelicExperienceChangeEvent(this.getRelicData().getEntity(), this.getRelicData().getStack(), ability, experienceSource, amount));

        var delta = event.getDelta();

        if (event.isCanceled() || delta == 0)
            return false;

        if (!(event.getStack().getItem() instanceof IRelicItem relic))
            return false;

        var leveling = relic.getRelicData(event.getBearer(), event.getStack()).getLevelingData();

        if (!leveling.addExperience(delta))
            return false;

        leveling.addSourceExperience(ability, experienceSource, delta);

        return true;
    }

    public double getSourceExperience(String ability, String experienceSource) {
        return getComponent().getSourceExperience().getOrDefault(getSourceExperienceKey(ability, experienceSource), 0D);
    }

    public double getTotalSourceExperience() {
        return getComponent().getSourceExperience().values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public void addSourceExperience(String ability, String experienceSource, double amount) {
        if (amount <= 0D)
            return;

        var key = getSourceExperienceKey(ability, experienceSource);
        var sourceExperience = new HashMap<>(getComponent().getSourceExperience());

        sourceExperience.put(key, sourceExperience.getOrDefault(key, 0D) + amount);

        setComponent(getComponent().toBuilder()
                .sourceExperience(sourceExperience)
                .build());
    }

    private String getSourceExperienceKey(String ability, String experienceSource) {
        return ability + "." + experienceSource;
    }

    public boolean addExperience(double amount) {
        var xp = getExperience();
        var level = getLevel();
        var oldLevel = level;
        var maxLevel = this.getRelicData().calculateMaxLevel();

        while ((amount > 0 && level < maxLevel) || (amount < 0 && level > 0)) {
            if (amount > 0) {
                var requirement = getTotalExperienceBetweenLevels(level, level + 1) - xp;

                if (requirement <= 1.0E-7D) {
                    level++;

                    xp = 0;
                } else if (amount + 1.0E-7D >= requirement) {
                    amount -= requirement;

                    if (amount < 0D)
                        amount = 0D;

                    level++;

                    xp = 0;
                } else {
                    xp += amount;

                    amount = 0;
                }
            } else {
                if (xp + amount >= 0) {
                    xp += amount;

                    amount = 0;
                } else {
                    amount += xp;

                    level--;

                    xp = getTotalExperienceBetweenLevels(level, level + 1);
                }
            }
        }

        if (amount < 0)
            xp += amount;

        if (level != oldLevel && !addLevel(level - oldLevel))
            return false;

        setExperience(xp);

        return true;
    }

    public int getRank() {
        return getComponent().getRank();
    }

    public void setRank(int amount) {
        setComponent(getComponent().toBuilder()
                .rank(Math.max(0, amount))
                .build());
    }

    public void addRank(int amount) {
        setRank(getRank() + amount);
    }

    public double getExperienceLeftForLevelUp(int level) {
        return getTotalExperienceBetweenLevels(getLevel(), level) - getExperience();
    }

    public double getTotalExperienceBetweenLevels(int from, int to) {
        return getTotalExperienceForLevel(to) - getTotalExperienceForLevel(from);
    }

    public double getTotalExperienceForLevel(int level) {
        if (level <= 0)
            return 0;

        var template = getTemplate();
        var operation = template.getScalingModel();

        var total = 0D;

        for (var i = 0; i < level; i++)
            total += operation.evaluate(this.getRelicData().getEntity(), this.getRelicData().getStack(), template.getInitialCost(), template.getStep(), i);

        return Math.floor(total);
    }

    public int getLevelFromExperience(int experience) {
        int result = 0;
        var amount = 0D;

        do {
            ++result;

            amount = getTotalExperienceForLevel(result);
        } while (amount <= experience);

        return result - 1;
    }

    public int getRankupPlayerExperienceCost() {
        return (int) Math.round(this.getRelicData().getTemplate().getLeveling().getRankupCost() * (getRank() + 1));
    }

    public int getRankupRankPointCost() {
        return 1;
    }

    public long getDismantleRankPointReward() {
        return 1L + getRank();
    }

    public boolean mayRankup() {
        return !getRelicData().isMaxRank();
    }

    public boolean mayPlayerRankup() {
        if (!(this.getRelicData().getEntity() instanceof Player player))
            return false;

        return mayRankup() && EntityUtils.getPlayerTotalExperience(player) >= getRankupPlayerExperienceCost()
                && RankHandler.getRankPoints(player, getRelicData().getStack().getItem()) >= getRankupRankPointCost();
    }

    public boolean rankup() {
        if (!(this.getRelicData().getEntity() instanceof ServerPlayer player) || !mayPlayerRankup())
            return false;

        player.giveExperiencePoints(-getRankupPlayerExperienceCost());

        RankHandler.addRankPoints(player, getRelicData().getStack().getItem(), -getRankupRankPointCost());

        addRank(1);

        return true;
    }
}
