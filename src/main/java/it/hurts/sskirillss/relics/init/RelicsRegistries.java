package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.scaling_models.ScalingModel;
import it.hurts.sskirillss.relics.api.relic_containers.RelicContainer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber
public class RelicsRegistries {
    public static final ResourceKey<Registry<RelicContainer>> RELIC_CONTAINER_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "relic_containers"));
    public static final Registry<RelicContainer> RELIC_CONTAINER_REGISTRY = new RegistryBuilder<>(RELIC_CONTAINER_REGISTRY_KEY).create();

    public static final ResourceKey<Registry<ScalingModel>> SCALING_MODEL_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "scaling_models"));
    public static final Registry<ScalingModel> SCALING_MODEL_REGISTRY = new RegistryBuilder<>(SCALING_MODEL_REGISTRY_KEY).create();

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(SCALING_MODEL_REGISTRY);
        event.register(RELIC_CONTAINER_REGISTRY);
    }
}