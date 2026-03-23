package it.hurts.sskirillss.relics.network.packets.description.synergy;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.relic.abilities.synergy.SynergyModeSwitchEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.network.packets.description.IRelicValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class C2SChangeSynergyMode implements CustomPacketPayload, IRelicValidator {
    private final int container;
    private final int slot;
    private final String synergy;
    private final String mode;

    public static final Type<C2SChangeSynergyMode> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "description/synergy/change_mode"));

    public static final StreamCodec<ByteBuf, C2SChangeSynergyMode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SChangeSynergyMode::getContainer,
            ByteBufCodecs.INT, C2SChangeSynergyMode::getSlot,
            ByteBufCodecs.STRING_UTF8, C2SChangeSynergyMode::getSynergy,
            ByteBufCodecs.STRING_UTF8, C2SChangeSynergyMode::getMode,
            C2SChangeSynergyMode::new
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

            var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(this.getSynergy());

            var event = new SynergyModeSwitchEvent(player, stack, this.getSynergy(), synergyData.getMode(), this.getMode());

            NeoForge.EVENT_BUS.post(event);

            if (!event.isCanceled())
                synergyData.setMode(event.getToMode());

            try {
                player.containerMenu.getSlot(this.getSlot()).set(stack);
            } catch (Exception e) {
                e.printStackTrace();

                this.causeError(player);
            }
        });
    }
}
