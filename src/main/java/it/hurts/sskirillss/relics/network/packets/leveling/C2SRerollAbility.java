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

public record C2SRerollAbility(int container, int slot, Item item, String ability, boolean wasMaxQuality, boolean repeat) implements CustomPacketPayload {
    public static final Type<C2SRerollAbility> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "reroll_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRerollAbility> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, C2SRerollAbility::container,
            ByteBufCodecs.VAR_INT, C2SRerollAbility::slot,
            ByteBufCodecs.registry(Registries.ITEM), C2SRerollAbility::item,
            ByteBufCodecs.STRING_UTF8, C2SRerollAbility::ability,
            ByteBufCodecs.BOOL, C2SRerollAbility::wasMaxQuality,
            ByteBufCodecs.BOOL, C2SRerollAbility::repeat,
            C2SRerollAbility::new
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

            var data = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability);

            if (data == null || data.isMaxQuality() != wasMaxQuality || !data.reroll())
                return;

            while (repeat && !wasMaxQuality && !data.isMaxQuality()) {
                if (!data.reroll())
                    break;
            }

            source.set(stack);
        });
    }
}
