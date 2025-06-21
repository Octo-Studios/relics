package it.hurts.sskirillss.relics;

import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.utils.Reference;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Reference.MODID)
public class Relics {
    public static final Logger LOGGER = LogManager.getLogger(Reference.MODID);

    public Relics(IEventBus bus, ModContainer container) {
        bus.addListener(this::setupCommon);

        RelicsItems.register(bus);
        TileRegistry.register(bus);
        BlockRegistry.register(bus);
        SoundRegistry.register(bus);
        BadgeRegistry.register(bus);
        RelicsEntities.register(bus);
        EffectRegistry.register(bus);
        CommandRegistry.register(bus);
        ParticleRegistry.register(bus);
        LootCodecRegistry.register(bus);
        CreativeTabRegistry.register(bus);
        ScalingModelRegistry.register(bus);
        DataComponentRegistry.register(bus);
        RelicContainerRegistry.register(bus);
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        DispenserBehaviorRegistry.register();
        ConfigRegistry.register();

        InterModComms.sendTo("darkmodeeverywhere", "dme-shaderblacklist", () -> Reference.MODID);
    }
}