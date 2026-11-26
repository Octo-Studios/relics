package it.hurts.sskirillss.relics.network.packets.leveling;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsAttachments;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record S2CSyncRankPoints(Map<ResourceLocation, Long> points) implements CustomPacketPayload {
    public static final Type<S2CSyncRankPoints> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "sync_rank_points"));
    public static final StreamCodec<ByteBuf, S2CSyncRankPoints> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_LONG), S2CSyncRankPoints::points,
            S2CSyncRankPoints::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> ctx.player().setData(RelicsAttachments.PLAYER_RANK_POINTS, points));
    }
}
