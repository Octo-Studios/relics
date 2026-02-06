package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.api.events.common.EntityBlockSpeedFactorEvent;
import it.hurts.sskirillss.relics.api.events.common.LivingSlippingEvent;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.curios.api.SlotContext;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;

public class RollerSkatesItem extends RelicItem {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("skating")
                                .stat(StatData.builder("speed")
                                        .initialValue(0.001D, 0.005D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.15D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 3) * 100 * 100))
                                        .build())
                                .stat(StatData.builder("duration")
                                        .initialValue(15D, 35D)
                                        .upgradeModifier(UpgradeOperation.MULTIPLY_BASE, 0.2D)
                                        .formatValue(value -> MathUtils.round(value / 5, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .loot(LootData.builder()
                        .entry(LootEntries.OVERWORLD)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        int duration = stack.getOrDefault(CHARGE, 0);

        if (player.isSprinting() && !player.isShiftKeyDown() && !player.isInWater() && !player.isInLava()) {
            if (player.tickCount % 20 == 0)
                spreadRelicExperience(player, stack, 1);

            if (duration < getStatValue(stack, "skating", "duration") && player.tickCount % 4 == 0)
                stack.set(CHARGE, duration + 1);
        } else if (duration > 0)
            stack.set(CHARGE, --duration);

        EntityUtils.removeAttribute(player, stack, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        if (duration > 0) {
            EntityUtils.applyAttribute(player, stack, Attributes.MOVEMENT_SPEED, (float) (duration * getStatValue(stack, "skating", "speed")), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            EntityUtils.applyAttribute(player, stack, Attributes.STEP_HEIGHT, 0.6F, AttributeModifier.Operation.ADD_VALUE);
        } else
            EntityUtils.removeAttribute(player, stack, Attributes.STEP_HEIGHT, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (stack.getItem() == newStack.getItem())
            return;

        LivingEntity entity = slotContext.entity();

        EntityUtils.removeAttribute(entity, stack, Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        EntityUtils.removeAttribute(entity, stack, Attributes.STEP_HEIGHT, AttributeModifier.Operation.ADD_VALUE);
    }

    @EventBusSubscriber(modid = Reference.MODID)
    public static class Events {
        @SubscribeEvent
        public static void onLivingSlipping(LivingSlippingEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.isInLiquid() || player.isFallFlying() || !player.onGround() || player.isSwimming())
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ROLLER_SKATES.get());

            if (stack.isEmpty())
                return;

            event.setFriction(1.075F);
        }

        @SubscribeEvent
        public static void onSpeedFactor(EntityBlockSpeedFactorEvent event) {
            if (!(event.getEntity() instanceof Player player) || player.isInLiquid() || player.isFallFlying() || !player.onGround() || player.isSwimming())
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.ROLLER_SKATES.get());

            if (stack.isEmpty())
                return;

            event.setSpeedFactor(1F);
        }
    }
}