package it.hurts.sskirillss.relics.items.relics.ring;

import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.init.ScalingModelRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.StatTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.misc.LootEntries;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.awt.*;

public class ChorusInhibitorItem extends RelicItem {
    @Override
    public RelicTemplate constructDefaultRelicTemplate() {
        return RelicTemplate.builder()
                .abilities(AbilitiesTemplate.builder()
                        .ability(AbilityTemplate.builder("blink")
                                .stat(StatTemplate.builder("distance")
                                        .initialValue(16D, 32D)
                                        .upgradeModifier(ScalingModelRegistry.MULTIPLICATIVE_BASE.get(), 0.2D)
                                        .formatValue(value -> (int) (MathUtils.round(value, 0)))
                                        .build())
                                .stat(StatTemplate.builder("cooldown")
                                        .initialValue(5D, 10D)
                                        .upgradeModifier(ScalingModelRegistry.ADDITIVE.get(), -0.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .leveling(LevelingTemplate.builder()
                        .initialCost(100)
                        .maxLevel(10)
                        .step(100)
                        .build())
                .loot(LootTemplate.builder()
                        .entry(LootEntries.THE_END, LootEntries.END_LIKE)
                        .build())
                .build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player player)
                || player.getItemInHand(InteractionHand.MAIN_HAND).getItem() != Items.CHORUS_FRUIT
                || player.getCooldowns().isOnCooldown(Items.CHORUS_FRUIT) || !player.level().isClientSide())
            return;

        BlockPos pos = getEyesPos(player, stack);

        if (pos == null)
            return;

        Vec3 start = player.position().add(0, player.getBbHeight() * 0.65D, 0);
        Vec3 end = new Vec3(pos.getX() + 0.5F, pos.getY() - 0.5F, pos.getZ() + 0.5F);

        ParticleUtils.createLine(ParticleUtils.constructSimpleSpark(new Color(20, 0, 80), 0.15F, 0, 0.5F),
                player.level(), start, end, (int) Math.round(start.distanceTo(end) * 5));
    }

    @Nullable
    public BlockPos getEyesPos(Player player, ItemStack stack) {
        Level world = player.getCommandSenderWorld();
        Vec3 view = player.getViewVector(0);
        Vec3 eyeVec = player.getEyePosition(0);

        double distance = getStatValue(player, stack, "blink", "distance");

        BlockHitResult ray = world.clip(new ClipContext(eyeVec, eyeVec.add(view.x * distance, view.y * distance,
                view.z * distance), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        BlockPos pos = ray.getBlockPos();

        if (!world.getBlockState(pos).blocksMotion())
            return null;

        pos = pos.above();

        for (int i = 0; i < 10; i++) {
            if (world.getBlockState(pos).blocksMotion() || world.getBlockState(pos.above()).blocksMotion()) {
                pos = pos.above();

                continue;
            }

            return pos;
        }

        return null;
    }

    @EventBusSubscriber
    public static class Events {
        @SubscribeEvent
        public static void onChorusTeleport(EntityTeleportEvent.ChorusFruit event) {
            if (!(event.getEntity() instanceof Player player))
                return;

            ItemStack stack = EntityUtils.findEquippedCurio(player, ItemRegistry.CHORUS_INHIBITOR.get());

            if (!(stack.getItem() instanceof ChorusInhibitorItem relic))
                return;

            event.setCanceled(true);

            BlockPos pos = relic.getEyesPos(player, stack);

            if (pos == null)
                return;

            relic.spreadRelicExperience(player, stack, (int) Math.floor(player.position().distanceTo(new Vec3(pos.getX(), pos.getY(), pos.getZ())) / 10F));

            player.teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            player.level().playSound(null, pos, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1F, 1F);
            player.getCooldowns().addCooldown(Items.CHORUS_FRUIT, Math.max((int) Math.round(relic.getStatValue(player, stack, "blink", "cooldown") * 20D), 0));
        }
    }
}