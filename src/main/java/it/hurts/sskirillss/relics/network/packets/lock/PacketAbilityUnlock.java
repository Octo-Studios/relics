package it.hurts.sskirillss.relics.network.packets.lock;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.utils.Reference;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class PacketAbilityUnlock implements CustomPacketPayload {
    private final int container;
    private final int slot;
    private final String ability;
    private final int unlocks;

    public static final CustomPacketPayload.Type<PacketAbilityUnlock> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "ability_unlock"));

    public static final StreamCodec<ByteBuf, PacketAbilityUnlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketAbilityUnlock::getContainer,
            ByteBufCodecs.INT, PacketAbilityUnlock::getSlot,
            ByteBufCodecs.STRING_UTF8, PacketAbilityUnlock::getAbility,
            ByteBufCodecs.INT, PacketAbilityUnlock::getUnlocks,
            PacketAbilityUnlock::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();

            if (player.containerMenu.containerId != container) {
                causeError(player);

                return;
            }

            ItemStack stack = DescriptionUtils.gatherRelicStack(player, slot);

            if (!(stack.getItem() instanceof IRelicItem relic)) {
                causeError(player);

                return;
            }

            relic.setLockUnlocks(stack, ability, unlocks);

            try {
                player.containerMenu.getSlot(slot).set(stack);
            } catch (Exception e) {
                e.printStackTrace();

                causeError(player);
            }
        });
    }

    private static void causeError(Player player) {
        player.displayClientMessage(Component.translatable("info.relics.researching.wrong_container").withStyle(ChatFormatting.RED), false);

        player.closeContainer();
    }
}