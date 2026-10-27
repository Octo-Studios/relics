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
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class C2SQuickStoreAstralSatchelDrop implements CustomPacketPayload {
    private final int entity;
    private final boolean offhand;

    public static final Type<C2SQuickStoreAstralSatchelDrop> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/quick_store_drop"));

    public static final StreamCodec<ByteBuf, C2SQuickStoreAstralSatchelDrop> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SQuickStoreAstralSatchelDrop::getEntity,
            ByteBufCodecs.BOOL, C2SQuickStoreAstralSatchelDrop::isOffhand,
            C2SQuickStoreAstralSatchelDrop::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> AstralSatchelItem.quickStoreDrop(ctx.player(), this.getEntity(), this.isOffhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
    }
}
