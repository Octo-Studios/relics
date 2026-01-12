package it.hurts.sskirillss.relics.items.relics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.SelfSacrificeProjectileEntity;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SphereOfSelfSacrifice extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("sacrifice")
                                .rankModifier(1, "resistance")
                                .rankModifier(3, "salvo")
                                .rankModifier(5, "salvation")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("stacks")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 2.2324D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .stat(StatTemplate.builder("resistance")
                                        .initialValue(0.01D, 0.025D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0286D)
                                        .formatValue(value -> MathUtils.round(value * 100, 1))
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("healing_done")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_resisted")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("resistance", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("projectiles_created")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("salvo", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("blood_projectile_damage")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("salvo", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("salvation_triggers")
                                                .formatValue(value -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .rankModifierVisibilityState("salvation", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("salvation_damage_blocked")
                                                .formatValue(value -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("salvation", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("self_inflicted")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("resisting_damage")
                                                .rankModifierVisibilityState("resistance", VisibilityState.OBFUSCATED)
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("salvation")
                                                .rankModifierVisibilityState("salvation", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 19, 9).star(1, 2, 9).star(2, 11, 27).star(3, 11, 19).star(4, 11, 12).star(5, 5, 16).star(6, 16, 16)
                                        .link(1, 5).link(5, 3).link(3, 6).link(6, 0).link(4, 3).link(3, 2)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_NETHER, LootEntries.NETHER_LIKE)
                        .build())
                .build();
    }

    public List<HealingStack> getHealingStacks(ItemStack stack) {
        return new ArrayList<>(stack.getOrDefault(RelicsDataComponents.SPHERE_OF_SELF_SACRIFICE_STACKS, List.of()));
    }

    public void setHealingStacks(ItemStack stack, List<HealingStack> stacks) {
        stack.set(RelicsDataComponents.SPHERE_OF_SELF_SACRIFICE_STACKS, new ArrayList<>(stacks));
    }

    public void addHealingStack(LivingEntity entity, ItemStack stack, HealingStack healingStack) {
        var stacks = this.getHealingStacks(stack);
        var maxStacks = Math.max(1, (int) Math.ceil(this.getRelicData(entity, stack).getAbilitiesData().getAbilityData("sacrifice").getStatData("stacks").getValue()));

        while (stacks.size() >= maxStacks)
            stacks.removeFirst();

        stacks.add(healingStack);

        stacks.replaceAll(entry -> entry.withTicks(entry.totalTicks()));

        this.setHealingStacks(stack, stacks);
    }

    private float getHealthCost(LivingEntity entity) {
        return entity.getMaxHealth() * 0.33F;
    }

    public HealingStack buildHealingStack(LivingEntity entity) {
        var ticks = 10 * 20;
        var totalHeal = this.getHealthCost(entity) * 2F;

        return new HealingStack(totalHeal, ticks, ticks);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (!this.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").canPlayerUse(player))
            return InteractionResultHolder.pass(stack);

        if (player.isShiftKeyDown() && this.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").isRankModifierUnlocked("salvo")) {
            if (!level.isClientSide()) {
                var hit = EntityUtils.rayTraceEntity(player, entity -> entity instanceof LivingEntity living && !living.isDeadOrDying() && !EntityUtils.isAlliedTo(player, living) && !living.is(player), 64D);

                if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof LivingEntity target))
                    return InteractionResultHolder.pass(stack);

                var stacks = this.getHealingStacks(stack);

                if (stacks.isEmpty())
                    return InteractionResultHolder.pass(stack);

                var look = player.getLookAngle();
                var eye = player.getEyePosition();
                var right = look.cross(new Vec3(0, 1, 0));

                if (right.lengthSqr() < 1e-4)
                    right = new Vec3(1, 0, 0);

                var up = look.cross(right).normalize();
                right = right.normalize();

                var count = stacks.size();
                var radius = 0.6D;
                var basePos = eye.subtract(look.scale(0.75D));

                for (int i = 0; i < count; i++) {
                    var healingStack = stacks.get(i);
                    var projectileDamage = Math.max(0F, healingStack.remainingHeal());

                    var angle = (Math.PI * 2D / count) * i + player.getRandom().nextDouble() * (Math.PI / count);
                    var offset = right.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
                    var spawnPos = basePos.add(offset);

                    ServerScheduler.schedule(i * 2, () -> {
                        var projectile = new SelfSacrificeProjectileEntity(RelicsEntities.SELF_SACRIFICE_PROJECTILE.get(), level);

                        projectile.setOwner(player);
                        projectile.setPos(spawnPos);
                        projectile.setStack(stack);
                        projectile.setDeltaMovement(look.scale(0.25F).add(offset.normalize().scale(0.25F)).add(0, 0.05F, 0));
                        projectile.setDamage(projectileDamage);
                        projectile.setTarget(target);
                        projectile.setFlawless(this.getRelicData(player, stack).isFlawless());

                        level.addFreshEntity(projectile);

                        this.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").getStatisticData().getMetricData("projectiles_created").addValue(1);
                    });
                }

                this.setHealingStacks(stack, List.of());
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!level.isClientSide()) {
            if (player.getHealth() / player.getMaxHealth() < 0.33F || this.getHealingStacks(stack).size() >= this.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").getStatData("stacks").getValue())
                return InteractionResultHolder.pass(stack);

            var damage = this.getHealthCost(player);

            var remaining = Math.max(0F, player.getHealth() - 1F);

            damage = Math.min(damage, remaining);

            player.invulnerableTime = 0;
            player.hurt(level.damageSources().magic(), damage);

            this.getRelicData(player, stack).getLevelingData().addExperience("sacrifice", "self_inflicted", 1);

            this.addHealingStack(player, stack, this.buildHealingStack(player));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide() || !(entity instanceof LivingEntity livingEntity))
            return;

        var stacks = this.getHealingStacks(stack);

        if (stacks.isEmpty())
            return;

        var updated = new ArrayList<HealingStack>();

        for (var healingStack : stacks) {
            var effectiveHeal = Math.max(0, Math.min(healingStack.healForCurrentTick(), livingEntity.getMaxHealth() - livingEntity.getHealth()));

            if (effectiveHeal > 0) {
                livingEntity.heal(effectiveHeal);

                this.getRelicData(livingEntity, stack).getAbilitiesData().getAbilityData("sacrifice").getStatisticData().getMetricData("healing_done").addValue(effectiveHeal);
            }

            var remainingTicks = healingStack.remainingTicks() - 1;

            if (remainingTicks > 0)
                updated.add(healingStack.withTicks(remainingTicks));
        }

        this.setHealingStacks(stack, updated);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        var max = (float) this.getRelicData(null, stack).getAbilitiesData().getAbilityData("sacrifice").getStatData("stacks").getValue();
        var cur = (float) this.getHealingStacks(stack).size();

        var ratio = max <= 0F ? 0F : Mth.clamp(cur / max, 0F, 1F);

        return Mth.clamp(Mth.ceil(13F * ratio), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        var max = (float) this.getRelicData(null, stack).getAbilitiesData().getAbilityData("sacrifice").getStatData("stacks").getValue();
        var cur = (float) this.getHealingStacks(stack).size();

        var ratio = max <= 0F ? 0F : Mth.clamp(cur / max, 0F, 1F);

        return Mth.hsvToRgb(ratio / 3F, 1F, 1F);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !this.getHealingStacks(stack).isEmpty();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return (int) this.getRelicData(null, stack).getAbilitiesData().getAbilityData("sacrifice").getStatData("stacks").getValue();
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    public record HealingStack(float totalHeal, int remainingTicks, int totalTicks) {
        public static final Codec<HealingStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("heal_per_tick").forGetter(HealingStack::encodedHealPerTick),
                Codec.INT.fieldOf("remaining_ticks").forGetter(HealingStack::remainingTicks),
                Codec.INT.optionalFieldOf("total_ticks").forGetter(stack -> stack.totalTicks == stack.remainingTicks ? Optional.empty() : Optional.of(stack.totalTicks))
        ).apply(instance, (healPerTick, remainingTicks, totalTicks) -> {
            var resolvedTotalTicks = totalTicks.orElse(remainingTicks);
            return new HealingStack(healPerTick * resolvedTotalTicks, remainingTicks, resolvedTotalTicks);
        }));

        private float encodedHealPerTick() {
            return this.totalHeal / Math.max(1, this.totalTicks);
        }

        public HealingStack withTicks(int remainingTicks) {
            return new HealingStack(this.totalHeal, remainingTicks, this.totalTicks);
        }

        public float healForCurrentTick() {
            var elapsedTicks = this.totalTicks - this.remainingTicks;
            var ticks = Math.max(1, this.totalTicks);
            var progress = (elapsedTicks + 1D) / ticks;
            var weight = progress * progress * progress;
            var weightSum = Math.pow(ticks + 1D, 2D) / (4D * ticks);

            return (float) (this.totalHeal * weight / weightSum);
        }

        public float remainingHeal() {
            var ticks = Math.max(1, this.totalTicks);
            var elapsed = Math.max(0, this.totalTicks - this.remainingTicks);
            var totalWeight = Math.pow(ticks + 1D, 2D) / (4D * ticks);
            var elapsedWeight = Math.pow(elapsed * (elapsed + 1D) / 2D, 2D) / Math.pow(ticks, 3D);
            var remainingWeight = Math.max(0D, totalWeight - elapsedWeight);

            return totalWeight <= 0D ? 0F : (float) (this.totalHeal * (remainingWeight / totalWeight));
        }
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onLivingDamage1(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();

            if (!(entity instanceof Player player))
                return;

            for (var stack : EntityUtils.findItemsInInventory(player, RelicsItems.SPHERE_OF_SELF_SACRIFICE.get())) {
                var relic = (SphereOfSelfSacrifice) stack.getItem();

                if (!relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").canPlayerUse(player))
                    continue;

                if (!relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").isRankModifierUnlocked("salvation"))
                    continue;

                var stacks = relic.getHealingStacks(stack);
                var maxStacks = Math.max(1, (int) Math.ceil(relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").getStatData("stacks").getValue()));
                var abilityDamage = relic.getHealthCost(player);

                if (stacks.size() >= maxStacks)
                    continue;

                if (event.getNewDamage() <= abilityDamage)
                    continue;

                var incomingDamage = event.getNewDamage();

                event.setNewDamage(abilityDamage);

                relic.addHealingStack(player, stack, relic.buildHealingStack(player));

                var prevented = Math.max(0F, incomingDamage - abilityDamage);

                relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").getStatisticData().getMetricData("salvation_triggers").addValue(1);
                relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").getStatisticData().getMetricData("salvation_damage_blocked").addValue(prevented);

                relic.getRelicData(player, stack).getLevelingData().addExperience("sacrifice", "salvation", prevented > 0 ? prevented : 1);
            }
        }

        @SubscribeEvent
        public static void onLivingDamage2(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();

            if (!(entity instanceof Player player))
                return;

            for (var stack : EntityUtils.findItemsInInventory(player, RelicsItems.SPHERE_OF_SELF_SACRIFICE.get())) {
                var relic = (SphereOfSelfSacrifice) stack.getItem();

                if (!relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").canPlayerUse(player))
                    continue;

                if (!relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").isRankModifierUnlocked("resistance"))
                    continue;

                var stacks = relic.getHealingStacks(stack);

                if (stacks.isEmpty())
                    continue;

                var modifier = event.getNewDamage() * stacks.size() * relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").getStatData("resistance").getValue();

                event.setNewDamage((float) Math.max(0, event.getNewDamage() - modifier));

                relic.getRelicData(player, stack).getAbilitiesData().getAbilityData("sacrifice").getStatisticData().getMetricData("damage_resisted").addValue(modifier);

                relic.getRelicData(player, stack).getLevelingData().addExperience("sacrifice", "resisting_damage", modifier);
            }
        }
    }
}