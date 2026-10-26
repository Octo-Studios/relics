package it.hurts.sskirillss.relics.client.tooltip;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.screen.description.relic.widgets.BigRelicCardWidget;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TooltipParticles {
    private static class Particle {
        private float offset;
        private float distance;
        private float phase;
        private float direction;
        private float red;
        private float green;
        private float blue;
        private long spawnTime;
        private long lifetime;
    }

    private static final ResourceLocation PIXEL = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/particles/pixel.png");

    private static final Map<Item, Integer> ITEM_COLORS = new HashMap<>();
    private static final Map<ResourceLocation, NativeImage> FRAMES = new HashMap<>();
    private static final List<Particle> PARTICLES = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static final long SPAWN_INTERVAL = 80L;
    private static final long RESET_DELAY = 250L;
    private static final long LIFETIME = 2200L;
    private static final long LIFETIME_RANGE = 1400L;
    private static final int ALPHA_THRESHOLD = 128;

    private static final float SPAWN_DENSITY = 1F / 175F;
    private static final float TRIANGLE_HEIGHT = 56F;
    private static final float EDGE_HEIGHT = 0.3F;
    private static final float TOP_EDGE_OFFSET = 3F;
    private static final float BOTTOM_EDGE_OFFSET = 2F;
    private static final float DISTANCE_MIN = 0.6F;
    private static final float DRIFT = 1.5F;
    private static final float DRIFT_SPEED = 2F;
    private static final float FADE_IN = 0.15F;
    private static final float FADE_OUT_START = 0.35F;
    private static final float ALPHA = 0.85F;
    private static final float SATURATION_BOOST = 1.4F;
    private static final float SATURATION_MIN = 0.15F;
    private static final float BRIGHTNESS_MIN = 0.85F;

    private static Item lastItem = null;
    private static long lastSpawn = 0L;
    private static long lastRender = 0L;

    public static void render(GuiGraphics guiGraphics, ItemStack stack, ResourceLocation frame, int x, int y, int width, int height) {
        var now = Util.getMillis();

        var fallback = ITEM_COLORS.computeIfAbsent(stack.getItem(), item -> BigRelicCardWidget.getItemColor(stack));

        if (lastItem != stack.getItem() || now - lastRender > RESET_DELAY) {
            PARTICLES.clear();

            lastItem = stack.getItem();
            lastSpawn = now;

            for (var cycle = 0; cycle < (LIFETIME + LIFETIME_RANGE / 2) / SPAWN_INTERVAL; cycle++) {
                var count = getSpawnCount(width);

                for (var i = 0; i < count; i++) {
                    prewarm(now, -1F, frame, fallback, x, width);
                    prewarm(now, 1F, frame, fallback, x, width);
                }
            }
        }

        lastRender = now;

        if (now - lastSpawn >= SPAWN_INTERVAL) {
            lastSpawn = now;

            var count = getSpawnCount(width);

            for (var i = 0; i < count; i++) {
                PARTICLES.add(create(now, -1F, frame, fallback, x, width));
                PARTICLES.add(create(now, 1F, frame, fallback, x, width));
            }
        }

        PARTICLES.removeIf(particle -> now - particle.spawnTime >= particle.lifetime);

        var poseStack = guiGraphics.pose();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        for (var particle : PARTICLES) {
            var age = now - particle.spawnTime;
            var progress = age / (float) particle.lifetime;

            var fadeOut = Mth.clamp((progress - FADE_OUT_START) / (1F - FADE_OUT_START), 0F, 1F);
            var visibility = Math.min(progress / FADE_IN, 1F) * (1F - fadeOut * fadeOut * (3F - 2F * fadeOut));

            var posX = x + width * particle.offset + Mth.sin(particle.phase + age / 1000F * DRIFT_SPEED) * DRIFT;
            var posY = (particle.direction < 0F ? y - TOP_EDGE_OFFSET : y + height + BOTTOM_EDGE_OFFSET) + particle.direction * particle.distance * progress;

            RenderSystem.setShaderColor(particle.red, particle.green, particle.blue, ALPHA * visibility);

            GUIRenderer.begin(PIXEL, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(posX, posY)
                    .end();
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static int getSpawnCount(int width) {
        var expected = width * SPAWN_DENSITY;

        return (int) expected + (RANDOM.nextFloat() < expected - (int) expected ? 1 : 0);
    }

    private static void prewarm(long now, float direction, ResourceLocation frame, int fallback, int x, int width) {
        var particle = create(now, direction, frame, fallback, x, width);

        particle.spawnTime = now - (long) (RANDOM.nextFloat() * particle.lifetime);

        PARTICLES.add(particle);
    }

    private static Particle create(long now, float direction, ResourceLocation frame, int fallback, int x, int width) {
        var particle = new Particle();

        var offset = RANDOM.nextFloat();
        var center = 1F - Math.abs(2F * offset - 1F);

        particle.offset = offset;
        particle.distance = TRIANGLE_HEIGHT * (EDGE_HEIGHT + (1F - EDGE_HEIGHT) * center) * (DISTANCE_MIN + RANDOM.nextFloat() * (1F - DISTANCE_MIN));
        particle.phase = RANDOM.nextFloat() * Mth.TWO_PI;
        particle.direction = direction;
        particle.spawnTime = now;
        particle.lifetime = LIFETIME + (long) (RANDOM.nextFloat() * LIFETIME_RANGE);

        var sampled = sampleFrame(frame, getFrameColumn(x, width, x + width * offset), direction < 0F);
        var source = sampled < 0 ? fallback : sampled;
        var hsb = Color.RGBtoHSB((source >> 16) & 0xFF, (source >> 8) & 0xFF, source & 0xFF, null);
        var color = Color.HSBtoRGB(hsb[0], Mth.clamp(hsb[1] * SATURATION_BOOST + SATURATION_MIN, 0F, 1F), Math.max(hsb[2], BRIGHTNESS_MIN));

        particle.red = ((color >> 16) & 0xFF) / 255F;
        particle.green = ((color >> 8) & 0xFF) / 255F;
        particle.blue = (color & 0xFF) / 255F;

        return particle;
    }

    private static int getFrameColumn(int x, int width, float position) {
        var middle = x + (width - TooltipBorderHandler.MIDDLE_WIDTH) / 2;

        if (position >= middle && position < middle + TooltipBorderHandler.MIDDLE_WIDTH)
            return TooltipBorderHandler.CORNER_WIDTH + (int) (position - middle);

        var left = x - TooltipBorderHandler.CORNER_WIDTH / 2 - TooltipBorderHandler.CORNER_MARGIN;

        if (position >= left && position < left + TooltipBorderHandler.CORNER_WIDTH)
            return (int) (position - left);

        var right = x + width - TooltipBorderHandler.CORNER_WIDTH / 2 + TooltipBorderHandler.CORNER_MARGIN;

        if (position >= right && position < right + TooltipBorderHandler.CORNER_WIDTH)
            return TooltipBorderHandler.PATTERN_WIDTH - TooltipBorderHandler.CORNER_WIDTH + (int) (position - right);

        return -1;
    }

    private static int sampleFrame(ResourceLocation frame, int column, boolean top) {
        var image = FRAMES.computeIfAbsent(frame, TooltipParticles::loadFrame);

        if (image == null || column < 0 || column >= image.getWidth())
            return -1;

        for (var i = 0; i < TooltipBorderHandler.CORNER_HEIGHT; i++) {
            var row = top ? TooltipBorderHandler.CORNER_HEIGHT - 1 - i : TooltipBorderHandler.PATTERN_HEIGHT - TooltipBorderHandler.CORNER_HEIGHT + i;
            var pixel = image.getPixelRGBA(column, row);

            if (FastColor.ABGR32.alpha(pixel) >= ALPHA_THRESHOLD)
                return (FastColor.ABGR32.red(pixel) << 16) | (FastColor.ABGR32.green(pixel) << 8) | FastColor.ABGR32.blue(pixel);
        }

        return -1;
    }

    private static NativeImage loadFrame(ResourceLocation frame) {
        var resource = Minecraft.getInstance().getResourceManager().getResource(frame);

        if (resource.isEmpty())
            return null;

        try (var stream = resource.get().open()) {
            return NativeImage.read(stream);
        } catch (IOException exception) {
            return null;
        }
    }
}
