package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.items.relics.feet.CutGlassBootItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber
public class RelicsCapabilities {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new CutGlassBootItem.CutGlassBootFluidHandler(stack),
                RelicsItems.CUT_GLASS_BOOT.get()
        );
    }
}