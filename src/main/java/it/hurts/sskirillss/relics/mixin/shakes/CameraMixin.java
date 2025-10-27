package it.hurts.sskirillss.relics.mixin.shakes;

import it.hurts.sskirillss.relics.dev.shake.ShakeManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "setup", at = @At("TAIL"))
    public void onSetup(BlockGetter blockGetter, Entity entity, boolean bl, boolean bl2, float partialTicks, CallbackInfo ci) {
        var MC = Minecraft.getInstance();
        var player = MC.player;

        if (player == null)
            return;

        var shakeRotation = new Vector3f();
        var shakeOffset = new Vector3f();

        for (var effect : ShakeManager.SHAKES.values()) {
            shakeRotation.add(effect.getShakeRotation(player, partialTicks));
            shakeOffset.add(effect.getShakeOffset(player, partialTicks));
        }

        var camera = MC.gameRenderer.getMainCamera();

        var inverseRotation = new Quaternionf(camera.rotation()).conjugate();

        if (shakeRotation.lengthSquared() > 0) {
            shakeRotation.rotate(inverseRotation);

            float rotationFactor = 10F;

            float rotationX = shakeRotation.x() * rotationFactor;
            float rotationY = shakeRotation.y() * rotationFactor;
            float rotationZ = shakeRotation.z() * rotationFactor;

            camera.rotation().mul(new Quaternionf()
                    .rotateX(-rotationX * (float) (Math.PI / 180F))
                    .rotateY(-rotationY * (float) (Math.PI / 180F))
                    .rotateZ(-rotationZ * (float) (Math.PI / 180F)));
        }

        if (shakeOffset.lengthSquared() > 0) {
            shakeOffset.rotate(inverseRotation);

            camera.move(-shakeOffset.z(), shakeOffset.y(), shakeOffset.x());
        }
    }
}