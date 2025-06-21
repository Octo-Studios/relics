package it.hurts.sskirillss.relics.items.relics.necklace;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.entities.ElectricSparkEntity;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import top.theillusivec4.curios.api.SlotContext;

import java.util.function.Predicate;

public class JellyfishNecklaceItem extends RelicItem {
    public static final int MAX_RINGS = 5;

    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("regeneration")
                                .maxLevel(10)
                                .stat(StatTemplate.builder("max_health")
                                        .initialValue(0.1D, 0.2D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("regeneration")
                                        .initialValue(0.15D, 0.35D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .ability(AbilityTemplate.builder("shock")
                                .stat(StatTemplate.builder("cooldown")
                                        .initialValue(60D, 30D)
                                        .thresholdValue(0, Double.MAX_VALUE)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), -0.1D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("radius")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("knockback")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("paralysis")
                                        .initialValue(0.25D, 0.75D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.15D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("bounces")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("damage")
                                        .initialValue(0.5D, 2.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("damage_modifier")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("duration")
                                        .initialValue(0.25D, 0.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff00baff)
                                .borderBottom(0xff0090a9)
                                .textured(true)
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(200)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.AQUATIC)
                        .build())
                .build();
    }

    public int getCooldown(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.JELLYFISH_NECKLACE_COOLDOWN.get(), 0);
    }

    public void setCooldown(ItemStack stack, int cooldown) {
        stack.set(DataComponentRegistry.JELLYFISH_NECKLACE_COOLDOWN, Math.max(0, cooldown));
    }

    public void addCooldown(ItemStack stack, int cooldown) {
        this.setCooldown(stack, this.getCooldown(stack) + cooldown);
    }

    public int getDuration(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.JELLYFISH_NECKLACE_DURATION.get(), 0);
    }

    public void setDuration(ItemStack stack, int duration) {
        stack.set(DataComponentRegistry.JELLYFISH_NECKLACE_DURATION, Math.max(0, duration));
    }

    public void addDuration(ItemStack stack, int duration) {
        this.setDuration(stack, this.getDuration(stack) + duration);
    }

    public int getRings(ItemStack stack) {
        return stack.getOrDefault(DataComponentRegistry.JELLYFISH_NECKLACE_RINGS, 0);
    }

    public void setRings(ItemStack stack, int rings) {
        stack.set(DataComponentRegistry.JELLYFISH_NECKLACE_RINGS, Math.max(0, rings));
    }

    public void addRings(ItemStack stack, int rings) {
        this.setRings(stack, this.getRings(stack) + rings);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        var entity = slotContext.entity();
        var level = entity.level();

        // TODO: Switch to something like health boost
        if (this.canPlayerUseAbility(entity, stack, "regeneration") && entity.isInWater())
            EntityUtils.resetAttribute(entity, stack, Attributes.MAX_HEALTH, (float) (entity.getMaxHealth() * this.getStatValue(entity, stack, "regeneration", "max_health")), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        if (this.canPlayerUseAbility(entity, stack, "shock")) {
            var cooldown = this.getCooldown(stack);
            var duration = this.getDuration(stack);
            var rings = this.getRings(stack);

            if (rings < MAX_RINGS) {
                var maxCooldown = (int) this.getStatValue(entity, stack, "shock", "cooldown");

                if (cooldown > 0)
                    this.addCooldown(stack, -1);
                else {
                    this.addRings(stack, 1);
                    this.setCooldown(stack, maxCooldown);
                }
            }

            if (duration > 0)
                this.addDuration(stack, -1);

            Predicate<LivingEntity> predicate = entry -> !entry.getStringUUID().equals(entity.getStringUUID());
            var collisions = level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox(), predicate);

            if (!collisions.isEmpty()) {
                var radius = this.getStatValue(entity, stack, "shock", "radius");

                for (var target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(radius), predicate)) {
                    var diff = target.position().add(0, target.getBbHeight() / 2D, 0).subtract(entity.position());

                    var distance = diff.length();

                    if (distance > 0 && distance <= radius) {
                        var factor = (radius - distance) / radius;
                        var knockback = entity.getAttributeValue(Attributes.ATTACK_KNOCKBACK) * this.getStatValue(entity, stack, "shock", "knockback");

                        target.setDeltaMovement(diff.normalize().scale(knockback * factor));
                    }

                    target.addEffect(new MobEffectInstance(EffectRegistry.PARALYSIS, (int) (this.getStatValue(entity, stack, "shock", "paralysis") * 20), 0, false, true));

                    var spark = new ElectricSparkEntity(RelicsEntities.ELECTRIC_SPARK.get(), level);

                    spark.setBounces((int) this.getStatValue(entity, stack, "shock", "bounces") + 20);
                    spark.setDamage((float) this.getStatValue(entity, stack, "shock", "damage"));
                    spark.setPos(target.getEyePosition());
                    spark.setOwner(entity);

                    level.addFreshEntity(spark);
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        super.onUnequip(slotContext, newStack, stack);

        if (stack.getItem() == newStack.getItem())
            return;

        var entity = slotContext.entity();

        EntityUtils.resetAttribute(entity, stack, Attributes.MAX_HEALTH, (float) (entity.getMaxHealth() * this.getStatValue(entity, stack, "regeneration", "max_health")), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            var entity = event.getEntity();

            for (var stack : EntityUtils.findEquippedCurios(entity, RelicsItems.JELLYFISH_NECKLACE.get())) {
                var relic = (JellyfishNecklaceItem) stack.getItem();

                if (!relic.canPlayerUseAbility(entity, stack, "jelly"))
                    continue;

                event.setAmount((float) (event.getAmount() + (event.getAmount() * relic.getStatValue(entity, stack, "regeneration", "regeneration"))));
            }
        }
    }
}