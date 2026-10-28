package it.hurts.sskirillss.relics.client.screen.utils.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.resources.ResourceLocation;

public class ToggleSwitch {
    private static final ResourceLocation SLIDER_ON = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_slider_on.png");
    private static final ResourceLocation SLIDER_OFF = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_slider_off.png");
    private static final ResourceLocation KNOT_ON = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_knot_on.png");
    private static final ResourceLocation KNOT_OFF = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/rank_modifier_switch_knot_off.png");

    private static final float KNOT_TRAVEL = 3F;

    public static void render(PoseStack poseStack, float amount) {
        render(poseStack, amount, amount);
    }

    public static void render(PoseStack poseStack, float position, float amount) {
        var eased = position * position * (3F - 2F * position);

        RenderSystem.enableBlend();

        GUIRenderer.begin(SLIDER_OFF, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(1, 1)
                .end();

        GUIRenderer.begin(SLIDER_ON, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(1, 1)
                .alpha(amount)
                .end();

        GUIRenderer.begin(KNOT_OFF, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(1 + KNOT_TRAVEL * eased, 1)
                .alpha(1F - amount)
                .end();

        GUIRenderer.begin(KNOT_ON, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(1 + KNOT_TRAVEL * eased, 1)
                .alpha(amount)
                .end();

        RenderSystem.disableBlend();
    }
}
