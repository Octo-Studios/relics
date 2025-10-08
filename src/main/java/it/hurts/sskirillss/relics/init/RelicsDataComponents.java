package it.hurts.sskirillss.relics.init;

import com.mojang.serialization.Codec;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LEAFY_MANTLE_PROGRESS = RelicsDataComponents.construct("leafy_mantle/progress", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LEAFY_MANTLE_HIDING = RelicsDataComponents.construct("leafy_mantle/hiding", Codec.BOOL);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PIGLIN_MASK_STACKS = RelicsDataComponents.construct("piglin_mask/stacks", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PIGLIN_MASK_DURATION = RelicsDataComponents.construct("piglin_mask/duration", Codec.INT);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHORUS_STAFF_CHARGE = RelicsDataComponents.construct("chorus_staff/charge", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CHORUS_STAFF_SAFE_FALL = RelicsDataComponents.construct("chorus_staff/safe_fall", Codec.BOOL);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIDNIGHT_MANTLE_PHASE_DURATION = RelicsDataComponents.construct("midnight_mantle/duration", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MIDNIGHT_MANTLE_INVISIBILITY_COOLDOWN = RelicsDataComponents.construct("midnight_mantle/invisibility_cooldown", Codec.INT);

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