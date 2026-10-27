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
public class C2SSetAstralSatchelActive implements CustomPacketPayload {
    private final boolean active;
    private final int slot;

    public static final Type<C2SSetAstralSatchelActive> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/set_active"));

    public static final StreamCodec<ByteBuf, C2SSetAstralSatchelActive> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, C2SSetAstralSatchelActive::isActive,
            ByteBufCodecs.VAR_INT, C2SSetAstralSatchelActive::getSlot,
            C2SSetAstralSatchelActive::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> AstralSatchelItem.setActive(ctx.player(), this.isActive(), this.getSlot()));
    }
}
