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
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class C2SWithdrawAstralSatchelItem implements CustomPacketPayload {
    private final int index;

    public static final Type<C2SWithdrawAstralSatchelItem> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/withdraw"));

    public static final StreamCodec<ByteBuf, C2SWithdrawAstralSatchelItem> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SWithdrawAstralSatchelItem::getIndex,
            C2SWithdrawAstralSatchelItem::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> AstralSatchelItem.withdraw(ctx.player(), this.getIndex()));
    }
}
