package it.hurts.sskirillss.relics.handlers;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicComponent;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CacheHandler {
    private static final Map<TemplateCacheKey, RelicTemplate> TEMPLATE_CACHE = new ConcurrentHashMap<>();

    public static RelicTemplate getOrCreateTemplate(TemplateCacheKey key, Function<TemplateCacheKey, RelicTemplate> factory) {
        return TEMPLATE_CACHE.computeIfAbsent(key, factory);
    }

    public static void clearTemplateCache() {
        TEMPLATE_CACHE.clear();
    }

    public record TemplateCacheKey(IRelicItem relic, RelicComponent component) {

    }

    @EventBusSubscriber
    public static class CommonEvents {
        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event) {
            CacheHandler.clearTemplateCache();
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
            CacheHandler.clearTemplateCache();
        }
    }
}
