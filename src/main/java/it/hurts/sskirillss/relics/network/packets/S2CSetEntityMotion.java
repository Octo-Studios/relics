package it.hurts.sskirillss.relics.network.packets;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

@Data
@AllArgsConstructor
public class S2CSetEntityMotion implements CustomPacketPayload {
    private final int id;
    private final Vector3f motion;

    public static final CustomPacketPayload.Type<S2CSetEntityMotion> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "entity_motion"));

    public static final StreamCodec<ByteBuf, S2CSetEntityMotion> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CSetEntityMotion::getId,
            ByteBufCodecs.VECTOR3F, S2CSetEntityMotion::getMotion,
            S2CSetEntityMotion::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var entity = ctx.player().level().getEntity(this.getId());

            if (entity != null)
                entity.setDeltaMovement(new Vec3(this.getMotion()));
        });
    }
}