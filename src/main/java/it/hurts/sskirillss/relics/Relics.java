package it.hurts.sskirillss.relics;

import it.hurts.sskirillss.relics.init.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Relics.MODID)
public class Relics {
    public static final String MODID = "relics";

    public static final Logger LOGGER = LogManager.getLogger(Relics.MODID);

    public Relics(IEventBus bus, ModContainer container) {
        bus.addListener(this::setupCommon);

        RelicsItems.register(bus);
        TileRegistry.register(bus);
        RelicsBlocks.register(bus);
        RelicsSounds.register(bus);
        BadgeRegistry.register(bus);
        RelicsEntities.register(bus);
        RelicsMobEffects.register(bus);
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

        InterModComms.sendTo("darkmodeeverywhere", "dme-shaderblacklist", () -> Relics.MODID);
    }
}