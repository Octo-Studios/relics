package it.hurts.sskirillss.relics.network.packets.leveling;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CDismantleResult(int requestId, boolean dismantled) implements CustomPacketPayload {
    public static final Type<S2CDismantleResult> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "dismantle_result"));
    public static final StreamCodec<ByteBuf, S2CDismantleResult> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, S2CDismantleResult::requestId,
            ByteBufCodecs.BOOL, S2CDismantleResult::dismantled,
            S2CDismantleResult::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof RelicDescriptionScreen screen)
                screen.getDismantleEffect().confirm(requestId, dismantled);
        });
    }
}
