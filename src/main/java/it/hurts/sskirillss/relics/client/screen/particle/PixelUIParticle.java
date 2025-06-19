package it.hurts.sskirillss.relics.client.screen.particle;

import it.hurts.octostudios.octolib.client.particle.UIParticle;
import it.hurts.octostudios.octolib.util.OctoColor;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2f;

import java.util.Random;

public class PixelUIParticle extends UIParticle {
    private static final Random RANDOM = new Random();

    public PixelUIParticle(float maxSpeed, int maxLifetime, float xStart, float yStart, Layer layer, float zOffset) {
        super(new Texture2D(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/particles/pixel.png"),
                0, 0, 1, 1, 1, 1), maxSpeed, maxLifetime, xStart, yStart, layer, zOffset);

        this.angularVelocity = 120;
        this.speed = RANDOM.nextFloat() * 2 * maxSpeed;
        this.endColor = new OctoColor(0F, 1F, 1F, 0F);
        this.direction = new Vector2f(RANDOM.nextFloat() - 0.5f, RANDOM.nextFloat() - 0.5f).normalize();
        this.resizeWithLifetime = false;
        this.size = 1F;
    }

    @Override
    public void tick(double dt) {
        super.tick(dt);
    }
}