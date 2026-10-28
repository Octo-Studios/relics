package it.hurts.sskirillss.relics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexBuffer;
import it.hurts.sskirillss.relics.client.astral.AstralSpace;
import it.hurts.sskirillss.relics.client.astral.AstralSpaceRenderer;
import it.hurts.sskirillss.relics.client.astral.AstralVoidOverlay;
import it.hurts.sskirillss.relics.client.renderer.sky.MidnightMantleSkyRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1)
public class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    private void relics$renderAstralSpace(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        var partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        if (AstralSpace.isTransitioning()) {
            AstralSpaceRenderer.render(frustumMatrix, projectionMatrix, partialTick, true);

            return;
        }

        if (!AstralSpace.isRendering())
            return;

        AstralSpaceRenderer.render(frustumMatrix, projectionMatrix, partialTick, false);

        ci.cancel();
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void relics$renderAstralOverlay(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (AstralSpace.isTransitioning())
            AstralVoidOverlay.render();
    }

    @WrapOperation(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V",
                    ordinal = 1
            )
    )
    private void relics$skipVanillaStars(VertexBuffer buffer, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shader, Operation<Void> original) {
        if (!MidnightMantleSkyRenderer.shouldSuppressVanillaStars())
            original.call(buffer, modelViewMatrix, projectionMatrix, shader);
    }
}
