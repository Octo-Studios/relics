package it.hurts.sskirillss.relics.client.post_effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.postEffects.PostEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class LensPostEffect extends PostEffect {
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void construct(PostChain postChain) {
        var mouseHandler = MC.mouseHandler;

        var player = MC.player;

        postChain.setUniform("mouseX", (float) mouseHandler.xpos());
        postChain.setUniform("mouseY", (float) mouseHandler.ypos());
    }

    @Override
    public boolean shouldRender() {
        return false;
    }

    @Override
    public ResourceLocation getPath() {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shaders/post/lens.json");
    }
}