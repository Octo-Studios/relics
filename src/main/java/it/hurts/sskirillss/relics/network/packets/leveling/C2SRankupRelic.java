package it.hurts.sskirillss.relics.network.packets.leveling;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SRankupRelic(int container, int slot, Item item, int rank, boolean repeat) implements CustomPacketPayload {
    public static final Type<C2SRankupRelic> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "rankup_relic"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRankupRelic> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SRankupRelic::container,
            ByteBufCodecs.VAR_INT, C2SRankupRelic::slot,
            ByteBufCodecs.registry(Registries.ITEM), C2SRankupRelic::item,
            ByteBufCodecs.VAR_INT, C2SRankupRelic::rank,
            ByteBufCodecs.BOOL, C2SRankupRelic::repeat,
            C2SRankupRelic::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var source = DescriptionUtils.getEditableRelicSlot(player, container, slot);

            if (source == null)
                return;

            var stack = source.getItem().copy();

            if (!stack.is(item) || !(item instanceof IRelicItem relic))
                return;

            var leveling = relic.getRelicData(player, stack).getLevelingData();

            if (leveling.getRank() != rank)
                return;

            do {
                if (!leveling.rankup())
                    break;
            } while (repeat);

            if (leveling.getRank() != rank)
                source.set(stack);
        });
    }
}
