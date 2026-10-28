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
public class C2SChangeAbilityEnabled implements CustomPacketPayload, IRelicValidator {
    private final int container;
    private final int slot;
    private final String ability;
    private final boolean enabled;

    public static final Type<C2SChangeAbilityEnabled> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "description/ability/change_enabled"));

    public static final StreamCodec<ByteBuf, C2SChangeAbilityEnabled> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SChangeAbilityEnabled::getContainer,
            ByteBufCodecs.INT, C2SChangeAbilityEnabled::getSlot,
            ByteBufCodecs.STRING_UTF8, C2SChangeAbilityEnabled::getAbility,
            ByteBufCodecs.BOOL, C2SChangeAbilityEnabled::isEnabled,
            C2SChangeAbilityEnabled::new
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

            if (abilityData.getTemplate() == null) {
                this.causeError(player);

                return;
            }

            abilityData.setEnabled(this.isEnabled());

            try {
                player.containerMenu.getSlot(this.getSlot()).set(stack);
            } catch (Exception e) {
                e.printStackTrace();

                this.causeError(player);
            }
        });
    }
}
