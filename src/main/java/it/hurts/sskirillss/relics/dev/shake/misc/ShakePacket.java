package it.hurts.sskirillss.relics.dev.shake.misc;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.dev.shake.Shake;
import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class ShakePacket implements CustomPacketPayload {
    private Shake shake;

    public static final CustomPacketPayload.Type<ShakePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shake"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShakePacket> STREAM_CODEC = StreamCodec.composite(
            Shake.STREAM_CODEC, ShakePacket::getShake,
            ShakePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> ShakeManager.add(ctx.player().level(), this.getShake()));
    }
}