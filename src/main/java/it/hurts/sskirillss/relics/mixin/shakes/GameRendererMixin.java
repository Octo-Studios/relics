package it.hurts.sskirillss.relics.mixin.shakes;

import it.hurts.sskirillss.relics.dev.shake.Shake;
import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;

        if (player == null)
            return;

        Iterator<Shake> iterator = ShakeManager.SHAKES.values().iterator();

        while (iterator.hasNext()) {
            Shake effect = iterator.next();

            effect.update(player);

            if (effect.isFinished())
                iterator.remove();
        }
    }
}