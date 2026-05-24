package it.hurts.sskirillss.relics;

import it.hurts.sskirillss.relics.init.*;
import it.hurts.sskirillss.relics.compat.ftbteams.FTBTeamsTargetingProvider;
import it.hurts.sskirillss.relics.compat.minecraft.MinecraftTargetingProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
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
        RelicsBlocks.register(bus);
        RelicsSounds.register(bus);
        RelicsEntities.register(bus);
        RelicsCommands.register(bus);
        RelicsParticles.register(bus);
        RelicsMobEffects.register(bus);
        RelicsLootCodecs.register(bus);
        RelicsAttachments.register(bus);
        RelicsCreativeTabs.register(bus);
        RelicsBlockEntities.register(bus);
        RelicsScalingModels.register(bus);
        RelicsDataComponents.register(bus);
        RelicsRelicContainers.register(bus);
    }

    private void setupCommon(final FMLCommonSetupEvent event) {
        RelicsDispenserBehaviors.register();
        RelicsConfigs.register();

        MinecraftTargetingProvider.register();

        if (ModList.get().isLoaded("ftbteams"))
            FTBTeamsTargetingProvider.register();

        InterModComms.sendTo("darkmodeeverywhere", "dme-shaderblacklist", () -> Relics.MODID);
    }
}
