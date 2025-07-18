package it.hurts.sskirillss.relics.client.screen.particle;

import it.hurts.octostudios.octolib.client.particle.ExtendedUIParticle;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.resources.ResourceLocation;

public class PixelUIParticle extends ExtendedUIParticle {
    public PixelUIParticle(float maxSpeed, int maxLifetime, float xStart, float yStart, Layer layer, float zOffset) {
        super(new Texture2D(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/particle/pixel.png"),
                0, 0, 1, 1, 1, 1), maxSpeed, maxLifetime, xStart, yStart, layer, zOffset);
    }
}