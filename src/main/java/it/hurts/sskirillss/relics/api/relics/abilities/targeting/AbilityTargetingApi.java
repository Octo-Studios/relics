package it.hurts.sskirillss.relics.api.relics.abilities.targeting;

import it.hurts.sskirillss.relics.Relics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class AbilityTargetingApi {
    private static final Map<ResourceLocation, AbilityTargetingTeamProvider> TEAM_PROVIDERS = new LinkedHashMap<>();

    public static void registerTeamProvider(ResourceLocation id, AbilityTargetingTeamProvider provider) {
        TEAM_PROVIDERS.put(id, provider);
    }

    public static boolean areAllied(Entity source, Entity target) {
        for (var entry : TEAM_PROVIDERS.entrySet()) {
            try {
                if (entry.getValue().areAllied(source, target))
                    return true;
            } catch (RuntimeException exception) {
                Relics.LOGGER.error("Ability targeting team provider {} failed", entry.getKey(), exception);
            }
        }

        return false;
    }

    public static boolean hasTeam(Entity entity) {
        for (var entry : TEAM_PROVIDERS.entrySet()) {
            try {
                if (entry.getValue().hasTeam(entity))
                    return true;
            } catch (RuntimeException exception) {
                Relics.LOGGER.error("Ability targeting team provider {} failed", entry.getKey(), exception);
            }
        }

        return false;
    }

    public static boolean unregisterTeamProvider(ResourceLocation id) {
        return TEAM_PROVIDERS.remove(id) != null;
    }

    public static Map<ResourceLocation, AbilityTargetingTeamProvider> getTeamProviders() {
        return Map.copyOf(TEAM_PROVIDERS);
    }
}
