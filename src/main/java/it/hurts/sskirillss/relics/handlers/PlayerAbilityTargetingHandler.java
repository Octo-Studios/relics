package it.hurts.sskirillss.relics.handlers;

import it.hurts.sskirillss.relics.api.relics.PlayerAbilityTargetingComponent;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import it.hurts.sskirillss.relics.network.packets.targeting.S2CSyncPlayerAbilityTargeting;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber
public class PlayerAbilityTargetingHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            sync(player);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            sync(player);
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            sync(player);
    }

    @SubscribeEvent
    public static void onPlayerStartedTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer tracked && event.getEntity() instanceof ServerPlayer tracker)
            syncTo(tracked, tracker);
    }

    public static void sync(ServerPlayer player) {
        var component = player.getData(RelicsAttachments.PLAYER_ABILITY_TARGETING);
        var packet = new S2CSyncPlayerAbilityTargeting(player.getId(), component.getAbilities());

        NetworkHandler.sendToClient(packet, player);
        NetworkHandler.sendToClientsTrackingEntity(packet, player);
    }

    public static void syncTo(ServerPlayer player, ServerPlayer target) {
        var component = player.getData(RelicsAttachments.PLAYER_ABILITY_TARGETING);

        NetworkHandler.sendToClient(new S2CSyncPlayerAbilityTargeting(player.getId(), component.getAbilities()), target);
    }

    public static void set(ServerPlayer player, PlayerAbilityTargetingComponent component) {
        player.setData(RelicsAttachments.PLAYER_ABILITY_TARGETING, component);

        sync(player);
    }
}
