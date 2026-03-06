package it.hurts.sskirillss.relics.network.packets.leveling;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.IntFunction;

@Data
@AllArgsConstructor
public class PacketRelicTweak implements CustomPacketPayload {
    private final int container;
    private final int slot;
    private final Operation operation;
    private final boolean withShift;

    public PacketRelicTweak(int container, int slot, String ability, Operation operation) {
        this(container, slot, operation, false);
    }

    public static final Type<PacketRelicTweak> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "ability_tweak"));

    public static final StreamCodec<ByteBuf, PacketRelicTweak> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PacketRelicTweak::getContainer,
            ByteBufCodecs.INT, PacketRelicTweak::getSlot,
            ByteBufCodecs.idMapper(Operation.BY_ID, Operation::getId), PacketRelicTweak::getOperation,
            ByteBufCodecs.BOOL, PacketRelicTweak::isWithShift,
            PacketRelicTweak::new
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

            if (!switch (operation) {
                case RANKUP -> relic.getRelicData(player, stack).getLevelingData().rankup();
            }) return;

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

    @Getter
    @AllArgsConstructor
    public enum Operation {
        RANKUP(0);

        public static final IntFunction<Operation> BY_ID = ByIdMap.continuous(Operation::getId, Operation.values(), ByIdMap.OutOfBoundsStrategy.ZERO);

        private final int id;
    }
}