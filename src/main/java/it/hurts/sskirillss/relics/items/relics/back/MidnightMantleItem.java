package it.hurts.sskirillss.relics.items.relics.back;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.DataComponentRegistry;
import it.hurts.sskirillss.relics.init.EffectRegistry;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
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
                                        .initialValue(0.10D, 0.25D)
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
        var level = entity.getCommandSenderWorld();

        if (this.canPlayerUseAbility(entity, stack, "phase")) {
            var mode = this.getAbilityMode(entity, stack, "phase");

            if (!mode.isEmpty()) {
                var totalEffectiveness = this.getModeEffectiveness(entity, stack);
                var attackEffectiveness = mode.equals("full_moon") ? totalEffectiveness : 0D;
                var healEffectiveness = mode.equals("new_moon") ? totalEffectiveness : 0D;

                EntityUtils.resetAttribute(entity, stack, Attributes.ATTACK_SPEED, (float) (this.getStatValue(entity, stack, "phase", "attack_speed") * attackEffectiveness), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                EntityUtils.resetAttribute(entity, stack, Attributes.ATTACK_DAMAGE, (float) (this.getStatValue(entity, stack, "phase", "attack_damage") * attackEffectiveness), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                EntityUtils.resetAttribute(entity, stack, Attributes.MAX_HEALTH, (float) (this.getStatValue(entity, stack, "phase", "max_health") * healEffectiveness), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            }
        }

        if (this.canPlayerUseAbility(entity, stack, "invisibility")) {
            var cooldown = this.getInvisibilityCooldown(stack);

            if (cooldown > 0) {
                if (level.getEntitiesOfClass(Mob.class, entity.getBoundingBox().inflate(16)).stream().noneMatch(mob -> mob.getTarget() == entity && !mob.hasLineOfSight(entity)))
                    this.addInvisibilityCooldown(stack, -1);
            } else if (!level.isClientSide() && this.canHideInTheDarkness(entity, stack))
                entity.addEffect(new MobEffectInstance(EffectRegistry.VANISHING, 5, 0, false, false));
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

                if (!relic.canPlayerUseAbility(entity, stack, "phase"))
                    continue;

                var mode = relic.getAbilityMode(entity, stack, "phase");

                if (mode.isEmpty())
                    continue;

                var totalEffectiveness = relic.getModeEffectiveness(entity, stack);
                var healEffectiveness = mode.equals("new_moon") ? totalEffectiveness : 0D;

                event.setAmount((float) (event.getAmount() + (event.getAmount() * healEffectiveness)));
            }
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onBlockBreakAttempt(PlayerEvent.BreakSpeed event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onAttackEntity(AttackEntityEvent event) {
            onInteract(event.getEntity());
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingIncomingDamageEvent event) {

        }
    }
}