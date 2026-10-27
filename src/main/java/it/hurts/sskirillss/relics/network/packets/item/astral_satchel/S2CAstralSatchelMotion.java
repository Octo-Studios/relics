package it.hurts.sskirillss.relics.network.packets.item.astral_satchel;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.astral.AstralMotion;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class S2CAstralSatchelMotion implements CustomPacketPayload {
    private final UUID id;
    private final List<Vec3> path;
    private final boolean thrown;

    public static final Type<S2CAstralSatchelMotion> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/motion"));

    private static final StreamCodec<ByteBuf, Vec3> POINT_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, vec -> vec.x,
            ByteBufCodecs.DOUBLE, vec -> vec.y,
            ByteBufCodecs.DOUBLE, vec -> vec.z,
            Vec3::new
    );

    public static final StreamCodec<ByteBuf, S2CAstralSatchelMotion> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, S2CAstralSatchelMotion::getId,
            POINT_CODEC.apply(ByteBufCodecs.list()), S2CAstralSatchelMotion::getPath,
            ByteBufCodecs.BOOL, S2CAstralSatchelMotion::isThrown,
            S2CAstralSatchelMotion::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> AstralMotion.onMotion(this.getId(), this.getPath(), this.isThrown()));
    }
}
