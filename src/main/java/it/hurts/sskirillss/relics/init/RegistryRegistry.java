package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.api.relics.abilities.stats.UpgradeOperation;
import it.hurts.sskirillss.relics.badges.base.AbstractBadge;
import it.hurts.sskirillss.relics.items.relics.base.data.cast.containers.base.RelicContainer;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class RegistryRegistry {
    public static final ResourceKey<Registry<RelicContainer>> RELIC_CONTAINER_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "relic_containers"));
    public static final Registry<RelicContainer> RELIC_CONTAINER_REGISTRY = new RegistryBuilder<>(RELIC_CONTAINER_REGISTRY_KEY).create();

    public static final ResourceKey<Registry<AbstractBadge>> BADGE_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "badges"));
    public static final Registry<AbstractBadge> BADGE_REGISTRY = new RegistryBuilder<>(BADGE_REGISTRY_KEY).create();

    public static final ResourceKey<Registry<UpgradeOperation>> UPGRADE_OPERATION_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "upgrade_operations"));
    public static final Registry<UpgradeOperation> UPGRADE_OPERATION_REGISTRY = new RegistryBuilder<>(UPGRADE_OPERATION_REGISTRY_KEY).create();

    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(UPGRADE_OPERATION_REGISTRY);
        event.register(RELIC_CONTAINER_REGISTRY);
        event.register(BADGE_REGISTRY);
    }
}