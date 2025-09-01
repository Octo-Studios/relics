package it.hurts.sskirillss.relics.items.relics;

import it.hurts.sskirillss.relics.api.events.common.ContainerSlotClickEvent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.items.relics.base.data.style.StyleTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.S2CSetEntityMotion;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.awt.*;

public class ChorusStaffItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("blink")
                                .initialMaxLevel(10)
                                .stat(StatTemplate.builder("distance")
                                        .initialValue(10D, 15D)
                                        .upgradeModifier(RelicsScalingModels.RADICAL.get(), 0.1268D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .stat(StatTemplate.builder("max_charge")
                                        .initialValue(10D, 15D)
                                        .upgradeModifier(RelicsScalingModels.RADICAL.get(), 0.1268D)
                                        .formatValue(value -> (int) MathUtils.round(value * 100, 0))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .step(200)
                        .build())
                .style(StyleTemplate.builder()
                        .tooltip(TooltipData.builder()
                                .borderTop(0xff00baff)
                                .borderBottom(0xff0090a9)
                                .textured(true)
                                .build())
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.NETHER_LIKE, LootEntries.THE_NETHER)
                        .build())
                .build();
    }

    public int getMaxCharge(LivingEntity entity, ItemStack stack) {
        return (int) this.getStatValue(entity, stack, "blink", "max_charge");
    }

    public int getCharge(LivingEntity entity, ItemStack stack) {
        return Math.clamp(stack.getOrDefault(RelicsDataComponents.CHARGE, 0), 0, this.getMaxCharge(entity, stack));
    }

    public void setCharge(LivingEntity entity, ItemStack stack, int charge) {
        stack.set(RelicsDataComponents.CHARGE, Math.clamp(charge, 0, this.getMaxCharge(entity, stack)));
    }

    public void addCharge(LivingEntity entity, ItemStack stack, int charge) {
        this.setCharge(entity, stack, this.getCharge(entity, stack) + charge);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        var radius = this.getStatValue(player, stack, "blink", "distance");

        var eyePos = player.getEyePosition();
        var lookAngle = player.getLookAngle().normalize();

        var destination = eyePos.add(lookAngle.scale(radius));

        var hit = level.clip(new ClipContext(eyePos, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        var impulse = Vec3.ZERO;

        if (hit.getType() == HitResult.Type.BLOCK && hit.getLocation().distanceTo(eyePos) <= radius) {
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

        if (level.isClientSide) {
            var random = level.getRandom();

            var shift = player.getBbHeight() / 2F;
            var from = player.position().add(0F, shift, 0F);
            var to = destination.add(0F, shift, 0F);

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

                    level.addParticle(ParticleUtils.constructSimpleSpark(new Color(155 + random.nextInt(100), 80, 255), 1F, 3, 0.5F), px, py, pz, 0F, 0F, 0F);
                }
            }
        }

        if (!level.isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.teleportTo((ServerLevel) level, destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
                serverPlayer.fallDistance = 0;
                NetworkHandler.sendToClient(new S2CSetEntityMotion(player.getId(), impulse.toVector3f()), serverPlayer);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onSlotClick(ContainerSlotClickEvent event) {

        }
    }
}