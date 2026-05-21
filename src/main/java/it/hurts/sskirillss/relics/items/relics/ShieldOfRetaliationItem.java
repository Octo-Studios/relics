package it.hurts.sskirillss.relics.items.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.dev.shake.Shake;
import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.item.shield_of_retaliation.C2SShieldOfRetaliationRelease;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShieldOfRetaliationItem extends RelicItem {
    private static final String REFLECTED_PROJECTILE_DAMAGE_TAG = "relics:shield_of_retaliation_projectile_damage";

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("retaliation")
                                .rankModifier(1, "projectile")
                                .rankModifier(3, "stun")
                                .rankModifier(5, "guard")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("window")
                                        .initialValue(0.5D, 0.8D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("miss_damage")
                                        .initialValue(0.25D, 0.15D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.05D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("cooldown")
                                        .initialValue(8D, 6D)
                                        .thresholdValue(1D, Double.MAX_VALUE)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 4D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("projectile_damage")
                                        .initialValue(0.25D, 0.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100D, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("extension")
                                        .initialValue(0.2D, 0.3D)
                                        .thresholdValue(0.05D, Double.MAX_VALUE)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun_radius")
                                        .initialValue(2D, 3D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 8D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(AbilityStatTemplate.builder("stun")
                                        .initialValue(1D, 1.5D)
                                        .targetValue(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("block")
                                        .source(ExperienceSourceTemplate.builder("stun")
                                                .rankModifierVisibilityState("stun", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("blocks")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_blocked")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("projectiles_reflected")
                                                .formatValue(value -> String.valueOf(value.intValue()))
                                                .rankModifierVisibilityState("projectile", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 12, 4).star(1, 6, 9).star(2, 18, 9).star(3, 12, 15).star(4, 5, 22).star(5, 19, 22).star(6, 12, 28)
                                        .link(0, 1).link(0, 2).link(1, 3).link(2, 3).link(3, 4).link(3, 5).link(4, 6).link(5, 6)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.NETHER_LIKE, LootEntries.THE_NETHER)
                        .build())
                .build();
    }

    private int getParryTicks(Player player, ItemStack stack) {
        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");

        if (!ability.canPlayerUse(player))
            return 0;

        return Math.max(1, (int) Math.round(ability.getStatData("window").getValue() * 20D));
    }

    private int getCooldownTicks(Player player, ItemStack stack) {
        var ability = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");

        if (!ability.canPlayerUse(player))
            return 0;

        return Math.max(1, (int) Math.round(ability.getStatData("cooldown").getValue()));
    }

    private static int getActiveTicks(ItemStack stack) {
        return Math.max(0, stack.getOrDefault(RelicsDataComponents.SHIELD_OF_RETALIATION_PARRY_TICKS, 0));
    }

    private static void setActiveTicks(ItemStack stack, int ticks) {
        stack.set(RelicsDataComponents.SHIELD_OF_RETALIATION_PARRY_TICKS, Math.max(0, ticks));
    }

    private static boolean hasSucceeded(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.SHIELD_OF_RETALIATION_PARRY_SUCCEEDED, false);
    }

    private static void setSucceeded(ItemStack stack, boolean state) {
        stack.set(RelicsDataComponents.SHIELD_OF_RETALIATION_PARRY_SUCCEEDED, state);
    }

    private static boolean hasMissed(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.SHIELD_OF_RETALIATION_MISSED, false);
    }

    private static void setMissed(ItemStack stack, boolean state) {
        stack.set(RelicsDataComponents.SHIELD_OF_RETALIATION_MISSED, state);
    }

    public static void setReleaseLocked(ItemStack stack, boolean state) {
        stack.set(RelicsDataComponents.SHIELD_OF_RETALIATION_RELEASE_LOCKED, state);
    }

    private static boolean isReleaseLocked(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.SHIELD_OF_RETALIATION_RELEASE_LOCKED, false);
    }

    private static List<CapturedProjectileData> getCapturedProjectiles(ItemStack stack) {
        return new ArrayList<>(stack.getOrDefault(RelicsDataComponents.SHIELD_OF_RETALIATION_CAPTURED_PROJECTILES, List.of()));
    }

    private static void setCapturedProjectiles(ItemStack stack, List<CapturedProjectileData> projectiles) {
        stack.set(RelicsDataComponents.SHIELD_OF_RETALIATION_CAPTURED_PROJECTILES, List.copyOf(projectiles));
    }

    private static boolean isParryStack(Player player, ItemStack stack) {
        return player.isUsingItem() && player.getUseItem() == stack;
    }

    private static void finishParry(Player player, ItemStack stack, boolean evaluateMiss) {
        var wasActive = getActiveTicks(stack) > 0 || !getCapturedProjectiles(stack).isEmpty();

        releaseCapturedProjectiles(player, stack);

        if (evaluateMiss && !hasSucceeded(stack) && stack.getItem() instanceof ShieldOfRetaliationItem relic
                && relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation").canPlayerUse(player))
            setMissed(stack, true);

        if (wasActive && stack.getItem() instanceof ShieldOfRetaliationItem relic) {
            var cooldown = relic.getCooldownTicks(player, stack);

            if (cooldown > 0)
                player.getCooldowns().addCooldown(relic, cooldown);
        }

        setActiveTicks(stack, 0);
        setSucceeded(stack, false);
        setReleaseLocked(stack, true);
    }

    private static boolean canBlockDamageFrom(Player player, LivingIncomingDamageEvent event) {
        Vec3 sourcePosition = event.getSource().getSourcePosition();

        if (sourcePosition == null && event.getSource().getDirectEntity() != null)
            sourcePosition = event.getSource().getDirectEntity().position();

        if (sourcePosition == null && event.getSource().getEntity() != null)
            sourcePosition = event.getSource().getEntity().position();

        if (sourcePosition == null)
            return false;

        var view = player.getViewVector(1F);
        var direction = sourcePosition.vectorTo(player.position()).normalize();
        direction = new Vec3(direction.x, 0D, direction.z);

        return direction.dot(view) < 0D;
    }

    private static boolean isProjectileInFront(Player player, Projectile projectile) {
        var view = player.getViewVector(1F);
        var direction = projectile.position().vectorTo(player.position()).normalize();
        direction = new Vec3(direction.x, 0D, direction.z);

        return direction.dot(view) < 0D;
    }

    private static boolean isProjectileFlyingToward(Player player, Projectile projectile) {
        var motion = projectile.getDeltaMovement();

        if (motion.lengthSqr() < 0.0001D)
            return true;

        return motion.normalize().dot(player.position().subtract(projectile.position()).normalize()) > 0D;
    }

    private static boolean captureProjectile(Player player, ItemStack stack, Projectile projectile) {
        if (projectile.isRemoved() || projectile.getOwner() == player)
            return false;

        var captured = getCapturedProjectiles(stack);
        var uuid = projectile.getStringUUID();

        if (captured.stream().anyMatch(data -> data.uuid().equals(uuid)))
            return false;

        var look = player.getLookAngle().normalize();
        var right = look.cross(new Vec3(0D, 1D, 0D));

        if (right.lengthSqr() < 0.0001D)
            right = new Vec3(1D, 0D, 0D);
        else
            right = right.normalize();

        var up = right.cross(look).normalize();
        var delta = projectile.position().subtract(player.getEyePosition());
        var forwardOffset = Mth.clamp(delta.dot(look), 0.75D, 2.25D);
        var rightOffset = Mth.clamp(delta.dot(right), -1.35D, 1.35D);
        var upOffset = Mth.clamp(delta.dot(up), -0.9D, 0.9D);

        captured.add(new CapturedProjectileData(uuid, forwardOffset, rightOffset, upOffset));
        setCapturedProjectiles(stack, captured);

        projectile.setNoGravity(true);
        projectile.setDeltaMovement(Vec3.ZERO);

        return true;
    }

    private static Projectile findCapturedProjectile(Player player, String uuid) {
        if (player.level() instanceof ServerLevel serverLevel) {
            var entity = serverLevel.getEntity(UUID.fromString(uuid));

            return entity instanceof Projectile projectile ? projectile : null;
        }

        return player.level().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(8D),
                        projectile -> projectile.getStringUUID().equals(uuid))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static boolean positionCapturedProjectiles(Player player, ItemStack stack, boolean cleanupMissing, float partialTick) {
        var captured = getCapturedProjectiles(stack);
        var changed = false;
        var look = player.getLookAngle().normalize();
        var horizontal = Math.sqrt(look.x * look.x + look.z * look.z);
        var yRot = (float) Math.toDegrees(Math.atan2(look.x, look.z));
        var xRot = (float) Math.toDegrees(Math.atan2(look.y, horizontal));
        var right = look.cross(new Vec3(0D, 1D, 0D));

        if (right.lengthSqr() < 0.0001D)
            right = new Vec3(1D, 0D, 0D);
        else
            right = right.normalize();

        var up = right.cross(look).normalize();

        for (int i = captured.size() - 1; i >= 0; i--) {
            var data = captured.get(i);
            var projectile = findCapturedProjectile(player, data.uuid());

            if (projectile == null || projectile.isRemoved()) {
                if (cleanupMissing) {
                    captured.remove(i);
                    changed = true;
                }

                continue;
            }

            var position = player.getEyePosition(partialTick)
                    .add(look.scale(data.forwardOffset()))
                    .add(right.scale(data.rightOffset()))
                    .add(up.scale(data.upOffset()));

            projectile.setNoGravity(true);
            projectile.setDeltaMovement(look.scale(0.05D));
            projectile.setPos(position.x, position.y, position.z);
            projectile.xo = position.x;
            projectile.yo = position.y;
            projectile.zo = position.z;
            projectile.xOld = position.x;
            projectile.yOld = position.y;
            projectile.zOld = position.z;
            projectile.setYRot(yRot);
            projectile.setXRot(xRot);
            projectile.yRotO = yRot;
            projectile.xRotO = xRot;
        }

        if (changed)
            setCapturedProjectiles(stack, captured);

        return changed;
    }

    private static void holdCapturedProjectiles(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof ShieldOfRetaliationItem relic))
            return;

        var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");

        if (!ability.canPlayerUse(player) || !ability.getRankModifierData("projectile").isEnabled())
            return;

        var captured = getCapturedProjectiles(stack);
        var changed = positionCapturedProjectiles(player, stack, true, 1F);

        var shieldCenter = player.getEyePosition().add(player.getLookAngle().normalize().scale(1.4D));
        var shieldBox = new AABB(shieldCenter, shieldCenter).inflate(1.25D, 1.0D, 1.25D);

        for (var projectile : player.level().getEntitiesOfClass(Projectile.class, shieldBox, projectile -> projectile.isAlive() && projectile.getOwner() != player)) {
            if (!isProjectileInFront(player, projectile) || !isProjectileFlyingToward(player, projectile))
                continue;

            var uuid = projectile.getStringUUID();

            if (captured.stream().anyMatch(data -> data.uuid().equals(uuid)))
                continue;

            var look = player.getLookAngle().normalize();
            var right = look.cross(new Vec3(0D, 1D, 0D));

            if (right.lengthSqr() < 0.0001D)
                right = new Vec3(1D, 0D, 0D);
            else
                right = right.normalize();

            var up = right.cross(look).normalize();
            var delta = projectile.position().subtract(player.getEyePosition());
            var forwardOffset = Mth.clamp(delta.dot(look), 0.75D, 2.25D);
            var rightOffset = Mth.clamp(delta.dot(right), -1.35D, 1.35D);
            var upOffset = Mth.clamp(delta.dot(up), -0.9D, 0.9D);

            captured.add(new CapturedProjectileData(uuid, forwardOffset, rightOffset, upOffset));
            projectile.setNoGravity(true);
            projectile.setDeltaMovement(Vec3.ZERO);

            setSucceeded(stack, true);
            addBlockRewards(player, stack, 0D, 0);
            extendShieldWindow(stack, relic, player);

            ShakeManager.addForPlayer(player, (projectile.getOwner() != null ? Shake.builder(projectile.getOwner().position()) : Shake.builder(player))
                    .amplitude(0.15F)
                    .radius(Integer.MAX_VALUE)
                    .duration(5)
                    .speed(2.5F)
                    .build());

            changed = true;
        }

        if (changed)
            setCapturedProjectiles(stack, captured);
    }

    private static int stunNearbyTargets(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof ShieldOfRetaliationItem relic))
            return 0;

        var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");

        if (!ability.getRankModifierData("stun").isEnabled())
            return 0;

        var stunned = 0;
        var radius = ability.getStatData("stun_radius").getValue();
        var stunTicks = (int) Math.round(ability.getStatData("stun").getValue() * 20D);
        var view = player.getViewVector(1F);

        for (var target : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), target -> !EntityUtils.isAlliedTo(player, target))) {
            var direction = target.position().subtract(player.position());
            direction = new Vec3(direction.x, 0D, direction.z);

            if (direction.lengthSqr() > 0.0001D && direction.normalize().dot(view) <= 0D)
                continue;

            target.addEffect(new MobEffectInstance(RelicsMobEffects.STUN, stunTicks, 0, false, false));
            stunned++;
        }

        return stunned;
    }

    private static void addBlockRewards(Player player, ItemStack stack, double blockedDamage, int stunned) {
        if (!(stack.getItem() instanceof ShieldOfRetaliationItem relic))
            return;

        var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");

        if (blockedDamage > 0D)
            relic.getRelicData(player, stack).getLevelingData().addExperience("retaliation", "block", player.getRandom().nextDouble() * blockedDamage);

        if (stunned > 0)
            relic.getRelicData(player, stack).getLevelingData().addExperience("retaliation", "stun", stunned);

        ability.getStatisticData().getMetricData("blocks").addValue(1D);
        ability.getStatisticData().getMetricData("damage_blocked").addValue(blockedDamage);
    }

    private static void extendShieldWindow(ItemStack stack, ShieldOfRetaliationItem relic, Player player) {
        var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");

        if (!ability.getRankModifierData("projectile").isEnabled())
            return;

        setActiveTicks(stack, getActiveTicks(stack) + Math.max(1, (int) Math.round(ability.getStatData("extension").getValue() * 20D)));
    }

    private static void addShake(Player player, float amplitude, int duration) {
        ShakeManager.addForPlayer(player, Shake.builder(player)
                .radius(Integer.MAX_VALUE)
                .amplitude(amplitude)
                .duration(duration)
                .build());
    }

    private static void releaseCapturedProjectiles(Player player, ItemStack stack) {
        if (!(player.level() instanceof ServerLevel serverLevel) || !(stack.getItem() instanceof ShieldOfRetaliationItem relic))
            return;

        var captured = getCapturedProjectiles(stack);

        if (captured.isEmpty())
            return;

        var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");
        var look = player.getLookAngle().normalize();
        var bonus = ability.getStatData("projectile_damage").getValue();
        var released = 0;

        for (var data : captured) {
            var entity = serverLevel.getEntity(UUID.fromString(data.uuid()));

            if (!(entity instanceof Projectile projectile) || projectile.isRemoved())
                continue;

            projectile.setOwner(player);
            projectile.setNoGravity(false);
            var position = player.getEyePosition().add(look.scale(1.4D));

            projectile.setPos(position.x, position.y, position.z);
            projectile.setDeltaMovement(look.scale(3.5F));
            projectile.getPersistentData().putDouble(REFLECTED_PROJECTILE_DAMAGE_TAG, bonus);

            released++;
        }

        if (released > 0)
            ability.getStatisticData().getMetricData("projectiles_reflected").addValue(released);

        if (released > 0)
            ShakeManager.addForPlayer(player, Shake.builder(player)
                    .amplitude(0.15F + Math.min(released * 0.05F, 0.25F))
                    .radius(Integer.MAX_VALUE)
                    .duration(10)
                    .speed(2.5F)
                    .build());

        setCapturedProjectiles(stack, List.of());
    }

    public record CapturedProjectileData(String uuid, double forwardOffset, double rightOffset, double upOffset) {
        public static final Codec<CapturedProjectileData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("uuid").forGetter(CapturedProjectileData::uuid),
                Codec.DOUBLE.fieldOf("forward_offset").forGetter(CapturedProjectileData::forwardOffset),
                Codec.DOUBLE.fieldOf("right_offset").forGetter(CapturedProjectileData::rightOffset),
                Codec.DOUBLE.fieldOf("up_offset").forGetter(CapturedProjectileData::upOffset)
        ).apply(instance, CapturedProjectileData::new));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var ticks = this.getParryTicks(player, stack);

        if (ticks <= 0 || isReleaseLocked(stack) || player.getCooldowns().isOnCooldown(this))
            return InteractionResultHolder.pass(stack);

        if (!level.isClientSide()) {
            setActiveTicks(stack, ticks);
            setSucceeded(stack, false);
            setMissed(stack, false);
            setReleaseLocked(stack, false);
            setCapturedProjectiles(stack, List.of());
        }

        player.startUsingItem(hand);

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide() || !(entity instanceof Player player))
            return;

        var ticks = getActiveTicks(stack);

        if (ticks <= 0)
            return;

        if (!isParryStack(player, stack)) {
            finishParry(player, stack, true);

            return;
        }

        holdCapturedProjectiles(player, stack);

        if (ticks == 1) {
            finishParry(player, stack, true);
            player.stopUsingItem();

            return;
        }

        setActiveTicks(stack, ticks - 1);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        super.releaseUsing(stack, level, livingEntity, timeCharged);

        if (!level.isClientSide() && livingEntity instanceof Player player)
            finishParry(player, stack, getActiveTicks(stack) > 0);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        var max = Math.max(1, (int) Math.round(this.getRelicData(null, stack).getAbilitiesData().getAbilityData("retaliation").getStatData("window").getValue() * 20D));

        return Mth.hsvToRgb(Math.max(0F, (float) getActiveTicks(stack) / max) / 3F, 1F, 1F);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var max = Math.max(1, (int) Math.round(this.getRelicData(null, stack).getAbilitiesData().getAbilityData("retaliation").getStatData("window").getValue() * 20D));

        return (int) Math.ceil((13F * getActiveTicks(stack)) / max);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getActiveTicks(stack) > 0;
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
            if (event.getEntity().level().isClientSide())
                return;

            if (event.getSource().getDirectEntity() instanceof Projectile projectile) {
                var bonus = projectile.getPersistentData().getDouble(REFLECTED_PROJECTILE_DAMAGE_TAG);

                if (bonus > 0D) {
                    event.setAmount((float) (event.getAmount() * (1D + bonus)));
                    projectile.getPersistentData().remove(REFLECTED_PROJECTILE_DAMAGE_TAG);
                }
            }

            if (!(event.getEntity() instanceof Player player))
                return;

            var inventory = player.getInventory();

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                var stack = inventory.getItem(i);

                if (!(stack.getItem() instanceof ShieldOfRetaliationItem relic))
                    continue;

                var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");

                if (!ability.canPlayerUse(player) || getActiveTicks(stack) <= 0)
                    continue;

                if (!isParryStack(player, stack) || !canBlockDamageFrom(player, event))
                    continue;

                var incomingProjectile = event.getSource().getDirectEntity() instanceof Projectile projectile ? projectile : null;

                var blockedDamage = event.getAmount();

                if (incomingProjectile != null) {
                    if (!ability.getRankModifierData("projectile").isEnabled())
                        continue;

                    event.setCanceled(true);
                    setSucceeded(stack, true);
                    captureProjectile(player, stack, incomingProjectile);
                    extendShieldWindow(stack, relic, player);

                    addBlockRewards(player, stack, blockedDamage, 0);

                    return;
                }

                event.setCanceled(true);

                setSucceeded(stack, true);

                ShakeManager.addForPlayer(player, Shake.builder(event.getSource().getSourcePosition())
                        .radius(Integer.MAX_VALUE)
                        .amplitude(0.15F)
                        .duration(10)
                        .speed(2.5F)
                        .build());

                if (!ability.getRankModifierData("guard").isEnabled()) {
                    var cooldown = relic.getCooldownTicks(player, stack);

                    if (cooldown > 0)
                        player.getCooldowns().addCooldown(relic, cooldown);

                    setActiveTicks(stack, 0);
                    setReleaseLocked(stack, true);
                    player.stopUsingItem();
                }

                addBlockRewards(player, stack, blockedDamage, stunNearbyTargets(player, stack));

                return;
            }

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                var stack = inventory.getItem(i);

                if (!(stack.getItem() instanceof ShieldOfRetaliationItem relic) || !hasMissed(stack))
                    continue;

                var ability = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("retaliation");

                if (!ability.canPlayerUse(player))
                    continue;

                setMissed(stack, false);
                event.setAmount((float) (event.getAmount() * (1D + ability.getStatData("miss_damage").getValue())));

                return;
            }
        }
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        private static boolean wasUseDown = false;
        private static int releaseSyncTicks = 0;

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            var minecraft = Minecraft.getInstance();

            if (minecraft.player == null)
                return;

            var useDown = minecraft.options.keyUse.isDown();

            if (wasUseDown && !useDown)
                releaseSyncTicks = 5;

            if (releaseSyncTicks > 0) {
                releaseSyncTicks--;

                NetworkHandler.sendToServer(new C2SShieldOfRetaliationRelease(true));
            }

            wasUseDown = useDown;
        }

        @SubscribeEvent
        public static void onRenderFrame(RenderFrameEvent.Pre event) {
            var minecraft = Minecraft.getInstance();
            var player = minecraft.player;

            if (player == null || minecraft.isPaused())
                return;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                var stack = player.getInventory().getItem(i);

                if (!(stack.getItem() instanceof ShieldOfRetaliationItem) || getCapturedProjectiles(stack).isEmpty())
                    continue;

                positionCapturedProjectiles(player, stack, false, Mth.clamp(RenderUtils.getPartialTick(false), 0F, 1F));
            }
        }
    }
}
