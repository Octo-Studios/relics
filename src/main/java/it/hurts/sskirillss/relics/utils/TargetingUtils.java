package it.hurts.sskirillss.relics.utils;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingApi;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingOption;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.SelectorType;
import it.hurts.sskirillss.relics.api.relics.data.AbilityData;
import it.hurts.sskirillss.relics.api.relics.data.SynergyData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class TargetingUtils {
    public static boolean canHarm(@Nullable Entity source, LivingEntity target, AbilityData abilityData) {
        return canAffect(source, target, abilityData, SelectorType.HARMFUL);
    }

    public static boolean canBenefit(@Nullable Entity source, LivingEntity target, AbilityData abilityData) {
        return canAffect(source, target, abilityData, SelectorType.BENEFICIAL);
    }

    public static boolean canHarm(@Nullable Entity source, LivingEntity target, SynergyData synergyData) {
        return canAffect(source, target, synergyData, SelectorType.HARMFUL);
    }

    public static boolean canBenefit(@Nullable Entity source, LivingEntity target, SynergyData synergyData) {
        return canAffect(source, target, synergyData, SelectorType.BENEFICIAL);
    }

    public static boolean canHarm(@Nullable Entity source, LivingEntity target, ItemStack stack, String ability) {
        return canAffect(source, target, stack, ability, SelectorType.HARMFUL);
    }

    public static boolean canBenefit(@Nullable Entity source, LivingEntity target, ItemStack stack, String ability) {
        return canAffect(source, target, stack, ability, SelectorType.BENEFICIAL);
    }

    public static boolean canHarmSynergy(@Nullable Entity source, LivingEntity target, ItemStack stack, String synergy) {
        return canAffectSynergy(source, target, stack, synergy, SelectorType.HARMFUL);
    }

    public static boolean canBenefitSynergy(@Nullable Entity source, LivingEntity target, ItemStack stack, String synergy) {
        return canAffectSynergy(source, target, stack, synergy, SelectorType.BENEFICIAL);
    }

    public static boolean canAffect(@Nullable Entity source, LivingEntity target, ItemStack stack, String ability, SelectorType selectorType) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return true;

        var abilityData = relic.getRelicData(source instanceof LivingEntity living ? living : target, stack).getAbilitiesData().getAbilityData(ability);

        return canAffect(source, target, abilityData, selectorType);
    }

    public static boolean canAffectSynergy(@Nullable Entity source, LivingEntity target, ItemStack stack, String synergy, SelectorType selectorType) {
        if (!(stack.getItem() instanceof IRelicItem relic))
            return true;

        var synergyData = relic.getRelicData(source instanceof LivingEntity living ? living : target, stack).getAbilitiesData().getSynergyData(synergy);

        return canAffect(source, target, synergyData, selectorType);
    }

    public static boolean canAffect(@Nullable Entity source, LivingEntity target, AbilityData abilityData, SelectorType selectorType) {
        var template = abilityData.getTemplate();
        var targeting = template == null ? null : template.getTargeting();

        if (targeting == null || !targeting.isActive() || !targeting.supportsSelector(selectorType))
            return true;

        if (selectorType == SelectorType.HARMFUL && isSelfTarget(source, target))
            return false;

        if (selectorType == SelectorType.HARMFUL && !canHarmVanillaPlayer(source, target))
            return false;

        return abilityData.getTargetingData().getOption(resolveOption(source, target, selectorType), selectorType);
    }

    public static boolean canAffect(@Nullable Entity source, LivingEntity target, SynergyData synergyData, SelectorType selectorType) {
        var template = synergyData.getTemplate();
        var targeting = template == null ? null : template.getTargeting();

        if (targeting == null || !targeting.isActive() || !targeting.supportsSelector(selectorType))
            return true;

        if (selectorType == SelectorType.HARMFUL && isSelfTarget(source, target))
            return false;

        if (selectorType == SelectorType.HARMFUL && !canHarmVanillaPlayer(source, target))
            return false;

        return synergyData.getTargetingData().getOption(resolveOption(source, target, selectorType), selectorType);
    }

    public static boolean hurtEnemy(LivingEntity target, DamageSource source, float amount, ItemStack stack, String ability) {
        if (!canHarm(source.getEntity(), target, stack, ability))
            return false;

        return target.hurt(source, amount);
    }

    public static boolean addHarmfulEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source, ItemStack stack, String ability) {
        if (!canHarm(source, target, stack, ability))
            return false;

        return target.addEffect(effect, source);
    }

    public static boolean addBeneficialEffect(LivingEntity target, MobEffectInstance effect, @Nullable Entity source, ItemStack stack, String ability) {
        if (!canBenefit(source, target, stack, ability))
            return false;

        return target.addEffect(effect, source);
    }

    public static boolean hurtEnemyBySynergy(LivingEntity target, DamageSource source, float amount, ItemStack stack, String synergy) {
        if (!canHarmSynergy(source.getEntity(), target, stack, synergy))
            return false;

        return target.hurt(source, amount);
    }

    public static boolean addHarmfulEffectBySynergy(LivingEntity target, MobEffectInstance effect, @Nullable Entity source, ItemStack stack, String synergy) {
        if (!canHarmSynergy(source, target, stack, synergy))
            return false;

        return target.addEffect(effect, source);
    }

    public static boolean addBeneficialEffectBySynergy(LivingEntity target, MobEffectInstance effect, @Nullable Entity source, ItemStack stack, String synergy) {
        if (!canBenefitSynergy(source, target, stack, synergy))
            return false;

        return target.addEffect(effect, source);
    }

    public static boolean areAllied(@Nullable Entity source, @Nullable Entity target) {
        if (source == null || target == null)
            return false;

        if (source.getUUID().equals(target.getUUID()))
            return true;

        var sourceOwner = getOwningPlayerId(source);
        var targetOwner = getOwningPlayerId(target);

        if (sourceOwner.isPresent() && target.getUUID().equals(sourceOwner.get()))
            return true;

        if (targetOwner.isPresent() && source.getUUID().equals(targetOwner.get()))
            return true;

        if (sourceOwner.isPresent() && targetOwner.isPresent() && sourceOwner.get().equals(targetOwner.get()))
            return true;

        return AbilityTargetingApi.areAllied(source, target);
    }

    private static AbilityTargetingOption resolveOption(@Nullable Entity source, LivingEntity target, SelectorType selectorType) {
        if (source != null && source.getUUID().equals(target.getUUID()))
            return AbilityTargetingOption.SELF;

        if (target instanceof Player)
            return resolvePlayerOption(source, target);

        if (isTamed(target))
            return resolveTamedOption(source, target, selectorType);

        if (target instanceof Monster)
            return AbilityTargetingOption.HOSTILE_MOBS;

        if (target instanceof NeutralMob)
            return AbilityTargetingOption.NEUTRAL_MOBS;

        var category = target.getType().getCategory();

        if (category == MobCategory.MONSTER)
            return AbilityTargetingOption.HOSTILE_MOBS;

        if (category == MobCategory.CREATURE || category == MobCategory.AMBIENT || category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT || category == MobCategory.AXOLOTLS)
            return AbilityTargetingOption.PEACEFUL_MOBS;

        return selectorType == SelectorType.BENEFICIAL ? AbilityTargetingOption.PEACEFUL_MOBS : AbilityTargetingOption.HOSTILE_MOBS;
    }

    private static boolean canHarmVanillaPlayer(@Nullable Entity source, LivingEntity target) {
        if (!(target instanceof Player targetPlayer))
            return true;

        var sourcePlayer = getPlayerOrOwner(source);

        return sourcePlayer.isEmpty() || sourcePlayer.get().canHarmPlayer(targetPlayer);
    }

    private static boolean isSelfTarget(@Nullable Entity source, LivingEntity target) {
        if (source == null)
            return false;

        if (source.getUUID().equals(target.getUUID()))
            return true;

        var sourceOwner = getOwningPlayerId(source);

        return sourceOwner.isPresent() && sourceOwner.get().equals(target.getUUID());
    }

    private static AbilityTargetingOption resolvePlayerOption(@Nullable Entity source, LivingEntity target) {
        if (source == null)
            return AbilityTargetingOption.OTHER_TEAM_PLAYERS;

        if (areAllied(source, target))
            return AbilityTargetingOption.TEAM_PLAYERS;

        return AbilityTargetingOption.OTHER_TEAM_PLAYERS;
    }

    private static AbilityTargetingOption resolveTamedOption(@Nullable Entity source, LivingEntity target, SelectorType selectorType) {
        if (selectorType == SelectorType.HARMFUL)
            return AbilityTargetingOption.TAMED_CREATURES;

        var sourcePlayer = source == null ? Optional.<UUID>empty() : getPlayerOrOwnerId(source);
        var targetOwner = getOwningPlayerId(target);

        if (sourcePlayer.isPresent() && targetOwner.isPresent()) {
            if (sourcePlayer.get().equals(targetOwner.get()))
                return AbilityTargetingOption.OWN_TAMED_CREATURES;

            if (AbilityTargetingApi.areAllied(source, target))
                return AbilityTargetingOption.ALLIED_TAMED_CREATURES;
        }

        return AbilityTargetingOption.PEACEFUL_MOBS;
    }

    private static boolean isTamed(Entity entity) {
        return entity instanceof TamableAnimal tamable && tamable.isTame() || entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null;
    }

    private static Optional<UUID> getPlayerOrOwnerId(Entity entity) {
        var player = getPlayerId(entity);

        return player.isPresent() ? player : getOwningPlayerId(entity);
    }

    private static Optional<Player> getPlayerOrOwner(@Nullable Entity entity) {
        if (entity instanceof Player player)
            return Optional.of(player);

        if (entity instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player owner)
            return Optional.of(owner);

        return Optional.empty();
    }

    private static Optional<UUID> getPlayerId(Entity entity) {
        return entity instanceof Player ? Optional.of(entity.getUUID()) : Optional.empty();
    }

    private static Optional<UUID> getOwningPlayerId(Entity entity) {
        return entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null ? Optional.of(ownable.getOwnerUUID()) : Optional.empty();
    }
}
