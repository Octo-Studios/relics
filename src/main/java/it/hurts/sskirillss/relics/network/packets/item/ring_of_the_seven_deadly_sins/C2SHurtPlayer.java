package it.hurts.sskirillss.relics.network.packets.item.ring_of_the_seven_deadly_sins;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.relics.belt.KineticBeltItem;
import it.hurts.sskirillss.relics.items.relics.ring.RingOfTheSevenDeadlySinsItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

@Data
@AllArgsConstructor
public class C2SHurtPlayer implements CustomPacketPayload {
    private final String identifier;
    private final int index;

    public static final Type<C2SHurtPlayer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "ring_of_the_seven_deadly_sins/hurt_player"));

    public static final StreamCodec<ByteBuf, C2SHurtPlayer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SHurtPlayer::getIdentifier,
            ByteBufCodecs.INT, C2SHurtPlayer::getIndex,
            C2SHurtPlayer::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();

            CuriosApi.getCuriosInventory(player).flatMap(inventory -> inventory.findCurio(this.getIdentifier(), this.getIndex())).ifPresent(slot -> {
                var stack = slot.stack();

                if (!(stack.getItem() instanceof RingOfTheSevenDeadlySinsItem relic))
                    return;

                if (player.hurt(player.level().damageSources().magic(), 1F)) {
                    relic.addHurtTimer(stack, 40);

                    relic.getRelicData(player, stack).getStatisticData().getMetricData("unequip_attempts").addValue(1);
                }
            });
        });
    }
}