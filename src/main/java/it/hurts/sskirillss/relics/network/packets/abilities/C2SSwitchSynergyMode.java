package it.hurts.sskirillss.relics.network.packets.abilities;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.relic.abilities.synergy.SynergyModeSwitchEvent;
import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
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
public class C2SSwitchSynergyMode implements CustomPacketPayload {
    private final RelicStackReference stackReference;
    private final String synergy;
    private final String mode;

    public static final Type<C2SSwitchSynergyMode> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "abilities/switch_synergy_mode"));

    public static final StreamCodec<ByteBuf, C2SSwitchSynergyMode> STREAM_CODEC = StreamCodec.composite(
            RelicStackReference.STREAM_CODEC, C2SSwitchSynergyMode::getStackReference,
            ByteBufCodecs.STRING_UTF8, C2SSwitchSynergyMode::getSynergy,
            ByteBufCodecs.STRING_UTF8, C2SSwitchSynergyMode::getMode,
            C2SSwitchSynergyMode::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            var stack = stackReference.getStack(player);

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            var data = relic.getRelicData(player, stack);

            if (!RelicContainer.contains(player, stack, data.getActiveContainers()))
                return;

            var synergyData = data.getAbilitiesData().getSynergyData(this.synergy);

            if (synergyData == null || !synergyData.isUnlocked() || !synergyData.getTemplate().getModes().contains(this.mode))
                return;

            var event = new SynergyModeSwitchEvent(player, stack, this.synergy, synergyData.getMode(), this.mode);

            NeoForge.EVENT_BUS.post(event);

            if (!event.isCanceled())
                synergyData.setMode(event.getToMode());
        });
    }

}
