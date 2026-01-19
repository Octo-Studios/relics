package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.relic_containers.CuriosRelicContainer;
import it.hurts.sskirillss.relics.relic_containers.InventoryRelicContainer;
import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class RelicsRelicContainers {
    public static final DeferredRegister<RelicContainer> RELIC_CONTAINERS = DeferredRegister.create(RelicsRegistries.RELIC_CONTAINER_REGISTRY, Relics.MODID);

    public static final Supplier<RelicContainer> CURIOS = RELIC_CONTAINERS.register("curios", CuriosRelicContainer::new);
    public static final Supplier<RelicContainer> INVENTORY = RELIC_CONTAINERS.register("inventory", InventoryRelicContainer::new);

    public static void register(IEventBus bus) {
        RELIC_CONTAINERS.register(bus);
    }
}