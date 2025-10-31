package it.hurts.sskirillss.relics.network.packets.item.cut_glass_boot;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.relics.feet.CutGlassBootItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Arrays;

@Data
@AllArgsConstructor
public class C2SCycleFluid implements CustomPacketPayload {
    private final int delta;

    public static final Type<C2SCycleFluid> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "cut_glass_boot/cycle_fluid"));

    public static final StreamCodec<ByteBuf, C2SCycleFluid> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SCycleFluid::getDelta,
            C2SCycleFluid::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();

            for (var stack : Arrays.asList(player.getMainHandItem(), player.getOffhandItem())) {
                if (!(stack.getItem() instanceof CutGlassBootItem item))
                    continue;

                var entries = item.getFluidEntries(player, stack);

                if (entries.isEmpty())
                    continue;

                item.setSelectedFluidIndex(player, stack, Math.floorMod(item.getSelectedFluidIndex(player, stack) + this.getDelta(), entries.size()));

                var fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(item.getSelectedFluid(player, stack).toString()));

                if (fluid == Fluids.EMPTY)
                    continue;

                player.displayClientMessage(Component.literal(fluid.getFluidType().getDescription().getString()), true);
            }
        });
    }
}