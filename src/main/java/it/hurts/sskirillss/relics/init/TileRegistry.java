package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TileRegistry {
    private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Reference.MODID);

    public static void register(IEventBus bus) {
        TILES.register(bus);
    }
}