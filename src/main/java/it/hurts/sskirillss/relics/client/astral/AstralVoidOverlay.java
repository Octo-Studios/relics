package it.hurts.sskirillss.relics.client.astral;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.hurts.sskirillss.relics.init.RelicsCoreShaders;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

public class AstralVoidOverlay {
    private static final Minecraft MC = Minecraft.getInstance();

    public static void render() {
        var coverage = AstralSpace.getCoverage();
        var shader = RelicsCoreShaders.ASTRAL_VOID_SHADER;

        if (coverage <= 0.001F || shader == null || AstralSceneBuffer.getTexture() == 0)
            return;

        var window = MC.getWindow();
        var oldProjection = RenderSystem.getProjectionMatrix();
        var oldSorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();

        RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.ORTHOGRAPHIC_Z);

        modelView.pushMatrix();
        modelView.identity();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, AstralSceneBuffer.getTexture());

        shader.safeGetUniform("Coverage").set(coverage);
        shader.safeGetUniform("Time").set(AstralSpace.getTime());
        shader.safeGetUniform("Aspect").set((float) window.getWidth() / window.getHeight());

        var builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        builder.addVertex(-1F, -1F, 0F).setUv(0F, 0F);
        builder.addVertex(1F, -1F, 0F).setUv(1F, 0F);
        builder.addVertex(1F, 1F, 0F).setUv(1F, 1F);
        builder.addVertex(-1F, 1F, 0F).setUv(0F, 1F);

        BufferUploader.drawWithShader(builder.buildOrThrow());

        modelView.popMatrix();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
    }
}
