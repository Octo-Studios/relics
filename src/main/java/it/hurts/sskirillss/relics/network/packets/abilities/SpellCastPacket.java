package it.hurts.sskirillss.relics.network.packets.abilities;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastStage;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.system.casts.abilities.AbilityReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Data
@AllArgsConstructor
public class SpellCastPacket implements CustomPacketPayload {
    private final CastType type;
    private final CastStage stage;
    private final CompoundTag ability;

    public static final CustomPacketPayload.Type<SpellCastPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "spell_cast"));

    public static final StreamCodec<ByteBuf, SpellCastPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(CastType.BY_ID, CastType::getId), SpellCastPacket::getType,
            ByteBufCodecs.idMapper(CastStage.BY_ID, CastStage::getId), SpellCastPacket::getStage,
            ByteBufCodecs.COMPOUND_TAG, SpellCastPacket::getAbility,
            SpellCastPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            AbilityReference reference = new AbilityReference().deserializeNBT(ability);
            ItemStack stack = reference.getSlot().gatherStack(player);

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(reference.getId());

            if (!abilityData.canPlayerUse(player)) {
                if (abilityData.isTicking()) {
                    abilityData.getExtenderData().setTicking(false);

                    relic.castActiveAbility(player, stack, reference.getId(), type, CastStage.END);
                }

                return;
            }

            switch (type) {
                case CYCLICAL, TOGGLEABLE -> {
                    switch (stage) {
                        case START -> abilityData.getExtenderData().setTicking(true);
                        case END -> abilityData.getExtenderData().setTicking(false);
                    }
                }
            }

            relic.castActiveAbility(player, stack, reference.getId(), type, stage);
        });
    }
}
