package it.hurts.sskirillss.relics.network.packets.description.ability;

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
public class C2SChangeAbilityRankModifier implements CustomPacketPayload, IRelicValidator {
    private final int container;
    private final int slot;
    private final String ability;
    private final String rankModifier;
    private final boolean enabled;

    public static final Type<C2SChangeAbilityRankModifier> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "description/ability/change_rank_modifier"));

    public static final StreamCodec<ByteBuf, C2SChangeAbilityRankModifier> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SChangeAbilityRankModifier::getContainer,
            ByteBufCodecs.INT, C2SChangeAbilityRankModifier::getSlot,
            ByteBufCodecs.STRING_UTF8, C2SChangeAbilityRankModifier::getAbility,
            ByteBufCodecs.STRING_UTF8, C2SChangeAbilityRankModifier::getRankModifier,
            ByteBufCodecs.BOOL, C2SChangeAbilityRankModifier::isEnabled,
            C2SChangeAbilityRankModifier::new
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

            var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(this.getAbility());

            if (abilityData.getTemplate() == null || !abilityData.getTemplate().getRankModifiers().containsValue(this.getRankModifier())) {
                this.causeError(player);

                return;
            }

            abilityData.getRankModifierData(this.getRankModifier()).setEnabled(this.isEnabled());

            try {
                player.containerMenu.getSlot(this.getSlot()).set(stack);
            } catch (Exception e) {
                e.printStackTrace();

                this.causeError(player);
            }
        });
    }
}
