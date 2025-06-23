package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.badges.ability.*;
import it.hurts.sskirillss.relics.badges.base.AbilityBadge;
import it.hurts.sskirillss.relics.badges.base.AbstractBadge;
import it.hurts.sskirillss.relics.badges.base.RelicBadge;
import it.hurts.sskirillss.relics.badges.relic.FlawlessRelicBadge;
import it.hurts.sskirillss.relics.utils.Reference;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BadgeRegistry {
    public static final DeferredRegister<AbstractBadge> BADGES = DeferredRegister.create(RelicsRegistries.BADGE_REGISTRY, Reference.MODID);

    public static final Supplier<AbilityBadge> SILENCE = BADGES.register("silence", SilenceBadge::new);
    public static final Supplier<AbilityBadge> OBLIVION = BADGES.register("oblivion", OblivionBadge::new);
    public static final Supplier<AbilityBadge> FLAWLESS_ABILITY = BADGES.register("flawless_ability", FlawlessAbilityBadge::new);
    public static final Supplier<AbilityBadge> INSTANTANEOUS = BADGES.register("instantaneous", InstantaneousBadge::new);
    public static final Supplier<AbilityBadge> INTERRUPTIBLE = BADGES.register("interruptible", InterruptibleBadge::new);
    public static final Supplier<AbilityBadge> CYCLICAL = BADGES.register("cyclical", CyclicalBadge::new);
    public static final Supplier<AbilityBadge> TOGGLEABLE = BADGES.register("toggleable", ToggleableBadge::new);
    public static final Supplier<AbilityBadge> CHARGEABLE = BADGES.register("chargeable", ChargeableBadge::new);

    public static final Supplier<RelicBadge> FLAWLESS_RELIC = BADGES.register("flawless_relic", FlawlessRelicBadge::new);

    public static void register(IEventBus bus) {
        BADGES.register(bus);
    }
}