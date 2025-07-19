package it.hurts.sskirillss.relics.network.packets.item.midnight_mantle;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.entities.relic.midnight_mantle.ConstellationStarEntity;
import it.hurts.sskirillss.relics.utils.MathUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class S2CSyncConstellation implements CustomPacketPayload {
    private final int id;
    private final List<Integer> constellation;

    public static final Type<S2CSyncConstellation> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "midnight_mantle/sync_constellation"));

    public static final StreamCodec<ByteBuf, S2CSyncConstellation> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CSyncConstellation::getId,
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.INT), S2CSyncConstellation::getConstellation,
            S2CSyncConstellation::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var level = player.level();

            if (!(level.getEntity(this.getId()) instanceof ConstellationStarEntity entity))
                return;

            entity.setClientConstellation(constellation);
        });
    }

}