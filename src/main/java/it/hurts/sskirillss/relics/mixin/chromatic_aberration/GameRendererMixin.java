package it.hurts.sskirillss.relics.mixin.chromatic_aberration;

import it.hurts.sskirillss.relics.dev.chromatic_aberration.ChromaticAberrationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;

        if (player == null)
            return;

        var iterator = ChromaticAberrationManager.CHROMATIC_ABERRATIONS.values().iterator();

        while (iterator.hasNext()) {
            var effect = iterator.next();

            effect.update(player);

            if (effect.isFinished())
                iterator.remove();
        }
    }
}