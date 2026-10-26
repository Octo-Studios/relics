package it.hurts.sskirillss.relics.client.tooltip;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.utility.TooltipDisplayEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.init.RelicsRelicStyles;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

@EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
public class TooltipBorderHandler {
    public static final int PATTERN_WIDTH = 160;
    public static final int PATTERN_HEIGHT = 64;
    public static final int CORNER_WIDTH = 32;
    public static final int CORNER_HEIGHT = 32;
    public static final int CORNER_MARGIN = 3;
    public static final int MIDDLE_WIDTH = 96;
    public static final int MIDDLE_HEIGHT = CORNER_HEIGHT;

    private static final long GLINT_PERIOD = 2800L;
    private static final float GLINT_ACTIVE = 0.4F;
    private static final float GLINT_WIDTH = 6F;
    private static final float GLINT_STRENGTH = 0.9F;
    private static final float BREATH_SATURATION = 0.4F;
    private static final float BREATH_BRIGHTNESS = 0.12F;
    private static final float BREATH_SPEED = 2F;

    @SubscribeEvent
    public static void onTooltipDisplay(TooltipDisplayEvent event) {
        var player = Minecraft.getInstance().player;

        if (player == null)
            return;

        var stack = event.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var optional = RelicsRelicStyles.getStyle(stack.getItem());

        if (optional.isEmpty())
            return;

        var style = optional.get();

        var frameTexture = style.getTooltipFrameTexture(player, stack);
        var starTexture = style.getTooltipStarTexture(player, stack);

        if (frameTexture == null || starTexture == null)
            return;

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, frameTexture);

        Minecraft.getInstance().getTextureManager().getTexture(frameTexture).bind();

        int texWidth = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int texHeight = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

        if (texHeight == 16 || texWidth == 16)
            return;

        var graphics = event.getGraphics();
        var poseStack = graphics.pose();

        var width = event.getWidth();
        var height = event.getHeight();

        var x = event.getX();
        var y = event.getY();

        TooltipParticles.render(graphics, stack, frameTexture, x, y, width, height);

        poseStack.pushPose();

        RenderSystem.enableBlend();

        poseStack.translate(0, 0, 410.0);

        var animation = AnimationData.fromMcmeta(ResourceLocation.fromNamespaceAndPath(Relics.MODID, frameTexture.getPath() + ".mcmeta"));

        int frame = animation.getFrameByTime(player.tickCount).getKey();

        int offset = PATTERN_HEIGHT * frame;

        graphics.blit(frameTexture, x - CORNER_WIDTH / 2 - CORNER_MARGIN, y - CORNER_HEIGHT / 2 - CORNER_MARGIN, 0, offset, CORNER_WIDTH, CORNER_HEIGHT, texWidth, texHeight);
        graphics.blit(frameTexture, x + width - CORNER_WIDTH / 2 + CORNER_MARGIN, y - CORNER_HEIGHT / 2 - CORNER_MARGIN, PATTERN_WIDTH - CORNER_WIDTH, offset, CORNER_WIDTH, CORNER_HEIGHT, texWidth, texHeight);

        graphics.blit(frameTexture, x - CORNER_WIDTH / 2 - CORNER_MARGIN, y + height - CORNER_HEIGHT / 2 + CORNER_MARGIN, 0, (PATTERN_HEIGHT - CORNER_HEIGHT) + offset, CORNER_WIDTH, CORNER_HEIGHT, texWidth, texHeight);
        graphics.blit(frameTexture, x + width - CORNER_WIDTH / 2 + CORNER_MARGIN, y + height - CORNER_HEIGHT / 2 + CORNER_MARGIN, PATTERN_WIDTH - CORNER_WIDTH, (PATTERN_HEIGHT - CORNER_HEIGHT) + offset, CORNER_WIDTH, CORNER_HEIGHT, texWidth, texHeight);

        graphics.blit(frameTexture, x + (width - MIDDLE_WIDTH) / 2, y - MIDDLE_HEIGHT + 1, CORNER_WIDTH, offset, MIDDLE_WIDTH, MIDDLE_HEIGHT, texWidth, texHeight);
        graphics.blit(frameTexture, x + (width - MIDDLE_WIDTH) / 2, y + height - 1, CORNER_WIDTH, MIDDLE_HEIGHT + offset, MIDDLE_WIDTH, MIDDLE_HEIGHT, texWidth, texHeight);

        RenderSystem.setShaderTexture(0, starTexture);

        var xOff = 0;

        var quality = relic.getRelicData(player, stack).calculateQuality();
        var glintCenter = -GLINT_WIDTH + (Util.getMillis() % GLINT_PERIOD) / (float) GLINT_PERIOD / GLINT_ACTIVE * (quality * 3F + 2F * GLINT_WIDTH);

        for (int i = 1; i < quality + 1; i++) {
            var isAliquot = i % 2 == 1;

            var glint = Mth.clamp(1F - Math.abs(xOff + (isAliquot ? 1.5F : 1F) - glintCenter) / GLINT_WIDTH, 0F, 1F) * GLINT_STRENGTH;

            var color = (float) (1F + Math.sin(player.tickCount * i * 0.05F) * 0.1F) + glint;

            RenderSystem.setShaderColor(color, color, color, 1F);

            graphics.blit(starTexture, x + width / 2 - 14 + xOff, y - 10, (isAliquot ? 0 : 3), 0, isAliquot ? 3 : 2, 5, 5, 5);

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            xOff += 3;
        }

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    private static int breathe(int argb, float breath) {
        var hsb = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);

        var saturation = Mth.clamp(hsb[1] * Mth.lerp(breath, 1F - BREATH_SATURATION, 1F + BREATH_SATURATION), 0F, 1F);
        var brightness = Mth.clamp(hsb[2] * Mth.lerp(breath, 1F - BREATH_BRIGHTNESS, 1F + BREATH_BRIGHTNESS), 0F, 1F);

        return (argb & 0xFF000000) | (Color.HSBtoRGB(hsb[0], saturation, brightness) & 0xFFFFFF);
    }

    @SubscribeEvent
    public static void onTooltipColorEvent(RenderTooltipEvent.Color event) {
        var stack = event.getItemStack();
        var player = Minecraft.getInstance().player;

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var optional = RelicsRelicStyles.getStyle(relic.getItem());

        if (optional.isEmpty())
            return;

        var style = optional.get();

        var topBorder = style.getTopTooltipBorderColor(player, stack);
        var bottomBorder = style.getBottomTooltipBorderColor(player, stack);

        if (topBorder != null)
            event.setBorderStart(topBorder.getARGB());
        if (bottomBorder != null)
            event.setBorderEnd(bottomBorder.getARGB());

        var breath = (Mth.sin(Util.getMillis() / 1000F * BREATH_SPEED) + 1F) / 2F;

        event.setBorderStart(breathe(event.getBorderStart(), breath));
        event.setBorderEnd(breathe(event.getBorderEnd(), breath));

        var topBackground = style.getTopTooltipBackgroundColor(player, stack);
        var bottomBackground = style.getBottomTooltipBackgroundColor(player, stack);

        if (topBackground != null)
            event.setBackgroundStart(topBackground.getARGB());
        if (bottomBackground != null)
            event.setBackgroundEnd(bottomBackground.getARGB());
    }
}