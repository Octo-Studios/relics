package it.hurts.sskirillss.relics.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.astral.AstralSpace;
import it.hurts.sskirillss.relics.init.RelicsDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

public class AstralSatchelItem extends ItemBase {
    public static final String ACTIVE_TAG = "relics:astral_satchel_active";
    public static final String CAMERA_TAG = "relics:astral_satchel_camera";

    public static final int MAX_ENTRIES = 64;
    public static final double RADIUS = 14D;

    public AstralSatchelItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    public static ItemStack find(Player player) {
        var inventory = player.getInventory();

        for (var i = 0; i < inventory.getContainerSize(); i++) {
            var stack = inventory.getItem(i);

            if (stack.getItem() instanceof AstralSatchelItem)
                return stack;
        }

        return ItemStack.EMPTY;
    }

    public static boolean isActive(Player player) {
        return player.getPersistentData().getBoolean(ACTIVE_TAG);
    }

    public static void setActive(Player player, boolean active) {
        if (!active) {
            player.getPersistentData().remove(ACTIVE_TAG);

            return;
        }

        if (find(player).isEmpty())
            return;

        setCamera(player, Vec3.ZERO);

        player.getPersistentData().putBoolean(ACTIVE_TAG, true);
    }

    public static Vec3 getCamera(Player player) {
        var data = player.getPersistentData();

        return new Vec3(data.getDouble(CAMERA_TAG + "_x"), data.getDouble(CAMERA_TAG + "_y"), data.getDouble(CAMERA_TAG + "_z"));
    }

    public static void setCamera(Player player, Vec3 camera) {
        var data = player.getPersistentData();

        data.putDouble(CAMERA_TAG + "_x", camera.x);
        data.putDouble(CAMERA_TAG + "_y", camera.y);
        data.putDouble(CAMERA_TAG + "_z", camera.z);
    }

    public static void syncCamera(Player player, Vec3 camera) {
        if (!isActive(player) || !Double.isFinite(camera.x) || !Double.isFinite(camera.y) || !Double.isFinite(camera.z))
            return;

        setCamera(player, clamp(camera));
    }

    public static List<Entry> getEntries(ItemStack stack) {
        return stack.getOrDefault(RelicsDataComponents.ASTRAL_SATCHEL_ENTRIES.get(), List.of());
    }

    public static void setEntries(ItemStack stack, List<Entry> entries) {
        stack.set(RelicsDataComponents.ASTRAL_SATCHEL_ENTRIES.get(), List.copyOf(entries));
    }

    public static Vec3 clamp(Vec3 position) {
        return position.length() > RADIUS - 1D ? position.normalize().scale(RADIUS - 1D) : position;
    }

    public static boolean deposit(Player player, ItemStack stack) {
        var satchel = find(player);

        if (satchel.isEmpty() || stack.isEmpty() || stack.getItem() instanceof AstralSatchelItem)
            return false;

        var entries = new ArrayList<>(getEntries(satchel));
        var moved = stack.copy();

        for (var i = 0; i < entries.size() && !moved.isEmpty(); i++) {
            var entry = entries.get(i);
            var existing = entry.stack();

            if (!ItemStack.isSameItemSameComponents(existing, moved) || existing.getCount() >= existing.getMaxStackSize())
                continue;

            var transfer = Math.min(moved.getCount(), existing.getMaxStackSize() - existing.getCount());

            entries.set(i, new Entry(existing.copyWithCount(existing.getCount() + transfer), entry.position()));

            moved.shrink(transfer);
        }

        if (!moved.isEmpty()) {
            if (entries.size() >= MAX_ENTRIES)
                return false;

            entries.add(new Entry(moved, clamp(getCamera(player).add(player.getLookAngle().scale(3D)))));
        }

        setEntries(satchel, entries);

        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.4F);

        return true;
    }

    public static void withdraw(Player player, int index) {
        var satchel = find(player);

        if (!isActive(player) || satchel.isEmpty())
            return;

        var entries = new ArrayList<>(getEntries(satchel));

        if (index < 0 || index >= entries.size())
            return;

        var entry = entries.get(index);
        var stack = entry.stack().copy();

        player.getInventory().add(stack);

        if (stack.isEmpty())
            entries.remove(index);
        else if (stack.getCount() == entry.stack().getCount())
            return;
        else
            entries.set(index, new Entry(stack, entry.position()));

        setEntries(satchel, entries);

        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 0.8F);
    }

    public record Entry(ItemStack stack, Vec3 position) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.fieldOf("stack").forGetter(Entry::stack),
                Vec3.CODEC.fieldOf("position").forGetter(Entry::position)
        ).apply(instance, Entry::new));
    }

    @EventBusSubscriber(modid = Relics.MODID)
    public static class CommonEvents {
        @SubscribeEvent
        public static void onItemToss(ItemTossEvent event) {
            var player = event.getPlayer();

            if (!(player instanceof ServerPlayer) || !isActive(player))
                return;

            var stack = event.getEntity().getItem();

            event.setCanceled(true);

            if (deposit(player, stack))
                return;

            player.getInventory().placeItemBackInInventory(stack.copy());

            if (!(stack.getItem() instanceof AstralSatchelItem))
                player.displayClientMessage(Component.translatable("relics.astral_satchel.full"), true);
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            var player = event.getEntity();

            if (!(player instanceof ServerPlayer) || player.tickCount % 20 != 0 || !isActive(player))
                return;

            if (find(player).isEmpty())
                setActive(player, false);
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            setActive(event.getEntity(), false);
        }

        @SubscribeEvent
        public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            setActive(event.getEntity(), false);
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            setActive(event.getEntity(), false);
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            setActive(event.getEntity(), false);
        }
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            if (!event.getLevel().isClientSide() || !(event.getItemStack().getItem() instanceof AstralSatchelItem))
                return;

            AstralSpace.enter();
        }
    }
}
