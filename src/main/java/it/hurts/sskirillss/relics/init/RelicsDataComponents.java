package it.hurts.sskirillss.relics.init;

import com.mojang.serialization.Codec;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicComponent;
import it.hurts.sskirillss.relics.utils.data.WorldPosition;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class RelicsDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Relics.MODID);

    // TODO: Rename to RELIC_DATA or just RELIC instead of DATA
    @Deprecated(since = "1.21", forRemoval = true)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RelicComponent>> DATA = DATA_COMPONENTS.register("data",
            () -> DataComponentType.<RelicComponent>builder()
                    .persistent(RelicComponent.CODEC)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE = DATA_COMPONENTS.register("charge",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TOGGLED = DATA_COMPONENTS.register("toggled",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TIME = DATA_COMPONENTS.register("time",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COOLDOWN = DATA_COMPONENTS.register("cooldown",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DURATION = DATA_COMPONENTS.register("duration",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COUNT = DATA_COMPONENTS.register("count",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PROGRESS = DATA_COMPONENTS.register("progress",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> TARGET = DATA_COMPONENTS.register("target",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> SPEED = DATA_COMPONENTS.register("speed",
            () -> DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> RADIUS = DATA_COMPONENTS.register("radius",
            () -> DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> HEIGHT = DATA_COMPONENTS.register("height",
            () -> DataComponentType.<Double>builder()
                    .persistent(Codec.DOUBLE)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WorldPosition>> WORLD_POSITION = DATA_COMPONENTS.register("world_position",
            () -> DataComponentType.<WorldPosition>builder()
                    .persistent(WorldPosition.CODEC)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PORTAL = DATA_COMPONENTS.register("portal",
            () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> BLOCK_STATE = DATA_COMPONENTS.register("block_state",
            () -> DataComponentType.<BlockState>builder()
                    .persistent(BlockState.CODEC)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MODE = DATA_COMPONENTS.register("mode",
            () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIDNIGHT_MANTLE_PHASE_DURATION = RelicsDataComponents.construct("midnight_mantle/duration", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIDNIGHT_MANTLE_INVISIBILITY_COOLDOWN = RelicsDataComponents.construct("midnight_mantle/invisibility_cooldown", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIDNIGHT_MANTLE_CONSTELLATION_COOLDOWN = RelicsDataComponents.construct("midnight_mantle/constellation_cooldown", Codec.INT);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ROLLER_SKATE_DURATION = RelicsDataComponents.construct("roller_skate/duration", Codec.INT);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SPRINGY_BOOT_BOUNCE_COOLDOWN = RelicsDataComponents.construct("springy_boot/bounce_cooldown", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SPRINGY_BOOT_LEAPED = RelicsDataComponents.construct("springy_boot/leaped", Codec.BOOL);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SPRINGY_BOOT_LEAPS = RelicsDataComponents.construct("springy_boot/leaps", Codec.INT);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> KINETIC_BELT_ACTIVE = RelicsDataComponents.construct("kinetic_belt/active", Codec.BOOL);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> KINETIC_BELT_LANDED = RelicsDataComponents.construct("kinetic_belt/landed", Codec.BOOL);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> JELLYFISH_NECKLACE_AFFECTED_ENTITIES = RelicsDataComponents.construct("jellyfish_necklace/affected_entities", Codec.list(Codec.STRING));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> JELLYFISH_NECKLACE_DAMAGED_ENTITIES = RelicsDataComponents.construct("jellyfish_necklace/damaged_entities", Codec.list(Codec.STRING));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JELLYFISH_NECKLACE_COOLDOWN = RelicsDataComponents.construct("jellyfish_necklace/cooldown", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JELLYFISH_NECKLACE_DURATION = RelicsDataComponents.construct("jellyfish_necklace/duration", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JELLYFISH_NECKLACE_RINGS = RelicsDataComponents.construct("jellyfish_necklace/rings", Codec.INT);

    public static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> construct(String name, Codec<T> codec) {
        return DATA_COMPONENTS.register(name, () -> DataComponentType.<T>builder()
                .persistent(codec)
                .build());
    }

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }

    @SubscribeEvent
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modifyMatching(item -> item instanceof IRelicItem, builder -> builder.set(DATA.get(), RelicComponent.EMPTY));
    }
}