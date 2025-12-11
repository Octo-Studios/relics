package it.hurts.sskirillss.relics.client.post_effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.postEffects.PostEffect;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberrationManager;
import it.hurts.sskirillss.relics.items.relics.ring.RingOfTheSevenDeadlySinsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

public class ChromaticAberrationPostEffect extends PostEffect {
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void construct(PostChain postChain) {
        var player = MC.player;

        if (player == null)
            return;

        var totalStrength = 0F;
        var colors = new ArrayList<Integer>();

        for (var chromaticAberration : ChromaticAberrationManager.CHROMATIC_ABERRATIONS.values()) {
            var s = chromaticAberration.getStrength(player);

            if (s > 0F) {
                totalStrength += s;

                colors.addAll(chromaticAberration.getColors());
            }
        }

        if (colors.size() > 8)
            colors = new ArrayList<>(colors.subList(0, 8));

        var colorCount = colors.size();
        var colorArray = new float[24];

        for (var i = 0; i < colorCount; i++) {
            var c = colors.get(i);

            var r = ((c >> 16) & 0xFF) / 255F;
            var g = ((c >> 8) & 0xFF) / 255F;
            var b = (c & 0xFF) / 255F;

            var idx = i * 3;
            colorArray[idx]     = r;
            colorArray[idx + 1] = g;
            colorArray[idx + 2] = b;
        }

        postChain.setUniform("Strength", totalStrength);

        for (var postpass : postChain.passes) {
            postpass.getEffect().safeGetUniform("ColorCount").set(colorCount);

            postpass.getEffect().safeGetUniform("Colors").set(colorArray);
        }
    }

    @Override
    public boolean shouldRender() {
        return !ChromaticAberrationManager.CHROMATIC_ABERRATIONS.isEmpty();
    }

    @Override
    public ResourceLocation getPath() {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shaders/post/chromatic_aberration.json");
    }
}