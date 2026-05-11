package it.hurts.sskirillss.relics.network.packets.abilities;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

@Data
@AllArgsConstructor
public class C2SActivateAbility implements CustomPacketPayload {
    private final String source;
    private final int slot;
    private final String identifier;
    private final String ability;
    private final AbilityActivationStage stage;

    public static final Type<C2SActivateAbility> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "abilities/activate"));

    public static final StreamCodec<ByteBuf, C2SActivateAbility> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SActivateAbility::getSource,
            ByteBufCodecs.INT, C2SActivateAbility::getSlot,
            ByteBufCodecs.STRING_UTF8, C2SActivateAbility::getIdentifier,
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
            var stack = resolveStack(player);

            if (!(stack.getItem() instanceof IRelicItem relic))
                return;

            var abilityData = relic.getRelicData(player, stack).getAbilitiesData().getAbilityData(this.ability);

            if (abilityData != null)
                abilityData.activate(player, this.stage);
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
