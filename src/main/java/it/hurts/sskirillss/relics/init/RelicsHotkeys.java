package it.hurts.sskirillss.relics.init;

import com.mojang.blaze3d.platform.InputConstants;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;

@EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
public class RelicsHotkeys {
    private static final String CATEGORY = "Relics";

    public static final KeyMapping RESEARCH_RELIC = new KeyMapping("key.relics.research_relic", GLFW_KEY_LEFT_SHIFT, CATEGORY);
    public static final KeyMapping ACTIVE_ABILITIES_LIST = new KeyMapping("key.relics.active_abilities_list", GLFW_KEY_LEFT_ALT, CATEGORY);

    public static boolean isHeld(KeyMapping mapping) {
        if (mapping.isUnbound() || !mapping.isConflictContextAndModifierActive())
            return false;

        var key = mapping.getKey();
        var window = Minecraft.getInstance().getWindow().getWindow();

        return switch (key.getType()) {
            case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
            case KEYSYM -> InputConstants.isKeyDown(window, key.getValue());
            case SCANCODE -> mapping.isDown();
        };
    }

    @SubscribeEvent
    public static void onKeybindingRegistry(RegisterKeyMappingsEvent event) {
        event.register(RESEARCH_RELIC);
        event.register(ACTIVE_ABILITIES_LIST);
    }
}
