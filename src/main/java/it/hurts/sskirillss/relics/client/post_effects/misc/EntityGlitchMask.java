package it.hurts.sskirillss.relics.client.post_effects.misc;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class EntityGlitchMask {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final ByteBufferBuilder MASK_BUFFER = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final MultiBufferSource.BufferSource BUFFER_SOURCE = MultiBufferSource.immediate(MASK_BUFFER);

    private static TextureTarget target;
    private static boolean renderingMask;
    private static boolean depthCopied;
    private static boolean renderedThisFrame;

    public static void beginFrame() {
        var mask = getTarget();

        if (mask == null)
            return;

        var window = MC.getWindow();

        if (mask.width != window.getWidth() || mask.height != window.getHeight())
            mask.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);

        mask.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        mask.clear(Minecraft.ON_OSX);

        depthCopied = false;
        renderedThisFrame = false;

        MC.getMainRenderTarget().bindWrite(false);
    }

    public static boolean hasRenderedThisFrame() {
        return renderedThisFrame;
    }

    public static TextureTarget getTarget() {
        var window = MC.getWindow();

        if (target == null)
            target = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);

        return target;
    }

    public static boolean shouldRender(LivingEntity entity) {
        return !renderingMask && MC.player != null && entity.isAlive() && entity.hasEffect(RelicsMobEffects.GLITCH);
    }

    public static <T extends LivingEntity, M extends EntityModel<T>> void render(T entity, M model, List<RenderLayer<T, M>> layers, ResourceLocation texture, PoseStack poseStack, int packedLight, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var mask = getTarget();

        if (mask == null || !shouldRender(entity))
            return;

        var main = MC.getMainRenderTarget();

        renderingMask = true;

        try {
            if (!depthCopied) {
                mask.copyDepthFrom(main);
                depthCopied = true;
            }

            mask.bindWrite(false);

            var buffer = BUFFER_SOURCE;
            var vertexConsumer = buffer.getBuffer(model.renderType(texture));

            model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
            renderedThisFrame = true;

            if (!entity.isSpectator()) {
                for (var layer : layers)
                    layer.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }

            buffer.endBatch();

            MASK_BUFFER.discard();
        } finally {
            renderingMask = false;

            main.bindWrite(false);
        }
    }
}
