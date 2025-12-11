package it.hurts.sskirillss.relics.dev.chromatic_aberration;

import it.hurts.sskirillss.relics.dev.chromatic_aberration.misc.ChromaticAberrationPacket;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChromaticAberrationManager {
    public static final Map<UUID, ChromaticAberration> CHROMATIC_ABERRATIONS = new HashMap<>();

    public static void add(Level level, ChromaticAberration chromaticAberration) {
        if (level.isClientSide()) {
            CHROMATIC_ABERRATIONS.put(chromaticAberration.getUuid(), chromaticAberration);

            return;
        }

        var vec = chromaticAberration.getAnchor().getPosition(level);

        NetworkHandler.sendToClientsTrackingChunk(new ChromaticAberrationPacket(chromaticAberration), (ServerLevel) level, new ChunkPos(new BlockPos((int) vec.x(), (int) vec.y(), (int) vec.z())));
    }

    public static void addForPlayer(Player player, ChromaticAberration chromaticAberration) {
        if (player.level().isClientSide()) {
            CHROMATIC_ABERRATIONS.put(chromaticAberration.getUuid(), chromaticAberration);

            return;
        }

        NetworkHandler.sendToClient(new ChromaticAberrationPacket(chromaticAberration), (ServerPlayer) player);
    }
}