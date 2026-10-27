package it.hurts.sskirillss.relics.network.packets.item.astral_satchel;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.AstralSatchelItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

import java.util.UUID;

@Data
@AllArgsConstructor
public class C2SPushAstralSatchelItem implements CustomPacketPayload {
    private final UUID id;
    private final Vector3f direction;

    public static final Type<C2SPushAstralSatchelItem> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/push"));

    public static final StreamCodec<ByteBuf, C2SPushAstralSatchelItem> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, C2SPushAstralSatchelItem::getId,
            ByteBufCodecs.VECTOR3F, C2SPushAstralSatchelItem::getDirection,
            C2SPushAstralSatchelItem::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> AstralSatchelItem.push(ctx.player(), this.getId(), new Vec3(this.getDirection())));
    }
}
