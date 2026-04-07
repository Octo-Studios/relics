package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.events.relic.leveling.RelicExperienceChangeEvent;
import it.hurts.sskirillss.relics.api.events.relic.leveling.RelicLevelChangeEvent;
import it.hurts.sskirillss.relics.api.events.relic.leveling.RelicLevelingPointsChangeEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.LevelingComponent;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import net.neoforged.neoforge.common.NeoForge;

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

        var delta = event.getDelta();
        var newLevel = currentLevel + delta;

        setLevel(newLevel);

        addPoints(delta);

        return true;
    }

    public double getExperience() {
        return getComponent().getExperience();
    }

    public void setExperience(double experience) {
        setComponent(getComponent().toBuilder()
                .experience(Math.clamp(experience, 0D, getTotalExperienceForLevel(getLevel() + 1)))
                .build());
    }

    public boolean addExperience(String ability, String experienceSource, double amount) {
        var event = NeoForge.EVENT_BUS.post(new RelicExperienceChangeEvent(this.getRelicData().getEntity(), this.getRelicData().getStack(), ability, experienceSource, amount));

        var delta = event.getDelta();

        if (event.isCanceled() || delta == 0)
            return false;

        if (!(event.getStack().getItem() instanceof IRelicItem relic))
            return false;

        relic.getRelicData(event.getBearer(), event.getStack()).getLevelingData().addExperience(delta);

        return true;
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
            xp = 0;

        setExperience(xp);

        if (level != oldLevel)
            addLevel(level - oldLevel);

        return true;
    }

    public int getPoints() {
        return getComponent().getPoints();
    }

    public void setPoints(int amount) {
        setComponent(getComponent().toBuilder()
                .points(Math.max(0, amount))
                .build());
    }

    public boolean addPoints(int amount) {
        var event = new RelicLevelingPointsChangeEvent(this.getRelicData().getEntity(), this.getRelicData().getStack(), amount);

        if (NeoForge.EVENT_BUS.post(event).isCanceled())
            return false;

        var delta = event.getDelta();
        var currentPoints = getPoints();
        var maxLevel = this.getRelicData().calculateMaxLevel();
        var newPoints = currentPoints + delta;

        if (newPoints < 0) {
            var deficit = -newPoints;

            var abilitiesData = this.getRelicData().getAbilitiesData();

            var abilities = abilitiesData.getAbilityIDs().stream()
                    .filter(ability -> abilitiesData.getAbilityData(ability).getLevel() > 0)
                    .toList();

            var comparator = java.util.Comparator.comparingInt((String ability) -> abilitiesData.getAbilityData(ability).getTemplate().getRequiredPoints());

            var sortedAbilities = deficit == 1
                    ? abilities.stream().sorted(comparator).toList()
                    : abilities.stream().sorted(comparator.reversed()).toList();

            for (var ability : sortedAbilities) {
                var abilityData = abilitiesData.getAbilityData(ability);

                while (deficit > 0 && abilityData.getLevel() > 0) {
                    var cost = abilityData.getTemplate().getRequiredPoints();

                    abilityData.addLevel(-1);

                    deficit -= cost;
                }

                if (deficit <= 0)
                    break;
            }

            newPoints = deficit < 0 ? -deficit : 0;
        }

        setPoints(Math.clamp(newPoints, 0, maxLevel));

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

        var template = this.getRelicData().getTemplate().getLeveling();
        var operation = template.getScalingModel();

        var total = 0D;

        for (int i = 0; i < level; i++)
            total += operation.evaluate(this.getRelicData().getEntity(), this.getRelicData().getStack(), template.getInitialCost(), template.getStep(), i);

        return (int) Math.floor(total);
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

    public boolean isPointsMismatch() {
        int current = getPoints();

        var abilitiesData = this.getRelicData().getAbilitiesData();

        for (var data : this.getRelicData().getTemplate().getAbilities().getAbilities().values())
            current += abilitiesData.getAbilityData(data.getId()).getComponent().getPoints() * data.getRequiredPoints();

        return current != getLevel();
    }

    public boolean mayPlayerRankup() {
        var levelingTemplate = this.getRelicData().getTemplate().getLeveling();

        return getLevel() == this.getRelicData().calculateMaxLevel() && getRank() < levelingTemplate.getMaxRank();
    }

    public boolean rankup() {
        if (!mayPlayerRankup())
            return false;

        addRank(1);
        setLevel(0);
        setPoints(0);

        for (var ability : this.getRelicData().getTemplate().getAbilities().getAbilities().values())
            this.getRelicData().getAbilitiesData().getAbilityData(ability.getId()).setLevel(0);

        return true;
    }
}
