package it.hurts.sskirillss.relics.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.api.postEffects.PostEffect;
import it.hurts.sskirillss.relics.init.RelicsPostEffects;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Final
    @Shadow
    Minecraft minecraft;
    @Final
    @Shadow
    private ResourceManager resourceManager;

    @Unique
    private final Map<PostEffect, PostChain> relics$postEffects = new HashMap<>();

    @Inject(method = "render", at = @At(value = "TAIL"))
    private void relics$onRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) throws Exception {
        if (minecraft == null)
            return;

        for (var postEffect : RelicsPostEffects.getPostEffects().values()) {
            if (!postEffect.shouldRender())
                continue;

            var postChain = relics$postEffects.get(postEffect);

            if (postChain == null) {
                postChain = new PostChain(minecraft.getTextureManager(), resourceManager, minecraft.getMainRenderTarget(), postEffect.getPath());

                var window = minecraft.getWindow();

                postChain.resize(window.getWidth(), window.getHeight());

                relics$postEffects.put(postEffect, postChain);
            }

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();

            postEffect.construct(postChain);

            postChain.process(deltaTracker.getGameTimeDeltaTicks());
        }
    }

    @Inject(method = "resize(II)V", at = @At("TAIL"))
    private void relics$onResize(int width, int height, CallbackInfo ci) {
        for (var postEffect : RelicsPostEffects.getPostEffects().values()) {
            var postChain = relics$postEffects.get(postEffect);

            if (postChain != null) {
                postChain.resize(width, height);

                relics$postEffects.put(postEffect, postChain);
            }
        }
    }
}