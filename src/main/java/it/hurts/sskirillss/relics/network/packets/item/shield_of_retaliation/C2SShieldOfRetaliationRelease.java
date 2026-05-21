package it.hurts.sskirillss.relics.network.packets.item.shield_of_retaliation;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.relics.ShieldOfRetaliationItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class C2SShieldOfRetaliationRelease implements CustomPacketPayload {
    private final boolean released;

    public static final CustomPacketPayload.Type<C2SShieldOfRetaliationRelease> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shield_of_retaliation/release"));

    public static final StreamCodec<ByteBuf, C2SShieldOfRetaliationRelease> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, C2SShieldOfRetaliationRelease::isReleased,
            C2SShieldOfRetaliationRelease::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        if (!this.isReleased())
            return;

        ctx.enqueueWork(() -> {
            var inventory = ctx.player().getInventory();

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                var stack = inventory.getItem(i);

                if (stack.getItem() instanceof ShieldOfRetaliationItem)
                    ShieldOfRetaliationItem.setReleaseLocked(stack, false);
            }
        });
    }
}
