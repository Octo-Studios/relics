package it.hurts.sskirillss.relics.network.packets;

import it.hurts.sskirillss.relics.Relics;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class PacketItemActivation implements CustomPacketPayload {
    private final ItemStack stack;

    public static final CustomPacketPayload.Type<PacketItemActivation> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "item_activation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketItemActivation> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, PacketItemActivation::getStack,
            PacketItemActivation::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> Minecraft.getInstance().gameRenderer.displayItemActivation(stack));
    }
}