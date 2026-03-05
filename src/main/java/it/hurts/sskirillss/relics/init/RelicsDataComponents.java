package it.hurts.sskirillss.relics.init;

import com.mojang.serialization.Codec;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicComponent;
import it.hurts.sskirillss.relics.items.PetBoneItem;
import it.hurts.sskirillss.relics.items.relics.ClotOfTimeItem;
import it.hurts.sskirillss.relics.items.relics.RiderFluteItem;
import it.hurts.sskirillss.relics.items.relics.SphereOfSelfSacrifice;
import it.hurts.sskirillss.relics.items.relics.feet.CutGlassBootItem;
import it.hurts.sskirillss.relics.items.relics.ring.RingOfTheSevenDeadlySinsItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class RelicsDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Relics.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RelicComponent>> RELIC = DATA_COMPONENTS.register("data", // TODO: Rename to "relic" instead of "data"
            () -> DataComponentType.<RelicComponent>builder()
                    .persistent(RelicComponent.CODEC)
                    .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, CutGlassBootItem.FluidEntry>>> CUT_GLASS_BOOT_FLUIDS = RelicsDataComponents.construct("cut_glass_boot/fluids", Codec.unboundedMap(Codec.STRING, CutGlassBootItem.FluidEntry.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CUT_GLASS_BOOT_SELECTED_FLUID_INDEX = RelicsDataComponents.construct("cut_glass_boot/selected_fluid_index", Codec.INT);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<RiderFluteItem.HorseSlotData>>> RIDER_FLUTE_SLOTS = RelicsDataComponents.construct("rider_flute/slots", Codec.list(RiderFluteItem.HorseSlotData.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RIDER_FLUTE_SELECTED_SLOT_INDEX = RelicsDataComponents.construct("rider_flute/selected_slot_index", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> CLOT_OF_TIME_REWIND_CURSOR = RelicsDataComponents.construct("clot_of_time/rewind_cursor", Codec.DOUBLE);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CLOT_OF_TIME_COOLDOWN = RelicsDataComponents.construct("clot_of_time/cooldown", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CLOT_OF_TIME_USE_TICKS = RelicsDataComponents.construct("clot_of_time/use_ticks", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ClotOfTimeItem.PathPointData>>> CLOT_OF_TIME_PATH = RelicsDataComponents.construct("clot_of_time/path", Codec.list(ClotOfTimeItem.PathPointData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LEAFY_MANTLE_PROGRESS = RelicsDataComponents.construct("leafy_mantle/progress", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LEAFY_MANTLE_HIDING = RelicsDataComponents.construct("leafy_mantle/hiding", Codec.BOOL);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LEAFY_MANTLE_INVISIBILITY_COOLDOWN = RelicsDataComponents.construct("leafy_mantle/invisibility_cooldown", Codec.INT);

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
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> KINETIC_BELT_LAST_ELECTRICITY_ID = RelicsDataComponents.construct("kinetic_belt/last_electricity_id", Codec.INT);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> JELLYFISH_NECKLACE_AFFECTED_ENTITIES = RelicsDataComponents.construct("jellyfish_necklace/affected_entities", Codec.list(Codec.STRING));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> JELLYFISH_NECKLACE_DAMAGED_ENTITIES = RelicsDataComponents.construct("jellyfish_necklace/damaged_entities", Codec.list(Codec.STRING));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JELLYFISH_NECKLACE_COOLDOWN = RelicsDataComponents.construct("jellyfish_necklace/cooldown", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JELLYFISH_NECKLACE_DURATION = RelicsDataComponents.construct("jellyfish_necklace/duration", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> JELLYFISH_NECKLACE_RINGS = RelicsDataComponents.construct("jellyfish_necklace/rings", Codec.INT);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RingOfTheSevenDeadlySinsItem.SlothData>> RING_OF_THE_SEVEN_DEADLY_SINS_SLOTH = RelicsDataComponents.construct("ring_of_the_seven_deadly_sins/sloth", RingOfTheSevenDeadlySinsItem.SlothData.CODEC);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RingOfTheSevenDeadlySinsItem.WrathData>> RING_OF_THE_SEVEN_DEADLY_SINS_WRATH = RelicsDataComponents.construct("ring_of_the_seven_deadly_sins/wrath", RingOfTheSevenDeadlySinsItem.WrathData.CODEC);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RING_OF_THE_SEVEN_DEADLY_SINS_HURT_TIMER = RelicsDataComponents.construct("ring_of_the_seven_deadly_sins/hurt_timer", Codec.INT);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<SphereOfSelfSacrifice.HealingStack>>> SPHERE_OF_SELF_SACRIFICE_STACKS = RelicsDataComponents.construct("sphere_of_self_sacrifice/stacks", Codec.list(SphereOfSelfSacrifice.HealingStack.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PetBoneItem.PetBoneData>> PET_BONE_DATA = RelicsDataComponents.construct("pet_bone/data", PetBoneItem.PetBoneData.CODEC);

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
        event.modifyMatching(item -> item instanceof IRelicItem, builder -> builder.set(RELIC.get(), RelicComponent.EMPTY));
    }
}
