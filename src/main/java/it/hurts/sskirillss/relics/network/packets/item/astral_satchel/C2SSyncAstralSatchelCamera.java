package it.hurts.sskirillss.relics.network.packets.item.astral_satchel;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.AstralSatchelItem;
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
public class C2SSyncAstralSatchelCamera implements CustomPacketPayload {
    private final Vector3f camera;

    public static final Type<C2SSyncAstralSatchelCamera> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/sync_camera"));

    public static final StreamCodec<ByteBuf, C2SSyncAstralSatchelCamera> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, C2SSyncAstralSatchelCamera::getCamera,
            C2SSyncAstralSatchelCamera::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> AstralSatchelItem.syncCamera(ctx.player(), new Vec3(this.getCamera())));
    }
}
