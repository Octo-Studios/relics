package it.hurts.sskirillss.relics.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.init.RelicsCoreShaders;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.List;

public class RenderUtils {
    public static float getPartialTick(boolean ignorePause){
        var tracker = Minecraft.getInstance().getTimer();

        if (ignorePause && tracker instanceof DeltaTracker.Timer timer)
            return timer.deltaTickResidual;

        return tracker.getGameTimeDeltaPartialTick(true);
    }

    public static void drawOutlinedText(GuiGraphics guiGraphics, MutableComponent text, float x, float y, int textColor, int outlineColor) {
        Font font = Minecraft.getInstance().font;

        ScreenUtils.drawCenteredString(guiGraphics, font, text, x + 1, y, outlineColor, false);
        ScreenUtils.drawCenteredString(guiGraphics, font, text, x - 1, y, outlineColor, false);
        ScreenUtils.drawCenteredString(guiGraphics, font, text, x, y + 1, outlineColor, false);
        ScreenUtils.drawCenteredString(guiGraphics, font, text, x, y - 1, outlineColor, false);

        ScreenUtils.drawCenteredString(guiGraphics, font, text, x, y, textColor, false);
    }

    public static void renderRevealingPanel(PoseStack matrices, float x, float y, float sizeX, float sizeY, List<Vector2f> points, List<Float> revealRadiuses, List<Float> noiseSpreads, float time) {
        RenderSystem.enableBlend();

        float[] arr = new float[256];
        float[] radiuses = new float[128];
        float[] noiseSpreadsArr = new float[128];

        for (int i = 0; i < arr.length; i += 2) {

            float lmx;
            float lmy;

            if (i / 2 < points.size()) {
                Vector2f v = points.get(i / 2);
                lmx = (v.x - x) / sizeX;
                lmy = (v.y - y) / sizeY;

                radiuses[i / 2] = revealRadiuses.get(i / 2);
                noiseSpreadsArr[i / 2] = noiseSpreads.get(i / 2);


            } else {
                radiuses[i / 2] = 0.0001f;
                noiseSpreadsArr[i / 2] = 0.000001f;
                lmx = -100;
                lmy = -100;
            }
            arr[i] = lmx;
            arr[i + 1] = lmy;
        }


        Matrix4f mat = matrices.last().pose();


        RenderSystem.setShader(() -> RelicsCoreShaders.REVEAL_SHADER);
        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("revealRadiuses").set(radiuses);
        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("noiseSpreads").set(noiseSpreadsArr);
        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("positions").set(arr);
        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("pixelCount").set(110F);


        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("greenRadius").set(0.035f);
        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("size").set(sizeX, sizeY);
        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("time").set(time);
        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("col1").set(0F, 0F, 1F);
        RelicsCoreShaders.REVEAL_SHADER.safeGetUniform("col2").set(1F, 0F, 0F);


        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        builder.addVertex(mat, x, y, 0).setUv(0, 0);
        builder.addVertex(mat, x + sizeX, y, 0).setUv(1, 0);
        builder.addVertex(mat, x + sizeX, y + sizeY, 0).setUv(1, 1);
        builder.addVertex(mat, x, y + sizeY, 0).setUv(0, 1);
        builder.addVertex(mat, x, y + sizeY, 0).setUv(0, 1);
        builder.addVertex(mat, x + sizeX, y + sizeY, 0).setUv(1, 1);
        builder.addVertex(mat, x + sizeX, y, 0).setUv(1, 0);
        builder.addVertex(mat, x, y, 0).setUv(0, 0);


        BufferUploader.drawWithShader(builder.buildOrThrow());

        RenderSystem.disableBlend();
    }


    public static void renderAnimatedTextureFromCenter(PoseStack matrix, float centerX, float centerY, float texWidth, float texHeight, float patternWidth, float patternHeight, float scale, AnimationData animation) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null)
            return;

        renderAnimatedTextureFromCenter(matrix, centerX, centerY, texWidth, texHeight, patternWidth, patternHeight, scale, animation, level.getGameTime());
    }

    public static void renderAnimatedTextureFromCenter(PoseStack matrix, float centerX, float centerY, float texWidth, float texHeight, float patternWidth, float patternHeight, float scale, AnimationData animation, long ticks) {
        Pair<Integer, Integer> pair = animation.getFrameByTime(ticks);

        renderTextureFromCenter(matrix, centerX, centerY, 0, patternHeight * pair.getKey(), texWidth, texHeight, patternWidth, patternHeight, scale);
    }

    public static void renderTextureFromCenter(PoseStack matrix, float centerX, float centerY, float width, float height, float scale) {
        renderTextureFromCenter(matrix, centerX, centerY, 0, 0, width, height, width, height, scale);
    }

    public static void renderTextureFromCenter(PoseStack matrix, float centerX, float centerY, float texOffX, float texOffY, float texWidth, float texHeight, float width, float height, float scale) {
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        matrix.pushPose();

        matrix.translate(centerX, centerY, 0);
        matrix.scale(scale, scale, scale);

        Matrix4f m = matrix.last().pose();

        float u1 = texOffX / texWidth;
        float u2 = (texOffX + width) / texWidth;
        float v1 = texOffY / texHeight;
        float v2 = (texOffY + height) / texHeight;

        float w2 = width / 2F;
        float h2 = height / 2F;

        builder.addVertex(m, -w2, +h2, 0).setUv(u1, v2);
        builder.addVertex(m, +w2, +h2, 0).setUv(u2, v2);
        builder.addVertex(m, +w2, -h2, 0).setUv(u2, v1);
        builder.addVertex(m, -w2, -h2, 0).setUv(u1, v1);

        matrix.popPose();

        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    public static void renderFlatBeam(GuiGraphics guiGraphics, float partialTicks, float length, float width, int startColor, int endColor) {
        var builder = guiGraphics.bufferSource().getBuffer(RenderType.dragonRays());
        var matrix4f = guiGraphics.pose().last().pose();

        int startRed = (startColor >> 16) & 0xFF;
        int startGreen = (startColor >> 8) & 0xFF;
        int startBlue = startColor & 0xFF;
        int startAlpha = Mth.clamp((int) (((startColor >> 24) & 0xFF) * (1F - partialTicks / 200F)), 0, 255);

        int endRed = (endColor >> 16) & 0xFF;
        int endGreen = (endColor >> 8) & 0xFF;
        int endBlue = endColor & 0xFF;
        int endAlpha = Mth.clamp((int) (((endColor >> 24) & 0xFF) * (1F - partialTicks / 200F)), 0, 255);

        builder.addVertex(matrix4f, 0F, 0F, 0F).setColor(startRed, startGreen, startBlue, startAlpha);
        builder.addVertex(matrix4f, width / 2F, length, 0F).setColor(endRed, endGreen, endBlue, endAlpha);
        builder.addVertex(matrix4f, -width / 2F, length, 0F).setColor(endRed, endGreen, endBlue, endAlpha);

        builder.addVertex(matrix4f, 0F, 0F, 0F).setColor(startRed, startGreen, startBlue, startAlpha);
        builder.addVertex(matrix4f, -width / 2F, length, 0F).setColor(endRed, endGreen, endBlue, endAlpha);
        builder.addVertex(matrix4f, width / 2F, length, 0F).setColor(endRed, endGreen, endBlue, endAlpha);
    }
}