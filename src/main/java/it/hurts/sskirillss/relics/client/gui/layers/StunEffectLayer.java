package it.hurts.sskirillss.relics.client.gui.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import it.hurts.sskirillss.relics.utils.data.SpriteMirror;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

public class StunEffectLayer implements LayeredDraw.Layer {
    private static final ResourceLocation VIGNETTE = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/hud/layer/stun_effect/vignette.png");

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        var MC = Minecraft.getInstance();

        var player = MC.player;

        if (player == null)
            return;

        var effect = player.getEffect(RelicsMobEffects.STUN);

        if (effect == null)
            return;

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        var window = MC.getWindow();

        var width = window.getGuiScaledWidth();
        var height = 128;

        var alpha = Math.min(effect.getDuration() * 0.01F, 1);

        RenderSystem.enableBlend();

        GUIRenderer.begin(VIGNETTE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .patternSize(width, height)
                .texSize(1, height)
                .alpha(alpha)
                .end();

        GUIRenderer.begin(VIGNETTE, poseStack)
                .pos(0, window.getGuiScaledHeight() - height)
                .anchor(SpriteAnchor.TOP_LEFT)
                .patternSize(width, height)
                .texSize(1, height)
                .mirror(SpriteMirror.VERTICAL)
                .alpha(alpha)
                .end();

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}