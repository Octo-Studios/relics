package it.hurts.sskirillss.relics.compat.ftbteams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingApi;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingTeamProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class FTBTeamsTargetingProvider implements AbilityTargetingTeamProvider {
    public static void register() {
        AbilityTargetingApi.registerTeamProvider(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "ftb_teams"), new FTBTeamsTargetingProvider());
    }

    @Override
    public boolean areAllied(Entity source, Entity target) {
        var sourcePlayer = getPlayerOrOwnerId(source);
        var targetPlayer = getPlayerOrOwnerId(target);

        if (sourcePlayer.isEmpty() || targetPlayer.isEmpty())
            return false;

        var api = FTBTeamsAPI.api();

        return api.isManagerLoaded() && api.getManager().arePlayersInSameTeam(sourcePlayer.get(), targetPlayer.get());
    }

    @Override
    public boolean hasTeam(Entity entity) {
        var player = getPlayerOrOwnerId(entity);

        if (player.isEmpty())
            return false;

        var api = FTBTeamsAPI.api();

        return api.isManagerLoaded() && api.getManager().getTeamForPlayerID(player.get()).isPresent();
    }

    private static Optional<UUID> getPlayerOrOwnerId(Entity entity) {
        var ownerId = EntityAccess.getOwningPlayerId(entity);

        return ownerId.isPresent() ? ownerId : EntityAccess.getPlayerId(entity);
    }

    private static class EntityAccess {
        private static Optional<UUID> getPlayerId(Entity entity) {
            return entity instanceof Player ? Optional.of(entity.getUUID()) : Optional.empty();
        }

        private static Optional<UUID> getOwningPlayerId(Entity entity) {
            return entity instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null ? Optional.of(ownable.getOwnerUUID()) : Optional.empty();
        }
    }
}
