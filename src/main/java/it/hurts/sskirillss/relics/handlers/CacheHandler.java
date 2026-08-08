package it.hurts.sskirillss.relics.handlers;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class CacheHandler {
    private static final Map<TemplateCacheKey, RelicTemplate> TEMPLATE_CACHE = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<TemplateCacheKey, RelicTemplate> eldest) {
            return size() > 1024;
        }
    });

    public static RelicTemplate getOrCreateTemplate(TemplateCacheKey key, Function<TemplateCacheKey, RelicTemplate> factory) {
        synchronized (TEMPLATE_CACHE) {
            var template = TEMPLATE_CACHE.get(key);

            if (template != null)
                return template;
        }

        var template = factory.apply(key);

        synchronized (TEMPLATE_CACHE) {
            var cached = TEMPLATE_CACHE.get(key);

            if (cached != null)
                return cached;

            TEMPLATE_CACHE.put(key, template);

            return template;
        }
    }

    public static void clearTemplateCache() {
        TEMPLATE_CACHE.clear();
    }

    public record TemplateCacheKey(IRelicItem relic, Object state) {

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
