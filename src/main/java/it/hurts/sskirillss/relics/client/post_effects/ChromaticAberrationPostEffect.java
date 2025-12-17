package it.hurts.sskirillss.relics.client.post_effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.post_effects.PostEffect;
import it.hurts.sskirillss.relics.api.post_effects.RenderStage;
import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberrationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class ChromaticAberrationPostEffect extends PostEffect {
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void construct(PostChain postChain) {
        var player = MC.player;

        if (player == null)
            return;

        var totalStrength = 0F;

        for (var chromaticAberration : ChromaticAberrationManager.CHROMATIC_ABERRATIONS.values()) {
            var s = chromaticAberration.getStrength(player);

            if (s > 0F)
                totalStrength += s;
        }

        postChain.setUniform("Strength", totalStrength);
    }

    @Override
    public boolean shouldRender() {
        return !ChromaticAberrationManager.CHROMATIC_ABERRATIONS.isEmpty();
    }

    @Override
    public ResourceLocation getPath() {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shaders/post/chromatic_aberration.json");
    }

    @Override
    public RenderStage getStage() {
        return RenderStage.LEVEL;
    }
}