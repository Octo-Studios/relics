package it.hurts.sskirillss.relics.items.relics.back;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.AbilityMetricTemplate;
import it.hurts.sskirillss.relics.api.relics.AbilityStatisticTemplate;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.VisibilityState;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourceTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.ExperienceSourcesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.LeavesBlockEntity;
import it.hurts.sskirillss.relics.init.RelicsEntities;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.research.ResearchTemplate;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ServerScheduler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.Comparator;
import java.util.stream.IntStream;

import static it.hurts.sskirillss.relics.init.RelicsDataComponents.PROGRESS;
import static it.hurts.sskirillss.relics.init.RelicsDataComponents.TOGGLED;

public class LeafyMantleItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("camouflage")
                                .rankModifier(3, "absorption")
                                .rankModifier(5, "disappearance")
                                .stat(StatTemplate.builder("heal")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 0.6279D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("absorption")
                                        .initialValue(1D, 3D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.1619D)
                                        .formatValue(value -> (int) MathUtils.round(value, 0))
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("hide_duration")
                                                .formatValue((value) -> MathUtils.formatTime(value.intValue()))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("heal_amount")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("hiding")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("healing")
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 8, 15).star(1, 14, 15).star(2, 2, 17).star(3, 20, 17).star(4, 2, 22).star(5, 11, 22).star(6, 20, 22).star(7, 2, 27).star(8, 20, 27)
                                        .link(5, 2).link(5, 3).link(5, 4).link(5, 6).link(5, 7).link(5, 8).link(0, 1)
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("revival")
                                .requiredLevel(5)
                                .rankModifier(1, "piercing")
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(RelicsScalingModels.EXPONENTIAL.get(), 0.0265D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("heal")
                                        .initialValue(0.5D, 1D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 1.1162D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.5D, 1D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 1.1162D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("paralysis")
                                        .initialValue(0.5D, 1D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 1.1162D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .statistic(AbilityStatisticTemplate.builder()
                                        .metric(AbilityMetricTemplate.builder("leaves_consumed")
                                                .formatValue((value) -> String.valueOf((int) MathUtils.round(value, 0)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_negated")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("damage_dealt")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("piercing", VisibilityState.OBFUSCATED)
                                                .build())
                                        .metric(AbilityMetricTemplate.builder("paralysis_duration")
                                                .formatValue((value) -> String.valueOf(MathUtils.round(value, 1)))
                                                .rankModifierVisibilityState("piercing", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .experienceSources(ExperienceSourcesTemplate.builder()
                                        .source(ExperienceSourceTemplate.builder("consuming_leaves")
                                                .build())
                                        .source(ExperienceSourceTemplate.builder("leaves_impact")
                                                .rankModifierVisibilityState("piercing", VisibilityState.OBFUSCATED)
                                                .build())
                                        .build())
                                .research(ResearchTemplate.builder()
                                        .star(0, 13, 5).star(1, 2, 14).star(2, 11, 17).star(3, 20, 19).star(4, 8, 28)
                                        .link(2, 0).link(2, 4).link(2, 1).link(2, 3)
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .build();
    }

    public boolean isHiding(ItemStack stack) {
        return stack.getOrDefault(TOGGLED, false);
    }

    public void setHiding(ItemStack stack, boolean hiding) {
        stack.set(TOGGLED, hiding);
    }

    public int getCurrentProgress(ItemStack stack) {
        return stack.getOrDefault(PROGRESS, 0);
    }

    public void setCurrentProgress(ItemStack stack, int progress) {
        stack.set(PROGRESS, Math.clamp(progress, 0, this.getMaxProgress()));
    }

    public void addCurrentProgress(ItemStack stack, int progress) {
        this.setCurrentProgress(stack, this.getCurrentProgress(stack) + progress);
    }

    public int getMaxProgress() {
        return 10;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof LivingEntity entity))
            return;

        var level = entity.getCommandSenderWorld();

        var progress = this.getCurrentProgress(stack);
        var hiding = this.isHiding(stack);

        boolean inLeaves = BlockPos
                .betweenClosedStream(entity.getBoundingBox())
                .anyMatch(pos -> level.getBlockState(pos).is(BlockTags.LEAVES));

        if (inLeaves) {
            if (!hiding)
                this.setHiding(stack, true);
            if (progress < this.getMaxProgress())
                this.addCurrentProgress(stack, 1);

            if (this.isAbilityRankModifierUnlocked(entity, stack, "camouflage", "disappearance"))
                entity.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 5, 0, false, false));

            if (entity.tickCount % 20 == 0) {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    var heal = (float) Math.min(this.getStatValue(entity, stack, "camouflage", "heal"), entity.getMaxHealth() - entity.getHealth());

                    entity.heal(heal);

                    if (!level.isClientSide()) {
                        this.addAbilityMetricValue(entity, stack, "camouflage", "heal_amount", heal);

                        this.addRelicExperience(entity, stack, "camouflage", "healing", heal);
                    }
                }

                if (!level.isClientSide()) {
                    this.addRelicExperience(entity, stack, "camouflage", "hiding", 1);

                    this.addAbilityMetricValue(entity, stack, "camouflage", "hide_duration", 1);
                }
            }

            if (level.isClientSide()) {
                if (entity instanceof LocalPlayer player && player.input.jumping) {
                    var motion = player.getDeltaMovement();

                    player.setDeltaMovement(motion.x(), 0.25F, motion.z());
                }
            }
        } else {
            if (hiding)
                this.setHiding(stack, false);

            if (progress > 0)
                this.addCurrentProgress(stack, -1);
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (stack.getItem() == newStack.getItem())
            return;

        var entity = slotContext.entity();

        EntityUtils.removeAttribute(entity, Attributes.MAX_ABSORPTION, AttributeModifier.Operation.ADD_VALUE, CommonEvents.ATTRIBUTE);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        public static final ResourceLocation ATTRIBUTE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "leafy_mantle/absorption");

        @SubscribeEvent
        public static void onLivingTick(EntityTickEvent.Post event) {
            if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide())
                return;

            var total = 0F;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.LEAFY_MANTLE.get())) {
                var relic = (LeafyMantleItem) stack.getItem();

                if (!relic.isHiding(stack))
                    continue;

                if (relic.canPlayerUseAbility(entity, stack, "camouflage") && relic.isAbilityRankModifierUnlocked(entity, stack, "camouflage", "absorption"))
                    total += (float) relic.getStatValue(entity, stack, "camouflage", "absorption");
            }

            var current = entity.getAbsorptionAmount();
            var cap = Math.max(current, total);

            if (cap > 0F) {
                EntityUtils.resetAttribute(entity, Attributes.MAX_ABSORPTION, cap, AttributeModifier.Operation.ADD_VALUE, ATTRIBUTE);

                if (current < total)
                    entity.setAbsorptionAmount(total);
            }
        }

        @SubscribeEvent
        public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
            if (!event.getSource().is(DamageTypeTags.IS_FALL))
                return;

            var entity = event.getEntity();
            var level = entity.level();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.LEAFY_MANTLE.get())) {
                var relic = (LeafyMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "camouflage") || !level.getBlockState(entity.getBlockPosBelowThatAffectsMyMovement()).is(BlockTags.LEAVES))
                    continue;

                event.setCanceled(true);

                break;
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            var entity = event.getEntity();
            var level = entity.level();
            var random = level.getRandom();

            var damage = event.getNewDamage();
            var health = entity.getHealth();
            var absorption = entity.getAbsorptionAmount();

            if (damage < health)
                return;

            var damageToThreshold = health - 0.1F;
            var remaining = damage - damageToThreshold;
            var diff = absorption - remaining;

            if (diff > 0)
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.LEAFY_MANTLE.get())) {
                var relic = (LeafyMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "revival"))
                    continue;

                if (diff > 0)
                    break;

                if (!level.isClientSide())
                    relic.addAbilityMetricValue(entity, stack, "revival", "damage_negated", Math.abs(diff));

                var radius = (int) Math.ceil(relic.getStatValue(entity, stack, "revival", "radius"));
                var heal = (float) relic.getStatValue(entity, stack, "revival", "heal");

                var center = entity.blockPosition();

                var positions = IntStream.rangeClosed(-radius, radius).boxed()
                        .flatMap(dx -> IntStream.rangeClosed(-radius, radius)
                                .boxed()
                                .flatMap(dy -> IntStream.rangeClosed(-radius, radius)
                                        .mapToObj(dz -> new BlockPos(center.getX() + dx, center.getY() + dy, center.getZ() + dz))))
                        .filter(pos -> level.getBlockState(pos).is(BlockTags.LEAVES))
                        .sorted(Comparator.comparingDouble(pos -> pos.distSqr(center)))
                        .toList();

                var potential = positions.size() * heal;

                if (potential + diff < 0)
                    continue;

                var blocks = 0;

                for (var pos : positions) {
                    ServerScheduler.schedule(blocks + random.nextInt(10), () -> {
                        var leaves = new LeavesBlockEntity(RelicsEntities.LEAVES_BLOCK.get(), level);

                        var posVec = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);

                        var toPlayer = entity.position().subtract(posVec).normalize();
                        var randomVec = new Vec3(MathUtils.randomFloat(random), MathUtils.randomFloat(random), MathUtils.randomFloat(random));
                        var perpendicular = randomVec.subtract(toPlayer.scale(randomVec.dot(toPlayer))).normalize().scale(0.5D + random.nextDouble() * 0.5D);

                        leaves.setParalysis((float) relic.getStatValue(entity, stack, "revival", "paralysis"));
                        leaves.setDeltaMovement(perpendicular.scale(0.5F + random.nextFloat()));
                        leaves.setFlawless(relic.isRelicFlawless(entity, stack));
                        leaves.setPos(posVec.x(), posVec.y(), posVec.z());
                        leaves.setBlockState(level.getBlockState(pos));
                        leaves.setTarget(entity);
                        leaves.setOwner(entity);
                        leaves.setDamage(heal);
                        leaves.setStack(stack);

                        level.addFreshEntity(leaves);

                        level.destroyBlock(pos, false);

                        if (!level.isClientSide()) {
                            relic.addAbilityMetricValue(entity, stack, "revival", "leaves_consumed", 1);

                            relic.addRelicExperience(entity, stack, "revival", "consuming_leaves", 1);
                        }
                    });

                    blocks++;

                    diff += heal;

                    if (diff >= 0)
                        break;
                }

                if (blocks > 0) {
                    event.setNewDamage(damageToThreshold);

                    if (diff > 0) {
                        var healAmount = diff;

                        ServerScheduler.schedule(1, () -> entity.heal(healAmount));
                    }
                }
            }
        }
    }
}