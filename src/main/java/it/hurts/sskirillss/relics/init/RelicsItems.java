package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.items.RelicExperienceBottleItem;
import it.hurts.sskirillss.relics.items.SolidSnowballItem;
import it.hurts.sskirillss.relics.items.relics.*;
import it.hurts.sskirillss.relics.items.relics.back.ElytraBoosterItem;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import it.hurts.sskirillss.relics.items.relics.belt.DrownedBeltItem;
import it.hurts.sskirillss.relics.items.relics.belt.HunterBeltItem;
import it.hurts.sskirillss.relics.items.relics.belt.LeatherBeltItem;
import it.hurts.sskirillss.relics.items.relics.feet.*;
import it.hurts.sskirillss.relics.items.relics.hands.EnderHandItem;
import it.hurts.sskirillss.relics.items.relics.hands.RageGloveItem;
import it.hurts.sskirillss.relics.items.relics.hands.WoolMittenItem;
import it.hurts.sskirillss.relics.items.relics.necklace.HolyLocketItem;
import it.hurts.sskirillss.relics.items.relics.necklace.JellyfishNecklaceItem;
import it.hurts.sskirillss.relics.items.relics.necklace.ReflectiveNecklaceItem;
import it.hurts.sskirillss.relics.items.relics.ring.BastionRingItem;
import it.hurts.sskirillss.relics.items.relics.ring.LeafyRingItem;
import it.hurts.sskirillss.relics.items.relics.ring.ChorusInhibitorItem;
import it.hurts.sskirillss.relics.items.relics.charm.SporeSackItem;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RelicsItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Reference.MODID);

    public static final DeferredHolder<Item, Item> SOLID_SNOWBALL = ITEMS.register("solid_snowball", SolidSnowballItem::new);
    public static final DeferredHolder<Item, Item> RELIC_EXPERIENCE_BOTTLE = ITEMS.register("relic_experience_bottle", RelicExperienceBottleItem::new);

    // TODO: public static final DeferredHolder<Item, RelicItem> SPATIAL_SIGN = ITEMS.register("spatial_sign", SpatialSignItem::new);
    public static final DeferredHolder<Item, RelicItem> REFLECTIVE_NECKLACE = ITEMS.register("reflective_necklace", ReflectiveNecklaceItem::new);
    public static final DeferredHolder<Item, RelicItem> MAGMA_WALKER = ITEMS.register("magma_walker", MagmaWalkerItem::new);
    public static final DeferredHolder<Item, RelicItem> AQUA_WALKER = ITEMS.register("aqua_walker", AquaWalkerItem::new);
    public static final DeferredHolder<Item, RelicItem> MIDNIGHT_MANTLE = ITEMS.register("midnight_mantle", MidnightMantleItem::new);
    public static final DeferredHolder<Item, RelicItem> DROWNED_BELT = ITEMS.register("drowned_belt", DrownedBeltItem::new);
    public static final DeferredHolder<Item, RelicItem> JELLYFISH_NECKLACE = ITEMS.register("jellyfish_necklace", JellyfishNecklaceItem::new);
    public static final DeferredHolder<Item, RelicItem> HUNTER_BELT = ITEMS.register("hunter_belt", HunterBeltItem::new);
    public static final DeferredHolder<Item, RelicItem> RAGE_GLOVE = ITEMS.register("rage_glove", RageGloveItem::new);
    public static final DeferredHolder<Item, RelicItem> ICE_SKATES = ITEMS.register("ice_skates", IceSkatesItem::new);
    public static final DeferredHolder<Item, RelicItem> BASTION_RING = ITEMS.register("bastion_ring", BastionRingItem::new);
    public static final DeferredHolder<Item, RelicItem> CHORUS_INHIBITOR = ITEMS.register("chorus_inhibitor", ChorusInhibitorItem::new);
    // TODO: public static final DeferredHolder<Item, RelicItem> ARROW_QUIVER = ITEMS.register("arrow_quiver", ArrowQuiverItem::new);
    public static final DeferredHolder<Item, RelicItem> SPACE_DISSECTOR = ITEMS.register("space_dissector", SpaceDissectorItem::new);
    public static final DeferredHolder<Item, RelicItem> HOLY_LOCKET = ITEMS.register("holy_locket", HolyLocketItem::new);
    public static final DeferredHolder<Item, RelicItem> ENDER_HAND = ITEMS.register("enders_hand", EnderHandItem::new);
    public static final DeferredHolder<Item, RelicItem> ELYTRA_BOOSTER = ITEMS.register("elytra_booster", ElytraBoosterItem::new);
    public static final DeferredHolder<Item, RelicItem> MAGIC_MIRROR = ITEMS.register("magic_mirror", MagicMirrorItem::new);
    public static final DeferredHolder<Item, RelicItem> ICE_BREAKER = ITEMS.register("ice_breaker", IceBreakerItem::new);
    public static final DeferredHolder<Item, RelicItem> BLAZING_FLASK = ITEMS.register("blazing_flask", BlazingFlaskItem::new);
    public static final DeferredHolder<Item, RelicItem> SPORE_SACK = ITEMS.register("spore_sack", SporeSackItem::new);
    public static final DeferredHolder<Item, RelicItem> SHADOW_GLAIVE = ITEMS.register("shadow_glaive", ShadowGlaiveItem::new);
    public static final DeferredHolder<Item, RelicItem> ROLLER_SKATES = ITEMS.register("roller_skates", RollerSkatesItem::new);
    public static final DeferredHolder<Item, RelicItem> INFINITY_HAM = ITEMS.register("infinity_ham", InfiniteHamItem::new); // TODO: Replace ID with "infinite_ham"
    public static final DeferredHolder<Item, RelicItem> LEATHER_BELT = ITEMS.register("leather_belt", LeatherBeltItem::new);
    // TODO: public static final DeferredHolder<Item, RelicItem> HORSE_FLUTE = ITEMS.register("horse_flute", HorseFluteItem::new);
    public static final DeferredHolder<Item, RelicItem> WOOL_MITTEN = ITEMS.register("wool_mitten", WoolMittenItem::new);
    public static final DeferredHolder<Item, RelicItem> AMPHIBIAN_BOOT = ITEMS.register("amphibian_boot", AmphibianBootItem::new);

    // NEW GEN
    public static final DeferredHolder<Item, RelicItem> LEAFY_RING = ITEMS.register("leafy_ring", LeafyRingItem::new);
    public static final DeferredHolder<Item, RelicItem> PHANTOM_BOOT = ITEMS.register("phantom_boot", PhantomBootItem::new);
    public static final DeferredHolder<Item, RelicItem> SPRINGY_BOOT = ITEMS.register("springy_boot", SpringyBootItem::new);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}