package it.hurts.sskirillss.relics.network.packets.item.springy_boot;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.items.relics.belt.KineticBeltItem;
import it.hurts.sskirillss.relics.utils.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

@Data
@AllArgsConstructor
public class C2SBounceFromSurface implements CustomPacketPayload {
    private final String identifier;
    private final int index;
    private final boolean active;

    public static final Type<C2SBounceFromSurface> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "springy_boot/bounce_from_surface"));

    public static final StreamCodec<ByteBuf, C2SBounceFromSurface> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SBounceFromSurface::getIdentifier,
            ByteBufCodecs.INT, C2SBounceFromSurface::getIndex,
            ByteBufCodecs.BOOL, C2SBounceFromSurface::isActive,
            C2SBounceFromSurface::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();

            CuriosApi.getCuriosInventory(player).flatMap(inventory -> inventory.findCurio(this.getIdentifier(), this.getIndex())).ifPresent(slot -> {
                var stack = slot.stack();

                if (!(stack.getItem() instanceof KineticBeltItem relic))
                    return;

                relic.setActive(stack, active);
            });
        });
    }

}