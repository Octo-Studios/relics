package it.hurts.sskirillss.relics.network.packets.abilities;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class C2SActivateAbility implements CustomPacketPayload {
    private final RelicStackReference stackReference;
    private final String ability;
    private final AbilityActivationStage stage;

    public static final Type<C2SActivateAbility> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "abilities/activate"));

    public static final StreamCodec<ByteBuf, C2SActivateAbility> STREAM_CODEC = StreamCodec.composite(
            RelicStackReference.STREAM_CODEC, C2SActivateAbility::getStackReference,
            ByteBufCodecs.STRING_UTF8, C2SActivateAbility::getAbility,
            ByteBufCodecs.idMapper(AbilityActivationStage.BY_ID, AbilityActivationStage::getId), C2SActivateAbility::getStage,
            C2SActivateAbility::new
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

            var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(this.ability);

            if (abilityData != null)
                abilityData.activate(player, this.stage);
        });
    }

}
