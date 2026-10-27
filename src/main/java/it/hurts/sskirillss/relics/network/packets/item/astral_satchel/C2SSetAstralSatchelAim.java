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
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
public class C2SSetAstralSatchelAim implements CustomPacketPayload {
    private final Optional<UUID> id;

    public static final Type<C2SSetAstralSatchelAim> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/set_aim"));

    public static final StreamCodec<ByteBuf, C2SSetAstralSatchelAim> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), C2SSetAstralSatchelAim::getId,
            C2SSetAstralSatchelAim::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> AstralSatchelItem.setAimed(ctx.player(), this.getId().orElse(null)));
    }
}
