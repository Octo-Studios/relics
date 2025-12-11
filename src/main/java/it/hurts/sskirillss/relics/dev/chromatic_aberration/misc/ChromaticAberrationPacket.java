package it.hurts.sskirillss.relics.dev.chromatic_aberration.misc;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberration;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberrationManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class ChromaticAberrationPacket implements CustomPacketPayload {
    private ChromaticAberration chromaticAberration;

    public static final CustomPacketPayload.Type<ChromaticAberrationPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "chromatic_aberration"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChromaticAberrationPacket> STREAM_CODEC = StreamCodec.composite(
            ChromaticAberration.STREAM_CODEC, ChromaticAberrationPacket::getChromaticAberration,
            ChromaticAberrationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> ChromaticAberrationManager.add(ctx.player().level(), this.getChromaticAberration()));
    }
}
