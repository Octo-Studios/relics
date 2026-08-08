package it.hurts.sskirillss.relics.network.packets.targeting;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.AbilityTargetingOption;
import it.hurts.sskirillss.relics.api.relics.abilities.targeting.SelectorType;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.network.packets.description.IRelicValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Locale;

@Data
@AllArgsConstructor
public class C2SChangeAbilityTargetingOption implements CustomPacketPayload, IRelicValidator {
    private final int container;
    private final int slot;
    private final String target;
    private final boolean synergy;
    private final String selector;
    private final String option;
    private final boolean value;

    public static final Type<C2SChangeAbilityTargetingOption> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "description/ability/change_targeting_option"));

    public static final StreamCodec<ByteBuf, C2SChangeAbilityTargetingOption> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeInt(packet.getContainer());
                buffer.writeInt(packet.getSlot());
                writeUtf(buffer, packet.getTarget());
                buffer.writeBoolean(packet.isSynergy());
                writeUtf(buffer, packet.getSelector());
                writeUtf(buffer, packet.getOption());
                buffer.writeBoolean(packet.isValue());
            },
            buffer -> new C2SChangeAbilityTargetingOption(
                    buffer.readInt(),
                    buffer.readInt(),
                    readUtf(buffer),
                    buffer.readBoolean(),
                    readUtf(buffer),
                    readUtf(buffer),
                    buffer.readBoolean()
            )
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

            var abilitiesData = relic.getRelicData(player, stack).getAbilitiesData();
            AbilityTargetingTemplate targeting = null;

            if (this.isSynergy()) {
                var template = abilitiesData.getSynergyData(this.getTarget()).getTemplate();

                targeting = template == null ? null : template.getTargeting();
            } else {
                var template = abilitiesData.getAbilityData(this.getTarget()).getTemplate();

                targeting = template == null ? null : template.getTargeting();
            }

            if (targeting == null || !targeting.isActive()) {
                this.causeError(player);

                return;
            }

            AbilityTargetingOption option;
            SelectorType selectorType;

            try {
                selectorType = SelectorType.valueOf(this.getSelector().toUpperCase(Locale.ROOT));
                option = AbilityTargetingOption.valueOf(this.getOption().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                this.causeError(player);

                return;
            }

            if (!targeting.hasOption(selectorType, option)) {
                this.causeError(player);

                return;
            }

            if (this.isSynergy())
                abilitiesData.getSynergyData(this.getTarget()).getTargetingData().setOption(option, selectorType, this.isValue());
            else
                abilitiesData.getAbilityData(this.getTarget()).getTargetingData().setOption(option, selectorType, this.isValue());
        });
    }

    private static void writeUtf(ByteBuf buffer, String value) {
        var bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    private static String readUtf(ByteBuf buffer) {
        var bytes = new byte[buffer.readInt()];

        buffer.readBytes(bytes);

        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }
}
