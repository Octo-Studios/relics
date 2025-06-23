package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.items.relics.base.data.cast.containers.CuriosRelicContainer;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.containers.InventoryRelicContainer;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.containers.base.RelicContainer;
import it.hurts.sskirillss.relics.utils.Reference;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RelicContainerRegistry {
    public static final DeferredRegister<RelicContainer> RELIC_CONTAINERS = DeferredRegister.create(RelicsRegistries.RELIC_CONTAINER_REGISTRY, Reference.MODID);

    public static final Supplier<RelicContainer> CURIOS = RELIC_CONTAINERS.register("curios", CuriosRelicContainer::new);
    public static final Supplier<RelicContainer> INVENTORY = RELIC_CONTAINERS.register("inventory", InventoryRelicContainer::new);

    public static void register(IEventBus bus) {
        RELIC_CONTAINERS.register(bus);
    }
}