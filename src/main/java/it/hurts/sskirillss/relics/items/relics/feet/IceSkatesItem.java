package it.hurts.sskirillss.relics.items.relics.feet;

import it.hurts.sskirillss.relics.init.UpgradeOperationRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootData;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.CHARGE;

public class IceSkatesItem extends RelicItem {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilities(AbilitiesData.builder()
                        .ability(AbilityData.builder("skating")
                                .stat(StatData.builder("speed")
                                        .initialValue(0.01D, 0.035D)
                                        .upgradeModifier(UpgradeOperationRegistry.MULTIPLICATIVE_BASE.get(), 0.15D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 3) * 10 * 100))
                                        .build())
                                .stat(StatData.builder("duration")
                                        .initialValue(25D, 50D)
                                        .upgradeModifier(UpgradeOperationRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value / 10, 1))
                                        .build())
                                .build())
                        .ability(AbilityData.builder("ram")
                                .requiredLevel(5)
                                .stat(StatData.builder("damage")
                                        .initialValue(0.05D, 0.25D)
                                        .upgradeModifier(UpgradeOperationRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value * 10, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingData(100, 10, 200))
                .loot(LootData.builder()
                        .entry(LootEntries.FROST)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        Level level = player.getCommandSenderWorld();
        BlockPos pos = player.blockPosition().atY((int) Math.floor(WorldUtils.getGroundHeight(player, player.position(), 16)));

        int duration = stack.getOrDefault(CHARGE, 0);

        int maxDuration = (int) Math.round(getStatValue(stack, "skating", "duration"));

        if (player.isSprinting() && !player.isShiftKeyDown() && !player.isInWater() && !player.isInLava()
                && (level.getBlockState(pos).is(BlockTags.ICE))) {
            if (player.tickCount % 20 == 0)
                spreadRelicExperience(player, stack, 1);

            if (duration < maxDuration && player.tickCount % 2 == 0) {
                stack.set(CHARGE, ++duration);
            }

            if (level.getRandom().nextInt(maxDuration) < duration)
                level.addParticle(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.15F,
                        player.getZ(), 0, 0.25F, 0);
        } else if (duration > 0)
            stack.set(CHARGE, Math.max(0, duration - 2));

        if (isAbilityUnlocked(stack, "ram") && duration >= 10) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox())) {
                if (entity == player || entity.hurtTime > 0)
                    continue;

                EntityUtils.hurt(entity, level.damageSources().playerAttack(player), (float) (duration * getStatValue(stack, "ram", "damage")));

                double factor = Mth.clamp(duration * 0.025D, 1D, 2D);

                entity.setDeltaMovement(entity.position().add(0, 0.5F, 0).subtract(player.position()).normalize().multiply(factor, Math.max(1, factor / 2), factor));
            }
        }

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
}