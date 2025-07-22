package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.BlockItemBase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelicsBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Relics.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Relics.MODID);

//    public static final DeferredHolder<Block, PhantomBlock> PHANTOM_BLOCK = BLOCKS.register("phantom_block", PhantomBlock::new);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);

        for (DeferredHolder<? extends Block, ? extends Block> block : BLOCKS.getEntries())
            ITEMS.register(block.getId().getPath(), () -> new BlockItemBase(block.get(), new Item.Properties()));

        ITEMS.register(bus);
    }
}