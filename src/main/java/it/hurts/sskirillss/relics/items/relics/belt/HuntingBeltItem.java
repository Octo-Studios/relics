package it.hurts.sskirillss.relics.items.relics.belt;

import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsItems;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import top.theillusivec4.curios.api.SlotContext;

public class HuntingBeltItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("pack")
                                .rankModifier(1, "leader")
                                .rankModifier(3, "relentless")
                                .stat(StatTemplate.builder("damage_modifier")
                                        .initialValue(0.1D, 0.25D)
                                        .upgradeModifier(RelicsScalingModels.MULTIPLICATIVE_BASE.get(), 0.0667D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("pet_radius")
                                        .initialValue(6D, 10D)
                                        .upgradeModifier(RelicsScalingModels.LOGARITHMIC.get(), 3.0276D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("health_per_pet")
                                        .initialValue(1D, 2D)
                                        .upgradeModifier(RelicsScalingModels.ADDITIVE.get(), 0.25D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_END)
                        .entry(LootEntries.END_LIKE)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        super.curioTick(slotContext, stack);

        var entity = slotContext.entity();

        if (!this.canPlayerUseAbility(entity, stack, "pack")
                || !this.isAbilityRankModifierUnlocked(entity, stack, "pack", "leader")) {
            EntityUtils.removeAttribute(entity, stack, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE);
            return;
        }

        var radius = this.getStatValue(entity, stack, "pack", "pet_radius");

        var pets = entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(radius),
                target -> target instanceof OwnableEntity ownable && ownable.getOwner() != null
                        && ownable.getOwner().getUUID().equals(entity.getUUID()));

        var bonusHealth = (float) (pets.size() * this.getStatValue(entity, stack, "pack", "health_per_pet"));

        if (bonusHealth > 0F)
            EntityUtils.resetAttribute(entity, stack, Attributes.MAX_HEALTH, bonusHealth, AttributeModifier.Operation.ADD_VALUE);
        else
            EntityUtils.removeAttribute(entity, stack, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        EntityUtils.removeAttribute(slotContext.entity(), stack, Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_VALUE);
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent.Pre event) {
            if (!(event.getSource().getEntity() instanceof OwnableEntity ownable))
                return;

            if (!(ownable.getOwner() instanceof LivingEntity owner))
                return;

            var totalModifier = 0D;
            var ignoreInvulnerability = false;

            for (var stack : EntityUtils.findEquippedCurios(owner, RelicsItems.HUNTING_BELT.get())) {
                var relic = (HuntingBeltItem) stack.getItem();

                if (!relic.canPlayerUseAbility(owner, stack, "pack"))
                    continue;

                totalModifier += relic.getStatValue(owner, stack, "pack", "damage_modifier");
                ignoreInvulnerability |= relic.isAbilityRankModifierUnlocked(owner, stack, "pack", "relentless");
            }

            if (ignoreInvulnerability)
                event.getEntity().invulnerableTime = 0;

            if (totalModifier <= 0D)
                return;

            event.setNewDamage((float) (event.getNewDamage() * (1D + totalModifier)));
        }
    }
}
