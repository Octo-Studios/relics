package it.hurts.sskirillss.relics.network.packets.capability;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class CapabilitySyncPacket implements CustomPacketPayload {
    private final CompoundTag data;

    public static final CustomPacketPayload.Type<CapabilitySyncPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "capability_sync"));

    public static final StreamCodec<ByteBuf, CapabilitySyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, CapabilitySyncPacket::getData,
            CapabilitySyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public boolean handle(IPayloadContext ctx) {
        ctx.enqueueWork(this::doSync);

        return true;
    }

    @OnlyIn(Dist.CLIENT)
    private void doSync() {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null)
            return;

        // TODO: CapabilityUtils.getRelicsCapability(player).deserializeNBT(data);
    }
}