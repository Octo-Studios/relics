package it.hurts.sskirillss.relics.handlers;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.relic.GatherRelicTemplateCacheKeyEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.leveling.S2CSyncRankPoints;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;

@EventBusSubscriber
public class RankHandler {
    public static long getRankPoints(Player player, Item item) {
        return player.getData(RelicsAttachments.PLAYER_RANK_POINTS).getOrDefault(BuiltInRegistries.ITEM.getKey(item), 0L);
    }

    public static void addRankPoints(ServerPlayer player, Item item, long amount) {
        var points = getRankPoints(player, item);

        setRankPoints(player, item, points + Math.min(amount, Long.MAX_VALUE - points));
    }

    public static void setRankPoints(ServerPlayer player, Item item, long amount) {
        var balances = new HashMap<>(player.getData(RelicsAttachments.PLAYER_RANK_POINTS));
        var key = BuiltInRegistries.ITEM.getKey(item);

        if (amount > 0)
            balances.put(key, amount);
        else
            balances.remove(key);

        player.setData(RelicsAttachments.PLAYER_RANK_POINTS, balances);
        syncRankPoints(player);
    }

    public static int strikeRelic(ServerPlayer player, int container, int index, Item item, int rank, int session) {
        var slot = DescriptionUtils.getRemovableRelicSlot(player, container, index);

        if (slot == null)
            return 0;

        var stack = slot.getItem();

        if (!stack.is(item) || !(item instanceof IRelicItem relic) || relic.getRelicData(player, stack).getLevelingData().getRank() != rank)
            return 0;

        var now = player.level().getGameTime();
        var previous = player.getData(RelicsAttachments.DISMANTLE_PROGRESS);
        var hits = 1;

        if (previous.session() == session && previous.container() == container && previous.slot() == index
                && previous.rank() == rank && previous.count() == stack.getCount() && ItemStack.matches(previous.stack(), stack))
            hits += DismantleProgress.remainingHits(previous.hits(), now - previous.lastHit());

        if (hits < DismantleProgress.REQUIRED_HITS) {
            player.setData(RelicsAttachments.DISMANTLE_PROGRESS, new DismantleProgress(session, container, index, stack, stack.getCount(), rank, hits, now));

            return hits;
        }

        player.removeData(RelicsAttachments.DISMANTLE_PROGRESS);

        var removed = slot.safeTake(1, 1, player);

        if (removed.isEmpty())
            return 0;

        addRankPoints(player, removed.getItem(), relic.getRelicData(player, removed).getLevelingData().getDismantleRankPointReward());

        return hits;
    }

    private static void syncRankPoints(ServerPlayer player) {
        NetworkHandler.sendToClient(new S2CSyncRankPoints(player.getData(RelicsAttachments.PLAYER_RANK_POINTS)), player);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            syncRankPoints(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            syncRankPoints(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            syncRankPoints(player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onGatherRelicTemplateCacheKey(GatherRelicTemplateCacheKeyEvent event) {
        var entity = event.getBearer();
        var stack = event.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        event.add(Relics.MODID + ":rank", relic.getRelicData(entity, stack).getLevelingData().getRank());
    }
}
