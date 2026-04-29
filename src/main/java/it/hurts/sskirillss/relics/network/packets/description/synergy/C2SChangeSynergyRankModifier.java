package it.hurts.sskirillss.relics.network.packets.description.synergy;

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
public class C2SChangeSynergyRankModifier implements CustomPacketPayload, IRelicValidator {
    private final int container;
    private final int slot;
    private final String synergy;
    private final String rankModifier;
    private final boolean enabled;

    public static final Type<C2SChangeSynergyRankModifier> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "description/synergy/change_rank_modifier"));

    public static final StreamCodec<ByteBuf, C2SChangeSynergyRankModifier> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SChangeSynergyRankModifier::getContainer,
            ByteBufCodecs.INT, C2SChangeSynergyRankModifier::getSlot,
            ByteBufCodecs.STRING_UTF8, C2SChangeSynergyRankModifier::getSynergy,
            ByteBufCodecs.STRING_UTF8, C2SChangeSynergyRankModifier::getRankModifier,
            ByteBufCodecs.BOOL, C2SChangeSynergyRankModifier::isEnabled,
            C2SChangeSynergyRankModifier::new
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

            if (synergyData.getTemplate() == null || !synergyData.getTemplate().getRankModifiers().containsValue(this.getRankModifier())) {
                this.causeError(player);

                return;
            }

            synergyData.getRankModifierData(this.getRankModifier()).setEnabled(this.isEnabled());

            try {
                player.containerMenu.getSlot(this.getSlot()).set(stack);
            } catch (Exception e) {
                e.printStackTrace();

                this.causeError(player);
            }
        });
    }
}
