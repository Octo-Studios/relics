package it.hurts.sskirillss.relics.client.post_effects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.post_effects.PostEffect;
import it.hurts.sskirillss.relics.items.relics.ring.RingOfTheSevenDeadlySinsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class SevenDeadlySinsPostEffect extends PostEffect {
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void construct(PostChain postChain) {
        var player = MC.player;

        if (player == null)
            return;

        postChain.setUniform("time", RingOfTheSevenDeadlySinsItem.getHurtTimer(player));
    }

    @Override
    public boolean shouldRender() {
        var player = MC.player;

        return player != null && RingOfTheSevenDeadlySinsItem.getHurtTimer(player) > 0;
    }

    @Override
    public ResourceLocation getPath() {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shaders/post/seven_deadly_sins.json");
    }
}
