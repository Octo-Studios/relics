package it.hurts.sskirillss.relics.items.relics;

import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberration;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberrationManager;
import it.hurts.sskirillss.relics.dev.shake.Shake;
import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import it.hurts.sskirillss.relics.init.RelicsCreativeTabs;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.misc.CreativeContentConstructor;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.S2CSetEntityMotion;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;

public class ChorusStaffItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("blink")
                                .rankModifier(1, "flicker")
                                .rankModifier(3, "safe_fall")
                                .rankModifier(5, "ascent")
                                .initialMaxLevel(10)
                                .stat(AbilityStatTemplate.builder("distance")
                                        .initialValue(10D, 15D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("max_charge")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(AbilityStatTemplate.builder("cooldown")
                                        .initialValue(30D, 15D)
                                        .thresholdValue(1D, Double.MAX_VALUE)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), -0.019D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source("blink")
                                        .source(ExperienceSourceTemplate.builder("flicker")
                                                .rankModifierVisibilityState("flicker", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("safe_fall")
                                                .rankModifierVisibilityState("safe_fall", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("blinks_amount")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("distance_traveled")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("targets")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .rankModifierVisibilityState("flicker", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("safe_falls")
                                                .formatValue((value) -> String.valueOf(value.intValue()))
                                                .rankModifierVisibilityState("safe_fall", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 14, 4).star(1, 8, 6).star(2, 19, 13).star(3, 12, 14).star(4, 3, 15).star(5, 7, 22).star(6, 14, 22).star(7, 7, 28)
                                        .link(7, 5).link(5, 4).link(5, 6).link(5, 3).link(3, 2).link(3, 1).link(3, 0)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.END_LIKE, LootEntries.THE_END)
                        .build())
                .build();
    }

    public int getMaxCharge(LivingEntity entity, ItemStack stack) {
        return (int) this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("blink").getStatData("max_charge").getValue();
    }

    public int getCharge(LivingEntity entity, ItemStack stack) {
        return Math.clamp(stack.getOrDefault(RelicsDataComponents.CHORUS_STAFF_CHARGE, 0), 0, this.getMaxCharge(entity, stack));
    }

    public void setCharge(LivingEntity entity, ItemStack stack, int charge) {
        stack.set(RelicsDataComponents.CHORUS_STAFF_CHARGE, Math.clamp(charge, 0, this.getMaxCharge(entity, stack)));
    }

    public void addCharge(LivingEntity entity, ItemStack stack, int charge) {
        this.setCharge(entity, stack, this.getCharge(entity, stack) + charge);
    }

    public void setSafeFall(ItemStack stack, boolean state) {
        stack.set(RelicsDataComponents.CHORUS_STAFF_SAFE_FALL, state);
    }

    public boolean shouldSafeFall(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.CHORUS_STAFF_SAFE_FALL, false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (!this.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").canPlayerUse(player))
            return InteractionResultHolder.pass(stack);

        var radius = this.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getStatData("distance").getValue();

        var eyePos = player.getEyePosition();
        var lookAngle = player.getLookAngle().normalize();

        var destination = eyePos.add(lookAngle.scale(radius));

        var hit = level.clip(new ClipContext(eyePos, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        var impulse = Vec3.ZERO;

        var charge = this.getCharge(player, stack);

        if (charge <= 0)
            return InteractionResultHolder.pass(stack);

        if (this.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getRankModifierData("ascent").isEnabled()
                && hit.getType() == HitResult.Type.BLOCK && hit.getLocation().distanceTo(eyePos) <= radius) {
            var current = hit.getBlockPos();

            var maxUp = (int) Math.floor(radius - eyePos.distanceTo(hit.getLocation()));

            BlockPos feetPos = null;

            for (int i = 1; i <= maxUp && current.getY() + i < level.getMaxBuildHeight(); i++) {
                var feet = current.above(i);
                var head = feet.above();

                if (level.isEmptyBlock(feet) && level.isEmptyBlock(head)) {
                    feetPos = feet;

                    break;
                }
            }

            if (feetPos != null)
                destination = feetPos.getBottomCenter();
            else
                destination = hit.getLocation().subtract(lookAngle.scale(0.5));
        } else {
            destination = hit.getLocation();

            if (!level.getBlockState(hit.getBlockPos().below()).blocksMotion())
                destination = destination.add(0, -1, 0);

            var extra = Math.min(player.getKnownMovement().length(), 5);

            impulse = lookAngle.scale(1.25 + extra);
        }

        var random = level.getRandom();

        var shift = player.getBbHeight() / 2F;
        var from = player.position().add(0F, shift, 0F);
        var to = destination.add(0F, shift, 0F);

        level.playSound(player, new BlockPos((int) from.x(), (int) from.y(), (int) from.z()), SoundEvents.ENDERMAN_TELEPORT, SoundSource.MASTER, 1F, 1F);
        level.playSound(player, new BlockPos((int) to.x(), (int) to.y(), (int) to.z()), SoundEvents.ENDERMAN_TELEPORT, SoundSource.MASTER, 1F, 1F);

        if (!level.isClientSide) {
            ServerScheduler.schedule(1, () -> {
                var direction = to.subtract(from);
                var dist = Math.max(0.001, direction.length());

                var forward = direction.scale(1.0 / dist);
                var up = Math.abs(forward.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
                var right = forward.cross(up).normalize();
                var binormal = forward.cross(right).normalize();

                var pointCount = (int) Math.round(dist);
                var points = new Vec3[pointCount];

                for (int i = 0; i < pointCount; i++) {
                    var t = i / (float) (pointCount - 1);

                    var base = new Vec3(Mth.lerp(t, from.x, to.x), Mth.lerp(t, from.y, to.y), Mth.lerp(t, from.z, to.z));
                    var wobble = 1F + 0.5F * (float) Math.sin(t * Math.PI);

                    var ox = (random.nextFloat() - 0.5F) * wobble;
                    var oy = (random.nextFloat() - 0.5F) * wobble;

                    points[i] = base.add(right.scale(ox)).add(binormal.scale(oy));
                }

                var segments = 12;

                for (int i = 0; i < pointCount - 1; i++) {
                    var p0 = points[i];
                    var p1 = points[i + 1];

                    for (int s = 0; s <= segments; s++) {
                        var t = s / (float) segments;

                        var px = Mth.lerp(t, p0.x, p1.x);
                        var py = Mth.lerp(t, p0.y, p1.y);
                        var pz = Mth.lerp(t, p0.z, p1.z);

                        if (level instanceof ServerLevel serverLevel)
                            serverLevel.sendParticles(ParticleUtils.constructSimpleSpark(FlawlessUtils.getColor(player, stack, new Color(155 + random.nextInt(100), 80, 255)), 1F, 3, 0.5F), px, py, pz, 1, 0, 0, 0, 0);
                    }
                }
            });

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.teleportTo((ServerLevel) level, destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());

                serverPlayer.fallDistance = 0;

                NetworkHandler.sendToClient(new S2CSetEntityMotion(player.getId(), impulse.toVector3f()), serverPlayer);

                var finalImpulse = impulse;

                ServerScheduler.schedule(1, () -> NetworkHandler.sendToClient(new S2CSetEntityMotion(player.getId(), finalImpulse.toVector3f()), serverPlayer));

                this.addCharge(player, stack, -1);

                this.getRelicData(player, stack).getLevelingData().addExperience("blink", "blink", 1);

                this.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getStatisticData().getMetricData("blinks_amount").addValue(1);
                this.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getStatisticData().getMetricData("distance_traveled").addValue(from.distanceTo(to));

                if (this.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getRankModifierData("safe_fall").isEnabled())
                    this.setSafeFall(stack, true);

                if (this.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getRankModifierData("flicker").isEnabled()) {
                    for (var mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(32))) {
                        if (mob.getTarget() == player) {
                            mob.setTarget(null);

                            this.getRelicData(player, stack).getLevelingData().addExperience("blink", "flicker", 1);

                            this.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getStatisticData().getMetricData("targets").addValue(1);
                        }
                    }
                }
            }
        } else {
            if (impulse == Vec3.ZERO)
                ShakeManager.add(level, Shake.builder(player)
                        .radius(Integer.MAX_VALUE)
                        .amplitude(0.2F)
                        .duration(5)
                        .build());
            else
                ShakeManager.add(level, Shake.builder(player)
                        .radius(Integer.MAX_VALUE)
                        .amplitude(0)
                        .fovAmplitude(0.35F)
                        .duration(10)
                        .speed(2)
                        .build());

            ChromaticAberrationManager.add(level, ChromaticAberration.builder(player)
                    .radius(Integer.MAX_VALUE)
                    .strength(0.075F)
                    .duration(10)
                    .build());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide() || !(entity instanceof LivingEntity livingEntity) || livingEntity.tickCount % ((int) this.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("blink").getStatData("cooldown").getValue() * 20) != 0
                || this.getCharge(livingEntity, stack) >= this.getMaxCharge(livingEntity, stack))
            return;

        this.addCharge(livingEntity, stack, 1);
    }

    @Override
    public void gatherCreativeTabContent(CreativeContentConstructor constructor) {
        var stack = this.getDefaultInstance();

        this.setCharge(null, stack, this.getMaxCharge(null, stack));

        constructor.entry(RelicsCreativeTabs.RELICS_TAB.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0F, (float) this.getCharge(null, stack) / this.getMaxCharge(null, stack)) / 3F, 1F, 1F);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.ceil((13F * this.getCharge(null, stack)) / this.getMaxCharge(null, stack));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return this.getCharge(null, stack) < this.getMaxCharge(null, stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return this.getMaxCharge(null, stack);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingFall(LivingFallEvent event) {
            if (!(event.getEntity() instanceof Player player))
                return;

            var inventory = player.getInventory();

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                var stack = inventory.getItem(i);

                if (stack.getItem() instanceof ChorusStaffItem relic) {
                    if (!relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getRankModifierData("safe_fall").isEnabled() || !relic.shouldSafeFall(stack))
                        continue;

                    relic.setSafeFall(stack, false);

                    relic.getRelicData(player, stack).getLevelingData().addExperience("blink", "blink", 1);

                    relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("blink").getStatisticData().getMetricData("safe_falls").addValue(1);

                    event.setDamageMultiplier(0F);
                }
            }
        }
    }
}