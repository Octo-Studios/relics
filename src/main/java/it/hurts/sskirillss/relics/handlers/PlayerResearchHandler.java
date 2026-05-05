package it.hurts.sskirillss.relics.handlers;

import it.hurts.sskirillss.relics.api.relics.PlayerResearchComponent;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.research.S2CSyncPlayerResearch;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber
public class PlayerResearchHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            PlayerResearchHandler.sync(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            PlayerResearchHandler.sync(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            PlayerResearchHandler.sync(player);
    }

    public static void sync(ServerPlayer player) {
        var component = player.getData(RelicsAttachments.PLAYER_RESEARCH);

        NetworkHandler.sendToClient(new S2CSyncPlayerResearch(component.getResearch()), player);
    }

    public static void set(ServerPlayer player, PlayerResearchComponent component) {
        player.setData(RelicsAttachments.PLAYER_RESEARCH, component);

        PlayerResearchHandler.sync(player);
    }
}