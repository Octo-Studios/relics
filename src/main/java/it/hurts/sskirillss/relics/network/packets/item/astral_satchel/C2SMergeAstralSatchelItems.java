package it.hurts.sskirillss.relics.network.packets.item.astral_satchel;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.AstralSatchelItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

@Data
@AllArgsConstructor
public class C2SMergeAstralSatchelItems implements CustomPacketPayload {
    private final UUID first;
    private final UUID second;

    public static final Type<C2SMergeAstralSatchelItems> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/merge"));

    public static final StreamCodec<ByteBuf, C2SMergeAstralSatchelItems> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, C2SMergeAstralSatchelItems::getFirst,
            UUIDUtil.STREAM_CODEC, C2SMergeAstralSatchelItems::getSecond,
            C2SMergeAstralSatchelItems::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> AstralSatchelItem.merge(ctx.player(), this.getFirst(), this.getSecond()));
    }
}
