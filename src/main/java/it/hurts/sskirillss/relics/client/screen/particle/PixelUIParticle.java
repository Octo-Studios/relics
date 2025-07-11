package it.hurts.sskirillss.relics.client.screen.particle;

import it.hurts.octostudios.octolib.client.particle.ExtendedUIParticle;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

public class PixelUIParticle extends ExtendedUIParticle {
    private static final Random RANDOM = new Random();

    public PixelUIParticle(Texture2D texture, float maxSpeed, int lifetime, float xStart, float yStart, Layer layer, float zOffset) {
        super(texture, maxSpeed, lifetime, xStart, yStart, layer, zOffset);
    }

    public PixelUIParticle(float maxSpeed, int maxLifetime, float xStart, float yStart, Layer layer, float zOffset) {
        super(new Texture2D(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/particle/pixel.png"),
                0, 0, 1, 1, 1, 1), maxSpeed, maxLifetime, xStart, yStart, layer, zOffset);
    }
}