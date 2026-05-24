package it.hurts.sskirillss.relics.api.relics.data;

import it.hurts.sskirillss.relics.api.events.relic.abilities.ability.AbilityModeSwitchEvent;
import it.hurts.sskirillss.relics.api.relics.LockComponent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationContext;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationComponent;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationPredicateContext;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationPredicateType;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationStage;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationType;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

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
        return this.abilitiesData;
    }

    public String getId() {
        return this.ability;
    }

    public AbilityTemplate getTemplate() {
        return this.getAbilitiesData().getTemplate().getAbilities().get(this.getId());
    }

    public AbilityComponent getComponent() {
        var template = getTemplate();

        if (template == null)
            return null;

        var abilitiesComponent = this.getAbilitiesData().getComponent();
        var abilityComponent = abilitiesComponent.getAbilities().get(this.getId());

        if (abilityComponent != null)
            return abilityComponent;

        abilityComponent = AbilityComponent.EMPTY;

        if (template.getActivation().getType() == AbilityActivationType.TOGGLEABLE)
            abilityComponent = abilityComponent.toBuilder()
                    .activation(abilityComponent.getActivation().toBuilder()
                            .ticking(true)
                            .build())
                    .build();

        if (template.getRequiredLevel() <= 0 && template.getRequiredRank() <= 0)
            abilityComponent = abilityComponent.toBuilder()
                    .lock(LockComponent.builder()
                            .unlocks(new LockData(this).getMaxUnlocks())
                            .build())
                    .build();

        this.getAbilitiesData().setComponent(abilitiesComponent.toBuilder()
                .ability(this.getId(), abilityComponent)
                .build());

        return abilityComponent;
    }

    public void setComponent(AbilityComponent component) {
        this.getAbilitiesData().setComponent(this.getAbilitiesData().getComponent().toBuilder()
                .ability(this.getId(), component)
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

    public AbilityTargetingData getTargetingData() {
        return new AbilityTargetingData(this);
    }

    public LockData getLockData() {
        return new LockData(this);
    }

    public AbilityRankModifierData getRankModifierData(String rankModifier) {
        return new AbilityRankModifierData(this, rankModifier);
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

    public String getNextMode() {
        var template = getTemplate();

        if (template == null)
            return "";

        var modes = template.getModes();

        if (modes.isEmpty())
            return "";

        var mode = getMode();
        var index = modes.indexOf(mode);

        return modes.get(index < 0 || index >= modes.size() - 1 ? 0 : index + 1);
    }

    public boolean cycleMode(Player player) {
        var template = getTemplate();

        if (template == null || template.getModes().isEmpty())
            return false;

        var stack = this.getAbilitiesData().getRelicData().getStack();
        var event = new AbilityModeSwitchEvent(player, stack, this.getId(), getMode(), getNextMode());

        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled())
            return false;

        setMode(event.getToMode());

        return true;
    }

    public void randomizeStats() {
        var template = getTemplate();

        if (template == null)
            return;

        var stats = template.getStats();

        if (stats.isEmpty())
            return;

        var entity = this.getAbilitiesData().getRelicData().getEntity();
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
        var entity = this.getAbilitiesData().getRelicData().getEntity();
        var random = entity == null ? RandomSource.create() : entity.getRandom();

        getStatData(stat).setInitialQuality(random.nextInt(getStatData(stat).getMaxQuality() + 1));
    }

    public boolean isEnoughLevel() {
        var template = getTemplate();

        return template != null && this.getAbilitiesData().getRelicData().getLevelingData().getLevel() >= template.getRequiredLevel();
    }

    public boolean isEnoughRank() {
        var template = getTemplate();

        return template != null && this.getAbilitiesData().getRelicData().getLevelingData().getRank() >= template.getRequiredRank();
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

    public boolean isActive() {
        var template = getTemplate();

        return template != null && template.getActivation().isActive();
    }

    public AbilityActivationType getActivationType() {
        var template = getTemplate();

        return template == null ? AbilityActivationType.NONE : template.getActivation().getType();
    }

    public AbilityActivationComponent getActivationComponent() {
        var component = getComponent();

        return component == null ? AbilityActivationComponent.EMPTY : component.getActivation();
    }

    public void setActivationComponent(AbilityActivationComponent activation) {
        setComponent(getComponent().toBuilder()
                .activation(activation)
                .build());
    }

    public int getActivationCooldown() {
        return getActivationComponent().getCooldown();
    }

    public void setActivationCooldown(int cooldown) {
        var value = Math.max(0, cooldown);

        setActivationComponent(getActivationComponent().toBuilder()
                .cooldown(value)
                .cooldownCap(Math.max(getActivationComponent().getCooldownCap(), value))
                .build());
    }

    public void addActivationCooldown(int cooldown) {
        setActivationCooldown(getActivationCooldown() + cooldown);
    }

    public int getActivationCooldownCap() {
        return getActivationComponent().getCooldownCap();
    }

    public boolean isActivationOnCooldown() {
        return getActivationCooldown() > 0;
    }

    public boolean isActivationTicking() {
        return isUnlocked() && getActivationComponent().isTicking();
    }

    public void setActivationTicking(boolean ticking) {
        setActivationComponent(getActivationComponent().toBuilder()
                .ticking(ticking)
                .build());
    }

    public int getActivationCharge() {
        return getActivationComponent().getCharge();
    }

    public void setActivationCharge(int charge) {
        setActivationComponent(getActivationComponent().toBuilder()
                .charge(Math.max(0, charge))
                .build());
    }

    public boolean testActivationPredicate(Player player, String predicate) {
        var template = getTemplate();

        if (template == null)
            return false;

        var predicateTemplate = template.getActivation().getPredicates().get(predicate);

        return predicateTemplate != null && predicateTemplate.test(new AbilityActivationPredicateContext(player, this.getAbilitiesData().getRelicData().getStack(), this));
    }

    public boolean testActivationPredicates(Player player, AbilityActivationPredicateType type) {
        var template = getTemplate();

        if (template == null)
            return false;

        var context = new AbilityActivationPredicateContext(player, this.getAbilitiesData().getRelicData().getStack(), this);

        for (var predicate : template.getActivation().getPredicates(type).values())
            if (!predicate.test(context))
                return false;

        return true;
    }

    public boolean canPlayerActivate(Player player) {
        return isActive()
                && canPlayerUse(player)
                && !isActivationOnCooldown()
                && testActivationPredicates(player, AbilityActivationPredicateType.CAST);
    }

    public boolean activate(Player player, AbilityActivationStage stage) {
        var template = getTemplate();

        if (template == null || !template.getActivation().isActive())
            return false;

        var type = template.getActivation().getType();
        var stack = this.getAbilitiesData().getRelicData().getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return false;

        if (!canPlayerActivate(player)) {
            if (isActivationTicking()) {
                setActivationTicking(false);

                relic.activateAbility(new AbilityActivationContext(player, stack, this, type, AbilityActivationStage.END));
            }

            return false;
        }

        switch (type) {
            case CYCLICAL, TOGGLEABLE -> {
                switch (stage) {
                    case START -> setActivationTicking(true);
                    case END -> setActivationTicking(false);
                }
            }
        }

        if (stage == AbilityActivationStage.START && type == AbilityActivationType.CYCLE_MODE)
            cycleMode(player);

        relic.activateAbility(new AbilityActivationContext(player, stack, this, type, stage));

        return true;
    }

    public boolean activate(Player player) {
        return activate(player, AbilityActivationStage.START);
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
                && this.getAbilitiesData().getRelicData().getLevelingData().getPoints() >= template.getRequiredPoints()
                && isUnlocked();
    }

    public boolean mayPlayerUpgrade() {
        var entity = this.getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player))
            return false;

        return mayUpgrade() && EntityUtils.getPlayerTotalExperience(player) >= getUpgradePlayerExperienceCost();
    }

    public boolean upgrade() {
        var entity = this.getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player) || !this.mayPlayerUpgrade())
            return false;

        player.giveExperiencePoints(-this.getUpgradePlayerExperienceCost());

        this.setLevel(getLevel() + 1);

        this.getAbilitiesData().getRelicData().getLevelingData().addPoints(-getTemplate().getRequiredPoints());

        return true;
    }

    public int getRerollPlayerExperienceCost() {
        return 150;
    }

    public boolean mayReroll() {
        var template = getTemplate();

        return template != null && !template.getStats().isEmpty() && isUnlocked();
    }

    public boolean mayPlayerReroll() {
        var entity = this.getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player))
            return false;

        return this.mayReroll() && EntityUtils.getPlayerTotalExperience(player) >= this.getRerollPlayerExperienceCost();
    }

    public boolean reroll() {
        var entity = this.getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player) || !this.mayPlayerReroll())
            return false;

        player.giveExperiencePoints(-this.getRerollPlayerExperienceCost());

        this.randomizeStats();

        return true;
    }

    public int getResetPlayerExperienceCost() {
        return this.getLevel() * 250;
    }

    public boolean mayReset() {
        return this.getLevel() > 0 && this.isUnlocked();
    }

    public boolean mayPlayerReset() {
        var entity = this.getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player))
            return false;

        var template = this.getTemplate();

        return template != null && !template.getStats().isEmpty()
                && this.mayReset()
                && EntityUtils.getPlayerTotalExperience(player) >= this.getResetPlayerExperienceCost();
    }

    public boolean reset() {
        var entity = this.getAbilitiesData().getRelicData().getEntity();

        if (!(entity instanceof Player player) || !this.mayPlayerReset())
            return false;

        player.giveExperiencePoints(-getResetPlayerExperienceCost());

        this.getAbilitiesData().getRelicData().getLevelingData().addPoints(this.getLevel() * this.getTemplate().getRequiredPoints());

        this.setLevel(0);

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
