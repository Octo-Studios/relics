package it.hurts.sskirillss.relics.dev.shake;

import it.hurts.sskirillss.relics.dev.shake.misc.ShakePacket;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShakeManager {
    public static final Map<UUID, Shake> SHAKES = new HashMap<>();

    public static void add(Level level, Shake shake) {
        if (level.isClientSide()) {
            SHAKES.put(shake.getUuid(), shake);

            return;
        }

        var vec = shake.getAnchor().getPosition(level);

        NetworkHandler.sendToClientsTrackingChunk(new ShakePacket(shake), (ServerLevel) level, new ChunkPos(new BlockPos((int) vec.x(), (int) vec.y(), (int) vec.z())));
    }

    public static void addForPlayer(Player player, Shake shake) {
        if (player.level().isClientSide()) {
            SHAKES.put(shake.getUuid(), shake);

            return;
        }

        NetworkHandler.sendToClient(new ShakePacket(shake), (ServerPlayer) player);
    }
}