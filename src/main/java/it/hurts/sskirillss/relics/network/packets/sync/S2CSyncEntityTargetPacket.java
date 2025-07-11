package it.hurts.sskirillss.relics.network.packets.sync;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class S2CSyncEntityTargetPacket implements CustomPacketPayload {
    private final int sourceId;
    private final int targetId;

    public static final CustomPacketPayload.Type<S2CSyncEntityTargetPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "entity_target"));

    public static final StreamCodec<ByteBuf, S2CSyncEntityTargetPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CSyncEntityTargetPacket::getTargetId,
            ByteBufCodecs.INT, S2CSyncEntityTargetPacket::getSourceId,
            S2CSyncEntityTargetPacket::new
    );

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var level = ctx.player().getCommandSenderWorld();

            if (level.getEntity(this.sourceId) instanceof ITargetableEntity source && level.getEntity(this.targetId) instanceof LivingEntity target)
                source.setTarget(target);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}