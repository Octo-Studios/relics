package it.hurts.sskirillss.relics.client.postEffects;

import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.postEffects.PostEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class SevenDeadlySinsPostEffect extends PostEffect {
    private static final Minecraft MC = Minecraft.getInstance();

    public static int TIMER = 0;

    @Override
    public void construct(PostChain postChain) {
        var player = MC.player;

        if (player == null)
            return;

        postChain.setUniform("time", TIMER);
    }

    @Override
    public boolean shouldRender() {
        return SevenDeadlySinsPostEffect.TIMER > 0;
    }

    @Override
    public ResourceLocation getPath() {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shaders/post/seven_deadly_sins.json");
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide())
            return;

        if (SevenDeadlySinsPostEffect.TIMER > 0)
            SevenDeadlySinsPostEffect.TIMER--;
    }
}