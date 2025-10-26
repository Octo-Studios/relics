package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.api.postEffects.PostEffect;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class RelicsPostEffects {
    private static final Map<ResourceLocation, Supplier<PostEffect>> POST_EFFECTS_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, PostEffect> POST_EFFECTS = new HashMap<>();

    public static void register(Supplier<PostEffect> postEffect) {
        POST_EFFECTS_REGISTRY.put(postEffect.get().getPath(), postEffect);
    }

    public static Map<ResourceLocation, PostEffect> getPostEffects() {
        return POST_EFFECTS;
    }

    public static void init() {
        for (Map.Entry<ResourceLocation, Supplier<PostEffect>> entry : POST_EFFECTS_REGISTRY.entrySet())
            POST_EFFECTS.put(entry.getKey(), entry.getValue().get());
    }
}