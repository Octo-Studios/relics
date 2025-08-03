package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.client.renderer.items.base.IRelicRenderer;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class RelicsRelicRenderers {
    private static final Map<Item, Supplier<IRelicRenderer>> RENDERER_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<Item, IRelicRenderer> RENDERERS = new HashMap<>();

    public static void register(Item item, Supplier<IRelicRenderer> renderer) {
        RENDERER_REGISTRY.put(item, renderer);
    }

    public static Optional<IRelicRenderer> getRenderer(Item item) {
        return Optional.ofNullable(RENDERERS.get(item));
    }

    public static void init() {
        for (Map.Entry<Item, Supplier<IRelicRenderer>> entry : RENDERER_REGISTRY.entrySet())
            RENDERERS.put(entry.getKey(), entry.getValue().get());
    }
}