package it.hurts.sskirillss.relics.api.relic_containers;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import it.hurts.sskirillss.relics.init.RelicsRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface RelicStackReference {
    StreamCodec<ByteBuf, RelicStackReference> STREAM_CODEC = ResourceLocation.STREAM_CODEC.dispatch(
            reference -> RelicsRegistries.RELIC_CONTAINER_REGISTRY.getKey(reference.getContainer()),
            id -> {
                if (RelicsRegistries.RELIC_CONTAINER_REGISTRY.get(id) instanceof SlottedRelicContainer<?> container)
                    return container.referenceCodec;

                throw new DecoderException("Unknown slotted relic container: " + id);
            }
    );

    ItemStack getStack(Player player);

    @Nullable
    default SlottedRelicContainer<?> getContainer() {
        return null;
    }
}
