package it.hurts.sskirillss.relics.items.relics.back;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.FallingStarEntity;
import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import top.theillusivec4.curios.api.SlotContext;

public class MidnightMantleItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("phase")
                                .modes("full_moon", "new_moon")
                                .stat(StatTemplate.builder("attack_damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("max_health")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("health_regeneration")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("attack_speed")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("invisibility")
                                .stat(StatTemplate.builder("brightness")
                                        .thresholdValue(0D, 1D)
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("cooldown")
                                        .initialValue(10D, 15D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), -0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.35D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("starfall")
                                .rankModifier(1, "stun")
                                .stat(StatTemplate.builder("chance")
                                        .thresholdValue(0D, 1D)
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.025D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("stun")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xffef9b53)
                                .borderBottom(0xfff3f38f)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_END, LootEntries.END_LIKE)
                        .build())
                .build();
    }

    public double getModeEffectiveness(LivingEntity entity, ItemStack stack) {
        var mode = this.getAbilityMode(entity, stack, "phase");
        var level = entity.getCommandSenderWorld();

        if (mode.isEmpty() || level.isDay())
            return 0;

        var maxPhase = 7;
        var selectedPhase = mode.equals("full_moon") ? maxPhase : 0;
        var currentPhase = level.getMoonPhase();

        var directDistance = Math.abs(currentPhase - selectedPhase);
        var wrappedDistance = 8 - directDistance;
        var minDistance = Math.min(directDistance, wrappedDistance);

        return 1 - (minDistance / 4D);
    }

    public boolean canHideInTheDarkness(LivingEntity entity, ItemStack stack) {
        var level = entity.getCommandSenderWorld();
        var pos = entity.blockPosition();

        var blockBrightness = level.getBrightness(LightLayer.BLOCK, pos);
        var skyBrightness = level.getBrightness(LightLayer.SKY, pos);
        var maxBrightness = level.getMaxLightLevel();
        var skyDarken = level.getSkyDarken();
        var maxSkyDarken = 11D;

        return ((1D - (skyDarken / maxSkyDarken)) * skyBrightness + blockBrightness) / (maxBrightness * 2D) <= this.getStatValue(entity, stack, "invisibility", "brightness");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();
        var level = entity.level();

        if (this.canPlayerUseAbility(entity, stack, "phase")) {
            var mode = this.getAbilityMode(entity, stack, "phase");

            if (!mode.isEmpty()) {
                var totalEffectiveness = this.getModeEffectiveness(entity, stack);

                var attackEffectiveness = mode.equals("full_moon") ? totalEffectiveness : 0D;
                var healEffectiveness = mode.equals("new_moon") ? totalEffectiveness : 0D;

                EntityUtils.resetAttribute(entity, stack, Attributes.ATTACK_SPEED, (float) (this.getStatValue(entity, stack, "phase", "attack_speed") * attackEffectiveness), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                EntityUtils.resetAttribute(entity, stack, Attributes.MAX_HEALTH, (float) (this.getStatValue(entity, stack, "phase", "max_health") * healEffectiveness), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            }
        }

        if (this.canPlayerUseAbility(entity, stack, "invisibility")) {
            var cooldown = this.getInvisibilityCooldown(stack);

            if (cooldown > 0) {
                if (level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(16)).stream().noneMatch(mob -> mob.getTarget() == entity && !mob.hasLineOfSight(entity)))
                    this.addInvisibilityCooldown(stack, -1);
            } else if (!level.isClientSide() && this.canHideInTheDarkness(entity, stack))
                entity.addEffect(new MobEffectInstance(RelicsMobEffects.VANISHING, 5, 0, false, false));
        }
    }

    public int getInvisibilityCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.COOLDOWN.get(), 0);
    }

    public void setInvisibilityCooldown(ItemStack stack, int cooldown) {
        stack.set(DataComponentRegistry.COOLDOWN.get(), cooldown);
    }

    public void addInvisibilityCooldown(ItemStack stack, int cooldown) {
        setInvisibilityCooldown(stack, getInvisibilityCooldown(stack) + cooldown);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        private static void onInteract(LivingEntity entity) {
            if (entity.getCommandSenderWorld().isClientSide())
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "invisibility"))
                    continue;

                relic.setInvisibilityCooldown(stack, (int) (relic.getStatValue(entity, stack, "invisibility", "cooldown") * 20));
            }
        }

        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "phase")  || !relic.getAbilityMode(entity, stack, "phase").equals("new_moon"))
                    continue;

                event.setAmount((float) (event.getAmount() + (event.getAmount() * relic.getModeEffectiveness(entity, stack))));
            }
        }

        @SubscribeEvent
        public static void onLivingHurt1(LivingIncomingDamageEvent event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "phase") || !relic.getAbilityMode(entity, stack, "phase").equals("full_moon"))
                    continue;

                event.setAmount((float) (event.getAmount() + (event.getAmount() * relic.getModeEffectiveness(entity, stack))));
            }
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onBlockBreakAttempt(PlayerEvent.BreakSpeed event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            CommonEvents.onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onLivingHurt2(LivingIncomingDamageEvent event) {
            if (event.getAmount() < 1D || !(event.getSource().getEntity() instanceof LivingEntity entity))
                return;

            var target = event.getEntity();

            var level = entity.level();
            var random = level.getRandom();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.MIDNIGHT_MANTLE.get())) {
                var relic = (MidnightMantleItem) stack.getItem();

                if (random.nextFloat() > relic.getStatValue(entity, stack, "starfall", "chance"))
                    continue;

                var pos = new Vec3(target.getX() + MathUtils.randomFloat(random) * 10, target.getY() + 25 + random.nextInt(25), target.getZ() + MathUtils.randomFloat(random) * 10);
                var motion = target.position().subtract(pos).normalize().scale(2F);

                var star = new FallingStarEntity(RelicsEntities.FALLING_STAR.get(), level);

                star.setDamage((float) (event.getOriginalAmount() * relic.getStatValue(entity, stack, "starfall", "damage")));
                star.setFlawless(relic.isRelicFlawless(entity, stack));
                star.setMotion(motion);
                star.setOwner(entity);
                star.setPos(pos);

                if (relic.isAbilityRankModifierUnlocked(entity, stack, "starfall", "stun"))
                    star.setStun((int) (relic.getStatValue(entity, stack, "starfall", "stun") * 20));

                level.addFreshEntity(star);
            }
        }
    }
}