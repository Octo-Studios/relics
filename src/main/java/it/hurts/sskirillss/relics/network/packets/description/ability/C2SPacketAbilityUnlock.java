package it.hurts.sskirillss.relics.network.packets.description.ability;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SPacketAbilityUnlock(int container, int slot, Item item, String ability) implements CustomPacketPayload {
    public static final Type<C2SPacketAbilityUnlock> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "ability_unlock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPacketAbilityUnlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SPacketAbilityUnlock::container,
            ByteBufCodecs.VAR_INT, C2SPacketAbilityUnlock::slot,
            ByteBufCodecs.registry(Registries.ITEM), C2SPacketAbilityUnlock::item,
            ByteBufCodecs.STRING_UTF8, C2SPacketAbilityUnlock::ability,
            C2SPacketAbilityUnlock::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = (ServerPlayer) ctx.player();
            var source = DescriptionUtils.getActionSlot(player, container, slot);

            if (source == null || !source.getItem().is(item) || !(item instanceof IRelicItem relic))
                return;

            var data = relic.getRelicData(player, source.getItem()).getAbilitiesData().getAbilityData(ability);

            if (data != null && data.mayUnlock())
                data.getResearchData().setLockOpen(true);
        });
    }
}
