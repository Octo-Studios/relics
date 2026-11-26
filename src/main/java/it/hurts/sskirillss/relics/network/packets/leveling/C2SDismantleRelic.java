package it.hurts.sskirillss.relics.network.packets.leveling;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.handlers.RankHandler;
import it.hurts.sskirillss.relics.network.NetworkHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SDismantleRelic(int container, int slot, Item item, int rank, int session) implements CustomPacketPayload {
    public static final Type<C2SDismantleRelic> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "dismantle_relic"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SDismantleRelic> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SDismantleRelic::container,
            ByteBufCodecs.VAR_INT, C2SDismantleRelic::slot,
            ByteBufCodecs.registry(Registries.ITEM), C2SDismantleRelic::item,
            ByteBufCodecs.VAR_INT, C2SDismantleRelic::rank,
            ByteBufCodecs.VAR_INT, C2SDismantleRelic::session,
            C2SDismantleRelic::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = (ServerPlayer) ctx.player();
            var hits = RankHandler.strikeRelic(player, container, slot, item, rank, session);

            NetworkHandler.sendToClient(new S2CDismantleStrike(session, hits), player);
        });
    }
}
