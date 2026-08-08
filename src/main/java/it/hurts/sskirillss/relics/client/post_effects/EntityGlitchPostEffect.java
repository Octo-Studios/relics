package it.hurts.sskirillss.relics.client.post_effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.post_effects.PostEffect;
import it.hurts.sskirillss.relics.api.post_effects.RenderStage;
import it.hurts.sskirillss.relics.client.post_effects.misc.EntityGlitchMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class EntityGlitchPostEffect extends PostEffect {
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void construct(PostChain postChain) {
        var player = MC.player;

        if (player == null)
            return;

        postChain.setUniform("time", player.tickCount + MC.getTimer().getGameTimeDeltaTicks());

        var mask = EntityGlitchMask.getTarget();

        if (mask == null)
            return;

        for (var postpass : postChain.passes)
            postpass.getEffect().setSampler("MaskSampler", mask::getColorTextureId);
    }

    @Override
    public boolean shouldRender() {
        return MC.player != null && EntityGlitchMask.hasRenderedThisFrame();
    }

    @Override
    public ResourceLocation getPath() {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shaders/post/entity_glitch.json");
    }

    @Override
    public RenderStage getStage() {
        return RenderStage.LEVEL;
    }
}
