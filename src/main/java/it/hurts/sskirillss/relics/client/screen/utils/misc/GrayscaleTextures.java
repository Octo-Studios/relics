package it.hurts.sskirillss.relics.client.screen.utils.misc;

import com.mojang.blaze3d.platform.NativeImage;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GrayscaleTextures {
    private static final Map<ResourceLocation, ResourceLocation> CACHE = new HashMap<>();

    public static ResourceLocation get(ResourceLocation source) {
        return CACHE.computeIfAbsent(source, GrayscaleTextures::generate);
    }

    private static ResourceLocation generate(ResourceLocation source) {
        var minecraft = Minecraft.getInstance();
        var resource = minecraft.getResourceManager().getResource(source).orElse(null);

        if (resource == null)
            return source;

        try (var in = resource.open()) {
            var image = NativeImage.read(in);

            for (var y = 0; y < image.getHeight(); y++) {
                for (var x = 0; x < image.getWidth(); x++) {
                    var pixel = image.getPixelRGBA(x, y);

                    var alpha = pixel >>> 24;
                    var blue = (pixel >> 16) & 0xFF;
                    var green = (pixel >> 8) & 0xFF;
                    var red = pixel & 0xFF;

                    var gray = (int) (red * 0.299F + green * 0.587F + blue * 0.114F);

                    image.setPixelRGBA(x, y, (alpha << 24) | (gray << 16) | (gray << 8) | gray);
                }
            }

            var result = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "grayscale/" + source.getNamespace() + "/" + source.getPath());

            minecraft.getTextureManager().register(result, new DynamicTexture(image));

            return result;
        } catch (IOException e) {
            return source;
        }
    }
}
