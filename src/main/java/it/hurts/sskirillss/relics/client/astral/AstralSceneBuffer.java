package it.hurts.sskirillss.relics.client.astral;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;

public class AstralSceneBuffer {
    private static final Minecraft MC = Minecraft.getInstance();

    private static RenderTarget target;

    public static void bind() {
        var window = MC.getWindow();
        var width = Math.max(1, window.getWidth());
        var height = Math.max(1, window.getHeight());

        if (target == null)
            target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        else if (target.width != width || target.height != height)
            target.resize(width, height, Minecraft.ON_OSX);

        target.bindWrite(true);
    }

    public static int getTexture() {
        return target == null ? 0 : target.getColorTextureId();
    }

    public static void release() {
        if (target == null)
            return;

        target.destroyBuffers();

        target = null;
    }
}
