package it.hurts.sskirillss.relics.client.tooltip;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.utility.TooltipDisplayEvent;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

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

    private static final Map<ResourceLocation, NativeImage> FRAMES = new HashMap<>();
    private static final List<Particle> PARTICLES = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static final long SPAWN_INTERVAL = 80L;
    private static final long RESET_DELAY = 250L;
    private static final long LIFETIME = 2200L;
    private static final long LIFETIME_RANGE = 1400L;

    private static final float SPAWN_DENSITY = 1F / 175F;
    private static final float TRIANGLE_HEIGHT = 56F;
    private static final float EDGE_HEIGHT = 0.3F;
    private static final int TOP_EDGE_OFFSET = TooltipRenderUtil.PADDING_TOP;
    private static final int BOTTOM_EDGE_OFFSET = TooltipRenderUtil.PADDING_BOTTOM - 1;
    private static final float DISTANCE_MIN = 0.6F;
    private static final float DRIFT = 1.5F;
    private static final float DRIFT_SPEED = 2F;
    private static final float FADE_IN = 0.15F;
    private static final float FADE_OUT_START = 0.35F;
    private static final float ALPHA = 0.85F;

    private static Item lastItem = null;
    private static long lastSpawn = 0L;
    private static long lastRender = 0L;

    public static void render(TooltipDisplayEvent event, ResourceLocation frame, int frameOffset) {
        var now = Util.getMillis();
        var item = event.getStack().getItem();
        var image = FRAMES.computeIfAbsent(frame, TooltipParticles::loadFrame);
        var width = event.getWidth();
        var topColor = event.getBorderStart();
        var bottomColor = event.getBorderEnd();

        if (lastItem != item || now - lastRender > RESET_DELAY) {
            PARTICLES.clear();

            lastItem = item;
            lastSpawn = now;

            for (var cycle = 0; cycle < (LIFETIME + LIFETIME_RANGE / 2) / SPAWN_INTERVAL; cycle++) {
                var count = getSpawnCount(width);

                for (var i = 0; i < count; i++) {
                    prewarm(now, -1F, image, frameOffset, topColor, width);
                    prewarm(now, 1F, image, frameOffset, bottomColor, width);
                }
            }
        }

        lastRender = now;

        if (now - lastSpawn >= SPAWN_INTERVAL) {
            lastSpawn = now;

            var count = getSpawnCount(width);

            for (var i = 0; i < count; i++) {
                PARTICLES.add(create(now, -1F, image, frameOffset, topColor, width));
                PARTICLES.add(create(now, 1F, image, frameOffset, bottomColor, width));
            }
        }

        PARTICLES.removeIf(particle -> now - particle.spawnTime >= particle.lifetime);

        var poseStack = event.getGraphics().pose();
        var x = event.getX();
        var y = event.getY();
        var height = event.getHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (var particle : PARTICLES) {
            var age = now - particle.spawnTime;
            var progress = age / (float) particle.lifetime;

            var fadeOut = Mth.clamp((progress - FADE_OUT_START) / (1F - FADE_OUT_START), 0F, 1F);
            var fadeIn = Math.min(progress / FADE_IN, 1F);
            var visibility = fadeIn * (1F - fadeOut * fadeOut * (3F - 2F * fadeOut));

            var posX = x + (int) (width * particle.offset) + Mth.sin(particle.phase + age / 1000F * DRIFT_SPEED) * DRIFT * fadeIn;
            var posY = (particle.direction < 0F ? y - TOP_EDGE_OFFSET : y + height + BOTTOM_EDGE_OFFSET) + particle.direction * particle.distance * progress;

            GUIRenderer.begin(PIXEL, poseStack)
                    .color(particle.red, particle.green, particle.blue, ALPHA * visibility)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(posX, posY)
                    .end();
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    static void clearFrameCache() {
        FRAMES.values().forEach(NativeImage::close);
        FRAMES.clear();
    }

    private static int getSpawnCount(int width) {
        var expected = width * SPAWN_DENSITY;

        return (int) expected + (RANDOM.nextFloat() < expected - (int) expected ? 1 : 0);
    }

    private static void prewarm(long now, float direction, NativeImage image, int frameOffset, int borderColor, int width) {
        var particle = create(now, direction, image, frameOffset, borderColor, width);

        particle.spawnTime = now - (long) (RANDOM.nextFloat() * particle.lifetime);

        PARTICLES.add(particle);
    }

    private static Particle create(long now, float direction, NativeImage image, int frameOffset, int borderColor, int width) {
        var particle = new Particle();

        var offset = RANDOM.nextFloat();
        var center = 1F - Math.abs(2F * offset - 1F);

        particle.offset = offset;
        particle.distance = TRIANGLE_HEIGHT * (EDGE_HEIGHT + (1F - EDGE_HEIGHT) * center) * (DISTANCE_MIN + RANDOM.nextFloat() * (1F - DISTANCE_MIN));
        particle.phase = RANDOM.nextFloat() * Mth.TWO_PI;
        particle.direction = direction;
        particle.spawnTime = now;
        particle.lifetime = LIFETIME + (long) (RANDOM.nextFloat() * LIFETIME_RANGE);

        var color = sampleFrame(image, frameOffset, width, (int) (width * offset), direction < 0F, borderColor);
        particle.red = FastColor.ARGB32.red(color) / 255F;
        particle.green = FastColor.ARGB32.green(color) / 255F;
        particle.blue = FastColor.ARGB32.blue(color) / 255F;

        return particle;
    }

    private static int sampleFrame(NativeImage image, int frameOffset, int width, int column, boolean top, int color) {
        if (image == null || image.getWidth() < TooltipBorderHandler.PATTERN_WIDTH || frameOffset + TooltipBorderHandler.PATTERN_HEIGHT > image.getHeight())
            return color;

        var cornerRow = top
                ? TooltipBorderHandler.CORNER_HEIGHT / 2 + TooltipBorderHandler.CORNER_MARGIN - TOP_EDGE_OFFSET
                : TooltipBorderHandler.PATTERN_HEIGHT - TooltipBorderHandler.CORNER_HEIGHT / 2 - TooltipBorderHandler.CORNER_MARGIN + BOTTOM_EDGE_OFFSET;
        var left = column + TooltipBorderHandler.CORNER_WIDTH / 2 + TooltipBorderHandler.CORNER_MARGIN;
        var right = column - width + TooltipBorderHandler.CORNER_WIDTH / 2 - TooltipBorderHandler.CORNER_MARGIN;
        var middle = column - (width - TooltipBorderHandler.MIDDLE_WIDTH) / 2;

        if (left < TooltipBorderHandler.CORNER_WIDTH)
            color = samplePixel(image, left, frameOffset + cornerRow, color);

        if (right >= 0)
            color = samplePixel(image, TooltipBorderHandler.PATTERN_WIDTH - TooltipBorderHandler.CORNER_WIDTH + right, frameOffset + cornerRow, color);

        if (middle >= 0 && middle < TooltipBorderHandler.MIDDLE_WIDTH) {
            var middleRow = top ? TooltipBorderHandler.MIDDLE_HEIGHT - 1 - TOP_EDGE_OFFSET : TooltipBorderHandler.MIDDLE_HEIGHT + 1 + BOTTOM_EDGE_OFFSET;
            color = samplePixel(image, TooltipBorderHandler.CORNER_WIDTH + middle, frameOffset + middleRow, color);
        }

        return color;
    }

    private static int samplePixel(NativeImage image, int column, int row, int color) {
        var pixel = image.getPixelRGBA(column, row);
        var alpha = FastColor.ABGR32.alpha(pixel);
        var argb = FastColor.ARGB32.color(alpha, FastColor.ABGR32.red(pixel), FastColor.ABGR32.green(pixel), FastColor.ABGR32.blue(pixel));

        return FastColor.ARGB32.lerp(alpha / 255F, color, argb);
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
