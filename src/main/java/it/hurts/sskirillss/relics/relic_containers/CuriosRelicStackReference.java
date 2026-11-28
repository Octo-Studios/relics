package it.hurts.sskirillss.relics.relic_containers;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.api.relic_containers.RelicStackReference;
import it.hurts.sskirillss.relics.init.RelicsRelicContainers;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public record CuriosRelicStackReference(String identifier, int index) implements RelicStackReference {
    public static final StreamCodec<ByteBuf, CuriosRelicStackReference> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CuriosRelicStackReference::identifier,
            ByteBufCodecs.VAR_INT, CuriosRelicStackReference::index,
            CuriosRelicStackReference::new
    );

    @Override
    public CuriosRelicContainer getContainer() {
        return RelicsRelicContainers.CURIOS.get();
    }

    @Override
    public ItemStack getStack(Player player) {
        if (index < 0)
            return ItemStack.EMPTY;

        return CuriosApi.getCuriosInventory(player)
                .flatMap(inventory -> inventory.findCurio(identifier, index))
                .map(SlotResult::stack)
                .orElse(ItemStack.EMPTY);
    }
}
