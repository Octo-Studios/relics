package it.hurts.sskirillss.relics.network.packets.sync;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.entities.misc.ITargetableEntity;
import it.hurts.sskirillss.relics.utils.Reference;
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
public class S2CEntityTargetPacket implements CustomPacketPayload {
    private final int sourceId;
    private final int targetId;

    public static final CustomPacketPayload.Type<S2CEntityTargetPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "entity_target"));

    public static final StreamCodec<ByteBuf, S2CEntityTargetPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CEntityTargetPacket::getTargetId,
            ByteBufCodecs.INT, S2CEntityTargetPacket::getSourceId,
            S2CEntityTargetPacket::new
    );

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var level = ctx.player().getCommandSenderWorld();

            if (level.getEntity(sourceId) instanceof ITargetableEntity source && level.getEntity(targetId) instanceof LivingEntity target)
                source.setTargetPos(target);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}