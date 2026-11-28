package it.hurts.sskirillss.relics.api.events.relic;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerResearchChangedEvent extends PlayerEvent {
    public PlayerResearchChangedEvent(ServerPlayer player) {
        super(player);
    }
}
