package it.hurts.sskirillss.relics.client.screen.utils.misc;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.hurts.sskirillss.relics.init.RelicsCoreShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

class FracturedRelicRenderer {
    static final int CRACK_COUNT = 5;
    private final float[] cracks = new float[CRACK_COUNT * 2];
    private TextureTarget target;
    private boolean frozen;

    public boolean render(GuiGraphics graphics, ItemStack stack, float stress, float impact, float burst) {
        var shader = RelicsCoreShaders.RELIC_FRACTURE_SHADER;

        if (shader == null)
            return false;

        graphics.flush();

        if (!frozen) {
            captureItem(graphics, stack);

            frozen = burst > 0F;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, target.getColorTextureId());

        shader.safeGetUniform("Stress").set(stress);
        shader.safeGetUniform("Impact").set(impact);
        shader.safeGetUniform("Burst").set(burst);
        shader.safeGetUniform("Cracks").set(cracks);

        var pose = graphics.pose().last().pose();
        var quad = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        quad.addVertex(pose, -32F, -32F, 0F).setUv(0F, 0F);
        quad.addVertex(pose, -32F, 32F, 0F).setUv(0F, 1F);
        quad.addVertex(pose, 32F, 32F, 0F).setUv(1F, 1F);
        quad.addVertex(pose, 32F, -32F, 0F).setUv(1F, 0F);

        BufferUploader.drawWithShader(quad.buildOrThrow());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        return true;
    }

    private void captureItem(GuiGraphics graphics, ItemStack stack) {
        var minecraft = Minecraft.getInstance();
        var items = minecraft.getItemRenderer();
        var model = items.getModel(stack, minecraft.level, minecraft.player, 0);
        var sprite = model.getParticleIcon(ModelData.EMPTY).contents();
        var width = sprite.width() * 2;
        var height = sprite.height() * 2;

        if (target == null) {
            target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            target.setClearColor(0F, 0F, 0F, 0F);

            var random = minecraft.player.getRandom();
            var rotation = random.nextFloat() * Mth.TWO_PI;

            for (var i = 0; i < CRACK_COUNT; i++) {
                var angle = rotation + (i + Mth.randomBetween(random, -0.25F, 0.25F)) * Mth.TWO_PI / CRACK_COUNT;
                var length = Mth.randomBetween(random, 0.74F, 1F);

                cracks[2 * i] = Mth.cos(angle) * length;
                cracks[2 * i + 1] = Mth.sin(angle) * length;
            }
        } else if (target.width != width || target.height != height)
            target.resize(width, height, Minecraft.ON_OSX);

        var projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var sorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();
        var pose = graphics.pose();

        modelView.pushMatrix();

        pose.pushPose();

        try {
            modelView.identity();

            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(-16F, 16F, 16F, -16F, -1000F, 1000F), VertexSorting.ORTHOGRAPHIC_Z);

            target.clear(Minecraft.ON_OSX);
            target.bindWrite(true);

            pose.setIdentity();
            pose.scale(16F, -16F, 16F);

            if (!model.usesBlockLight())
                Lighting.setupForFlatItems();

            items.render(stack, ItemDisplayContext.GUI, false, pose, graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);

            graphics.flush();
        } finally {
            if (!model.usesBlockLight())
                Lighting.setupFor3DItems();

            pose.popPose();

            modelView.popMatrix();

            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection, sorting);

            minecraft.getMainRenderTarget().bindWrite(true);
        }
    }

    public void close() {
        frozen = false;

        if (target != null) {
            target.destroyBuffers();

            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

            target = null;
        }
    }
}
