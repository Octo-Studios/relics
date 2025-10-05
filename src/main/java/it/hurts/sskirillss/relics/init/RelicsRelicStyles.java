package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.client.style.base.RelicStyle;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class RelicsRelicStyles {
    private static final Map<Item, Supplier<RelicStyle>> STYLE_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<Item, RelicStyle> STYLES = new HashMap<>();

    public static void register(Item item, Supplier<RelicStyle> style) {
        STYLE_REGISTRY.put(item, style);
    }

    public static Optional<RelicStyle> getStyle(Item item) {
        return Optional.ofNullable(STYLES.get(item));
    }

    public static void init() {
        for (Map.Entry<Item, Supplier<RelicStyle>> entry : STYLE_REGISTRY.entrySet())
            STYLES.put(entry.getKey(), entry.getValue().get());
    }
}