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
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class C2SQuickStoreAstralSatchelSlot implements CustomPacketPayload {
    private final int slot;
    private final boolean secondary;

    public static final Type<C2SQuickStoreAstralSatchelSlot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "astral_satchel/quick_store_slot"));

    public static final StreamCodec<ByteBuf, C2SQuickStoreAstralSatchelSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SQuickStoreAstralSatchelSlot::getSlot,
            ByteBufCodecs.BOOL, C2SQuickStoreAstralSatchelSlot::isSecondary,
            C2SQuickStoreAstralSatchelSlot::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var menu = player.containerMenu;

            if (this.getSlot() < 0 || this.getSlot() >= menu.slots.size())
                return;

            AstralSatchelItem.quickClick(player, menu, menu.slots.get(this.getSlot()), this.isSecondary() ? ClickAction.SECONDARY : ClickAction.PRIMARY, true);
        });
    }
}
