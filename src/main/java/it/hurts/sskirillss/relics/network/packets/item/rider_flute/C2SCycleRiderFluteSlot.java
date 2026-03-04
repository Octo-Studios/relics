package it.hurts.sskirillss.relics.network.packets.item.rider_flute;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.relics.RiderFluteItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Arrays;

@Data
@AllArgsConstructor
public class C2SCycleRiderFluteSlot implements CustomPacketPayload {
    private final int delta;

    public static final Type<C2SCycleRiderFluteSlot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "rider_flute/cycle_slot"));

    public static final StreamCodec<ByteBuf, C2SCycleRiderFluteSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SCycleRiderFluteSlot::getDelta,
            C2SCycleRiderFluteSlot::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();

            for (var stack : Arrays.asList(player.getMainHandItem(), player.getOffhandItem())) {
                if (!(stack.getItem() instanceof RiderFluteItem item))
                    continue;

                if (!item.cycleSelectedHorseSlot(stack, this.getDelta()))
                    continue;

                item.notifySelectedHorseSlot(player, stack);

                break;
            }
        });
    }
}
