package it.hurts.sskirillss.relics.items.relics;

import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.WorldUtils;
import it.hurts.sskirillss.relics.utils.data.WorldPosition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotContext;

import java.awt.*;
import java.util.List;

import static it.hurts.sskirillss.relics.init.DataComponentRegistry.*;

public class BlazingFlaskItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("bonfire")
                                .stat(StatTemplate.builder("step")
                                        .initialValue(1D, 2.5D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat(StatTemplate.builder("speed")
                                        .initialValue(0.01D, 0.05D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 1.9D)
                                        .formatValue(value -> MathUtils.round(value * 8, 1))
                                        .build())
                                .stat(StatTemplate.builder("height")
                                        .initialValue(3D, 5D)
                                        .upgradeModifier(ScalingModelRegistry.ADDITIVE.get(), 1D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(new LevelingTemplate(100, 10, 100))
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_NETHER, LootEntries.NETHER_LIKE)
                        .build())
                .build();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        if (!(entity instanceof Player player))
            return;

        Level world = player.getCommandSenderWorld();

        int fire = getFireAround(stack, world);

        if (fire <= 0) {
            stack.set(WORLD_POSITION, null);
        } else {
            stack.set(COUNT, fire);
        }

        WorldPosition center = stack.get(WORLD_POSITION);

        if (center != null) {
            double radius = stack.getOrDefault(RADIUS, 0D);

            if (!player.isCreative() && !player.isSpectator() && !player.getAbilities().flying && !player.getAbilities().mayfly) {
                if (new Vec3(player.getX(), center.getPos().y(), player.getZ()).distanceTo(center.getPos()) <= radius + 0.5F) {
                    player.fallDistance = 0F;

                    if (player.tickCount % 100 == 0)
                        spreadRelicExperience(player, stack, 1);

                    double speed = getStatValue(stack, "bonfire", "speed");

                    if (world.isClientSide()) {
                        if (!player.onGround() && (player.zza != 0 || player.xxa != 0))
                            player.move(MoverType.SELF, player.getDeltaMovement().multiply(speed, 0, speed));

                        if (player instanceof LocalPlayer localPlayer && localPlayer.input.jumping
                                && (WorldUtils.getGroundHeight(player, player.position(), 64) + getStatValue(stack, "bonfire", "height")) - player.getY() > 0) {
                            Vec3 motion = player.getDeltaMovement();

                            if (motion.y() < 0)
                                player.setDeltaMovement(motion.x(), motion.y() * 0.9F, motion.z());

                            player.setDeltaMovement(player.getDeltaMovement().add(0F, 0.1F, 0F));
                        }
                    }
                }
            }

            double size = stack.getOrDefault(COUNT, 0) * getStatValue(stack, "bonfire", "step");
            double step = 0.1D;
            int time = 0;

            if (radius < size) {
                if (radius + step < size)
                    radius += step;
                else
                    radius = size;

                time = 10;

                stack.set(RADIUS, radius);
            }

            if (radius > size) {
                if (radius - step > size)
                    radius -= step;
                else
                    radius = size;

                time = 10;

                stack.set(RADIUS, radius);
            }

            if (radius <= step)
                ParticleUtils.createBall(ParticleUtils.constructSimpleSpark(new Color(255, 100, 0), 0.3F, 20, 0.9F),
                        center.getPos(), level, 3, 0.2F);

            ParticleUtils.createCyl(ParticleUtils.constructSimpleSpark(new Color(255, 100, 0), 0.2F, time, 0.8F),
                    center.getPos(), level, radius, 0.15F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        Vec3 view = player.getViewVector(0);
        Vec3 eyeVec = player.getEyePosition(0);

        float distance = (float) (8F + getStatValue(stack, "bonfire", "height"));

        Vec3 end = level.clip(new ClipContext(eyeVec, eyeVec.add(view.x * distance, view.y * distance,
                view.z * distance), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();

        if (getFireAround(stack, end, level) > 0) {
            WorldPosition center = stack.get(WORLD_POSITION);

            double radius = stack.getOrDefault(RADIUS, 0D);

            stack.set(RADIUS, (center != null && end.distanceTo(center.getPos()) <= radius) ? radius - center.getPos().distanceTo(end) : 0D);
            stack.set(WORLD_POSITION, new WorldPosition(player.level().dimension(), end));

            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.pass(stack);
    }

    public int getFireAround(ItemStack stack, Level level) {
        WorldPosition center = stack.get(WORLD_POSITION);

        if (center == null)
            return 0;

        return getFireAround(stack, center.getPos(), level);
    }

    public int getFireAround(ItemStack stack, Vec3 center, Level level) {
        List<BlockPos> positions = WorldUtils.getBlockSphere(new BlockPos((int) center.x, (int) center.y, (int) center.z), getStatValue(stack, "bonfire", "step"))
                .stream().filter(pos -> (level.getBlockState(pos).getBlock() instanceof BaseFireBlock)).toList();

        return positions.size();
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }
}