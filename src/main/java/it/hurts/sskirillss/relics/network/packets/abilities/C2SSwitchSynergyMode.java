package it.hurts.sskirillss.relics.network.packets.abilities;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.relic.abilities.synergy.SynergyModeSwitchEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

@Data
@AllArgsConstructor
public class C2SSwitchSynergyMode implements CustomPacketPayload {
    private final String source;
    private final int slot;
    private final String identifier;
    private final String synergy;
    private final String mode;

    public static final Type<C2SSwitchSynergyMode> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "abilities/switch_synergy_mode"));

    public static final StreamCodec<ByteBuf, C2SSwitchSynergyMode> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SSwitchSynergyMode::getSource,
            ByteBufCodecs.INT, C2SSwitchSynergyMode::getSlot,
            ByteBufCodecs.STRING_UTF8, C2SSwitchSynergyMode::getIdentifier,
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
            var stack = this.resolveStack(player);

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            var synergyData = relic.getRelicData(player, stack).getAbilitiesData().getSynergyData(this.synergy);

            if (synergyData == null || !synergyData.isUnlocked() || !synergyData.getTemplate().getModes().contains(this.mode))
                return;

            var event = new SynergyModeSwitchEvent(player, stack, this.synergy, synergyData.getMode(), this.mode);

            NeoForge.EVENT_BUS.post(event);

            if (!event.isCanceled())
                synergyData.setMode(event.getToMode());
        });
    }

    private ItemStack resolveStack(net.minecraft.world.entity.player.Player player) {
        return switch (this.source) {
            case "inventory" -> this.slot >= 0 && this.slot < player.getInventory().getContainerSize()
                    ? player.getInventory().getItem(this.slot)
                    : ItemStack.EMPTY;
            case "curios" -> CuriosApi.getCuriosInventory(player)
                    .flatMap(inventory -> inventory.findCurio(this.identifier, this.slot))
                    .map(SlotResult::stack)
                    .orElse(ItemStack.EMPTY);
            default -> ItemStack.EMPTY;
        };
    }
}
