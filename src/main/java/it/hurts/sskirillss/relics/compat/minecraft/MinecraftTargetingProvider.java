package it.hurts.sskirillss.relics.compat.minecraft;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingApi;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingTeamProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MinecraftTargetingProvider implements AbilityTargetingTeamProvider {
    public static void register() {
        AbilityTargetingApi.registerTeamProvider(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "minecraft"), new MinecraftTargetingProvider());
    }

    @Override
    public boolean areAllied(Entity source, Entity target) {
        return source.isAlliedTo(target) || target.isAlliedTo(source);
    }

    @Override
    public boolean hasTeam(Entity entity) {
        return entity.getTeam() != null;
    }
}
