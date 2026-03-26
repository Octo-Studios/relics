package it.hurts.sskirillss.relics.init;

import it.hurts.sskirillss.relics.Relics;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

@EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
public class RelicsHotkeys {
    private static final String CATEGORY = "Relics";

    public static final KeyMapping RESEARCH_RELIC = new KeyMapping("key.relics.research_relic", GLFW_KEY_LEFT_SHIFT, CATEGORY);

    @SubscribeEvent
    public static void onKeybindingRegistry(RegisterKeyMappingsEvent event) {
        event.register(RESEARCH_RELIC);
    }
}