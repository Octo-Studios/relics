package it.hurts.sskirillss.relics.network.packets.description.ability;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.network.packets.description.IRelicValidator;
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
public class C2SPacketAbilityUnlock implements CustomPacketPayload, IRelicValidator {
    private final int container;
    private final int slot;
    private final String ability;
    private final int unlocks;

    public static final CustomPacketPayload.Type<C2SPacketAbilityUnlock> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "ability_unlock"));

    public static final StreamCodec<ByteBuf, C2SPacketAbilityUnlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SPacketAbilityUnlock::getContainer,
            ByteBufCodecs.INT, C2SPacketAbilityUnlock::getSlot,
            ByteBufCodecs.STRING_UTF8, C2SPacketAbilityUnlock::getAbility,
            ByteBufCodecs.INT, C2SPacketAbilityUnlock::getUnlocks,
            C2SPacketAbilityUnlock::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();

            if (player.containerMenu.containerId != container) {
                this.causeError(player);

                return;
            }

            var stack = DescriptionUtils.gatherRelicStack(player, slot);

            if (!(stack.getItem() instanceof IRelicItem relic)) {
                this.causeError(player);

                return;
            }

            relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(ability).getLockData().setUnlocks(unlocks);

            try {
                player.containerMenu.getSlot(slot).set(stack);
            } catch (Exception e) {
                e.printStackTrace();

                this.causeError(player);
            }
        });
    }
}