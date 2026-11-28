package it.hurts.sskirillss.relics.api.relic_containers;

import io.netty.buffer.ByteBuf;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class SlottedRelicContainer<R extends RelicStackReference> extends RelicContainer {
    final StreamCodec<ByteBuf, R> referenceCodec;

    protected SlottedRelicContainer(StreamCodec<ByteBuf, R> referenceCodec) {
        this.referenceCodec = referenceCodec;
    }

    protected abstract void visitSlots(LivingEntity entity, BiConsumer<R, ItemStack> consumer);

    @Override
    protected void visitRelics(LivingEntity entity, Consumer<ItemStack> consumer) {
        visitSlots(entity, (slot, stack) -> {
            if (stack.getItem() instanceof IRelicItem)
                consumer.accept(stack);
        });
    }

    @Override
    public Function<LivingEntity, List<ItemStack>> gatherRelics() {
        return entity -> {
            var stacks = new ArrayList<ItemStack>();

            visitRelics(entity, stacks::add);

            return stacks;
        };
    }

    @Override
    public Function<Player, List<RelicStackReference>> gatherRelicReferences() {
        return player -> {
            var references = new ArrayList<RelicStackReference>();

            visitSlots(player, (reference, stack) -> {
                if (stack.getItem() instanceof IRelicItem)
                    references.add(reference);
            });

            return references;
        };
    }
}
