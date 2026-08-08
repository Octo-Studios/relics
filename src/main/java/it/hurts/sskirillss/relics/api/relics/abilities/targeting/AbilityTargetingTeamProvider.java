package it.hurts.sskirillss.relics.api.relics.abilities.targeting;

import net.minecraft.world.entity.Entity;

public interface AbilityTargetingTeamProvider {
    boolean areAllied(Entity source, Entity target);

    default boolean hasTeam(Entity entity) {
        return false;
    }
}
