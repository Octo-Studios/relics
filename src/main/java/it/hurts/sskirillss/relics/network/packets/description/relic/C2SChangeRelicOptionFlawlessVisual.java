package it.hurts.sskirillss.relics.network.packets.description.relic;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.network.packets.description.IRelicValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class C2SChangeRelicOptionFlawlessVisual implements CustomPacketPayload, IRelicValidator {
    private final int container;
    private final int slot;
    private final boolean enabled;

    public static final Type<C2SChangeRelicOptionFlawlessVisual> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "description/relic/change_option_flawless_visual"));

    public static final StreamCodec<ByteBuf, C2SChangeRelicOptionFlawlessVisual> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SChangeRelicOptionFlawlessVisual::getContainer,
            ByteBufCodecs.INT, C2SChangeRelicOptionFlawlessVisual::getSlot,
            ByteBufCodecs.BOOL, C2SChangeRelicOptionFlawlessVisual::isEnabled,
            C2SChangeRelicOptionFlawlessVisual::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();

            if (player.containerMenu.containerId != this.getContainer()) {
                this.causeError(player);

                return;
            }

            var stack = DescriptionUtils.gatherRelicStack(player, this.getSlot());

            if (!(stack.getItem() instanceof IRelicItem relic)) {
                this.causeError(player);

                return;
            }

            relic.getRelicData(player, stack).getOptionsData().setVisuallyFlawless(this.isEnabled());

            try {
                player.containerMenu.getSlot(this.getSlot()).set(stack);
            } catch (Exception e) {
                e.printStackTrace();

                this.causeError(player);
            }
        });
    }
}
