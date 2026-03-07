package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.items.GoldenToothItem;
import it.hurts.sskirillss.relics.items.PetBoneItem;
import it.hurts.sskirillss.relics.items.CookedMeatballItem;
import it.hurts.sskirillss.relics.items.RawMeatballItem;
import it.hurts.sskirillss.relics.items.RelicExperienceBottleItem;
import it.hurts.sskirillss.relics.items.relics.ChorusStaffItem;
import it.hurts.sskirillss.relics.items.relics.ClotOfTimeItem;
import it.hurts.sskirillss.relics.items.relics.RiderFluteItem;
import it.hurts.sskirillss.relics.items.relics.SphereOfSelfSacrifice;
import it.hurts.sskirillss.relics.items.relics.back.LeafyMantleItem;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.belt.HuntingBeltItem;
import it.hurts.sskirillss.relics.items.relics.belt.KineticBeltItem;
import it.hurts.sskirillss.relics.items.relics.feet.CutGlassBootItem;
import it.hurts.sskirillss.relics.items.relics.feet.RollerSkateItem;
import it.hurts.sskirillss.relics.items.relics.feet.SpringyBootItem;
import it.hurts.sskirillss.relics.items.relics.head.ChefHatItem;
import it.hurts.sskirillss.relics.items.relics.head.PiglinMaskItem;
import it.hurts.sskirillss.relics.items.relics.necklace.JellyfishNecklaceItem;
import it.hurts.sskirillss.relics.items.relics.necklace.ReflectiveNecklaceItem;
import it.hurts.sskirillss.relics.items.relics.ring.RingOfTheSevenDeadlySinsItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelicsItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Relics.MODID);

    public static final DeferredHolder<Item, Item> RELIC_EXPERIENCE_BOTTLE = ITEMS.register("relic_experience_bottle", RelicExperienceBottleItem::new);
    public static final DeferredHolder<Item, Item> GOLDEN_TOOTH = ITEMS.register("golden_tooth", GoldenToothItem::new);
    public static final DeferredHolder<Item, Item> PET_BONE = ITEMS.register("pet_bone", PetBoneItem::new);
    public static final DeferredHolder<Item, Item> RAW_MEATBALL = ITEMS.register("raw_meatball", RawMeatballItem::new);
    public static final DeferredHolder<Item, Item> COOKED_MEATBALL = ITEMS.register("cooked_meatball", CookedMeatballItem::new);

//    public static final DeferredHolder<Item, RelicItem> MAGMA_WALKER = ITEMS.register("magma_walker", MagmaWalkerItem::new);
//    public static final DeferredHolder<Item, RelicItem> AQUA_WALKER = ITEMS.register("aqua_walker", AquaWalkerItem::new);
//    public static final DeferredHolder<Item, RelicItem> DROWNED_BELT = ITEMS.register("drowned_belt", DrownedBeltItem::new);
//    public static final DeferredHolder<Item, RelicItem> RAGE_GLOVE = ITEMS.register("rage_glove", RageGloveItem::new);
//    public static final DeferredHolder<Item, RelicItem> BASTION_RING = ITEMS.register("bastion_ring", BastionRingItem::new);
//    public static final DeferredHolder<Item, RelicItem> CHORUS_INHIBITOR = ITEMS.register("chorus_inhibitor", ChorusInhibitorItem::new);
//    public static final DeferredHolder<Item, RelicItem> HOLY_LOCKET = ITEMS.register("holy_locket", HolyLocketItem::new);
//    public static final DeferredHolder<Item, RelicItem> ELYTRA_BOOSTER = ITEMS.register("elytra_booster", ElytraBoosterItem::new);
//    public static final DeferredHolder<Item, RelicItem> SPORE_SACK = ITEMS.register("spore_sack", SporeSackItem::new);
//    public static final DeferredHolder<Item, RelicItem> SHADOW_GLAIVE = ITEMS.register("shadow_glaive", ShadowGlaiveItem::new);
//    public static final DeferredHolder<Item, RelicItem> INFINITY_HAM = ITEMS.register("infinity_ham", InfiniteHamItem::new); // TODO: Replace ID with "infinite_ham"
//    public static final DeferredHolder<Item, RelicItem> PHANTOM_BOOT = ITEMS.register("phantom_boot", PhantomBootItem::new);

    // NEW GEN
    public static final DeferredHolder<Item, RelicItem> LEAFY_MANTLE = ITEMS.register("leafy_mantle", LeafyMantleItem::new);
    public static final DeferredHolder<Item, RelicItem> SPRINGY_BOOT = ITEMS.register("springy_boot", SpringyBootItem::new);
    public static final DeferredHolder<Item, RelicItem> KINETIC_BELT = ITEMS.register("kinetic_belt", KineticBeltItem::new);
    public static final DeferredHolder<Item, RelicItem> REFLECTIVE_NECKLACE = ITEMS.register("reflective_necklace", ReflectiveNecklaceItem::new);
    public static final DeferredHolder<Item, RelicItem> JELLYFISH_NECKLACE = ITEMS.register("jellyfish_necklace", JellyfishNecklaceItem::new);
    public static final DeferredHolder<Item, RelicItem> MIDNIGHT_MANTLE = ITEMS.register("midnight_mantle", MidnightMantleItem::new);
    public static final DeferredHolder<Item, RelicItem> ROLLER_SKATE = ITEMS.register("roller_skate", RollerSkateItem::new);
    public static final DeferredHolder<Item, RelicItem> CHORUS_STAFF = ITEMS.register("chorus_staff", ChorusStaffItem::new);
    public static final DeferredHolder<Item, RelicItem> CLOT_OF_TIME = ITEMS.register("clot_of_time", ClotOfTimeItem::new);
    public static final DeferredHolder<Item, RelicItem> PIGLIN_MASK = ITEMS.register("piglin_mask", PiglinMaskItem::new);
    public static final DeferredHolder<Item, RelicItem> CHEF_HAT = ITEMS.register("chef_hat", ChefHatItem::new);
    public static final DeferredHolder<Item, RelicItem> CUT_GLASS_BOOT = ITEMS.register("cut_glass_boot", CutGlassBootItem::new);
    public static final DeferredHolder<Item, RelicItem> RIDER_FLUTE = ITEMS.register("rider_flute", RiderFluteItem::new);
    public static final DeferredHolder<Item, RelicItem> RING_OF_THE_SEVEN_DEADLY_SINS = ITEMS.register("ring_of_the_seven_deadly_sins", RingOfTheSevenDeadlySinsItem::new);
    public static final DeferredHolder<Item, RelicItem> SPHERE_OF_SELF_SACRIFICE = ITEMS.register("sphere_of_self_sacrifice", SphereOfSelfSacrifice::new);
    public static final DeferredHolder<Item, RelicItem> HUNTING_BELT = ITEMS.register("hunting_belt", HuntingBeltItem::new);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
