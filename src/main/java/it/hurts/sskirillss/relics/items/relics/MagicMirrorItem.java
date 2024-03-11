package it.hurts.sskirillss.relics.items.relics;

import it.hurts.sskirillss.relics.client.tooltip.base.RelicStyleData;
import it.hurts.sskirillss.relics.init.ItemRegistry;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.base.RelicData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicAbilityStat;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.RelicLevelingData;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.ParticleUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.awt.*;

public class MagicMirrorItem extends RelicItem {
    @Override
    public RelicData constructDefaultRelicData() {
        return RelicData.builder()
                .abilityData(RelicAbilityData.builder()
                        .ability("teleport", RelicAbilityEntry.builder()
                                .stat("distance", RelicAbilityStat.builder()
                                        .initialValue(500D, 1000D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, 0.5D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .stat("cooldown", RelicAbilityStat.builder()
                                        .initialValue(60D, 120D)
                                        .upgradeModifier(RelicAbilityStat.Operation.MULTIPLY_BASE, -0.05D)
                                        .formatValue(value -> MathUtils.round(value, 1))
                                        .build())
                                .build())
                        .build())
                .levelingData(new RelicLevelingData(100, 10, 200))
                .styleData(RelicStyleData.builder()
                        .borders("#008cd7", "#0a3484")
                        .build())
                .build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (playerIn.getCooldowns().isOnCooldown(ItemRegistry.MAGIC_MIRROR.get())
                || DurabilityUtils.isBroken(stack) || worldIn.isClientSide())
            return InteractionResultHolder.fail(stack);

        ServerPlayer serverPlayer = (ServerPlayer) playerIn;

        Pair<ServerLevel, Vec3> data = getHomePos(serverPlayer, false);

        if (!canTeleport(serverPlayer, data, stack))
            return InteractionResultHolder.fail(stack);

        playerIn.startUsingItem(handIn);

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entity) {
        if (world.isClientSide() || !(entity instanceof ServerPlayer player))
            return stack;

        Pair<ServerLevel, Vec3> data = getHomePos(player, true);

        if (!canTeleport(player, data, stack))
            return stack;

        Vec3 pos = data.getRight();

        addExperience(player, stack, (int) (1 + (Math.round((player.position().distanceTo(new Vec3(pos.x(), player.getY(), pos.z()))
                * DimensionType.getTeleportationScale(player.getLevel().dimensionType(), data.getLeft().dimensionType()))) / 50)));

        player.teleportTo(data.getLeft(), pos.x() + 0.5F, pos.y() + 1.0F, pos.z() + 0.5F, player.getYRot(), player.getXRot());

        if (!player.isCreative())
            player.getCooldowns().addCooldown(stack.getItem(), (int) Math.round(getAbilityValue(stack, "teleport", "cooldown") * 20));

        world.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

        return stack;
    }


    @Override
    public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
        if (player.level.isClientSide())
            return;

        ServerLevel serverLevel = (ServerLevel) player.level;

        float radius = count * 0.075F;
        double extraY = player.getY() + 1.5F - Math.log((count + getUseDuration(stack) * 0.075F) * 0.1F);

        RandomSource random = serverLevel.getRandom();

        Color color = switch (serverLevel.dimension().location().getPath()) {
            case "overworld" -> new Color(75, 150, 255);
            case "the_nether" -> new Color(150, 0, 0);
            case "the_end" -> new Color(100, 0, 200);
            default -> new Color(50, 150, 0);
        };

        for (int i = 0; i < 5; i++) {
            float angle = (0.01F * (count * 3 + i * 125));

            double extraX = (double) (radius * Mth.sin((float) (Math.PI + angle))) + player.getX();
            double extraZ = (double) (radius * Mth.cos(angle)) + player.getZ();

            serverLevel.sendParticles(ParticleUtils.constructSimpleSpark(color, Math.max(0.2F, (getUseDuration(stack) - count) * 0.015F),
                    40, 0.92F), extraX, extraY, extraZ, 1, 0F, 0F, 0F, 0F);
        }

        serverLevel.sendParticles(ParticleUtils.constructSimpleSpark(color, (getUseDuration(stack) - count) * 0.005F, 10 + random.nextInt(50),
                        0.95F), entity.getX(), entity.getY() + entity.getBbHeight() * 0.5F, entity.getZ(),
                (int) ((getUseDuration(stack) - count) * 0.5F), 0.25F, entity.getBbHeight() * 0.4F, 0.25F, 0.025F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 40;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Nullable
    private Pair<ServerLevel, Vec3> getHomePos(ServerPlayer player, boolean useAnchor) {
        if (player.level.isClientSide())
            return null;

        BlockPos pos = player.getRespawnPosition();

        MinecraftServer server = player.getServer();

        if (server == null)
            return null;

        ServerLevel world = player.getServer().getLevel(player.getRespawnDimension());

        if (world == null || pos == null)
            return null;

        return Player.findRespawnPositionAndUseSpawnBlock(world, pos, player.getRespawnAngle(), true, !useAnchor)
                .map(vec3 -> Pair.of(world, vec3)).orElse(null);
    }

    private boolean canTeleport(ServerPlayer player, Pair<ServerLevel, Vec3> data, ItemStack stack) {
        if (data == null)
            return false;

        Vec3 pos = data.getRight();
        ServerLevel level = data.getLeft();

        return !(player.position().distanceTo(new Vec3(pos.x(), player.getY(), pos.z())) * DimensionType.getTeleportationScale(player.level().dimensionType(),
                level.dimensionType()) > getAbilityValue(stack, "teleport", "distance"));
    }

    @Mod.EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onFovUpdate(ComputeFovModifierEvent event) {
            Player player = event.getPlayer();
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (stack.getItem() != ItemRegistry.MAGIC_MIRROR.get()
                    || !player.isUsingItem())
                return;

            int time = player.getTicksUsingItem();

            if (time > 0)
                event.setNewFovModifier(event.getNewFovModifier() - time * 0.02F);
        }
    }

    @Mod.EventBusSubscriber
    public static class ServerEvents {
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            Item item = ItemRegistry.MAGIC_MIRROR.get();

            if (!(event.getEntity() instanceof Player player) || !player.isUsingItem()
                    || (player.getMainHandItem().getItem() != item && player.getOffhandItem().getItem() != item))
                return;

            player.stopUsingItem();

            player.getCooldowns().addCooldown(item, 20);
        }
    }
}