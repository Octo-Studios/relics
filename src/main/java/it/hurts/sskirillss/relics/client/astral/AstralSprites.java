package it.hurts.sskirillss.relics.client.astral;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.hurts.sskirillss.relics.Relics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AstralSprites {
    public static final ResourceLocation SPARK = ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/particle/spark.png");

    public static final ResourceLocation[] STARS = {
            ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_0.png"),
            ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_1.png"),
            ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_2.png"),
            ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_3.png"),
            ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_4.png"),
            ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/sky/star_5.png")
    };

    public static BufferBuilder begin(ResourceLocation texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);

        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    public static void end(BufferBuilder builder) {
        var mesh = builder.build();

        if (mesh != null)
            BufferUploader.drawWithShader(mesh);
    }

    public static void quad(VertexConsumer builder, Matrix4f matrix, double x, double y, double z, Vector3f right, Vector3f up, float size, float roll, int rgb, int alpha) {
        var cos = Mth.cos(roll) * size;
        var sin = Mth.sin(roll) * size;

        var rx = right.x * cos + up.x * sin;
        var ry = right.y * cos + up.y * sin;
        var rz = right.z * cos + up.z * sin;

        var ux = -right.x * sin + up.x * cos;
        var uy = -right.y * sin + up.y * cos;
        var uz = -right.z * sin + up.z * cos;

        var red = rgb >> 16 & 255;
        var green = rgb >> 8 & 255;
        var blue = rgb & 255;
        var a = Mth.clamp(alpha, 0, 255);

        builder.addVertex(matrix, (float) x - rx - ux, (float) y - ry - uy, (float) z - rz - uz).setUv(0F, 1F).setColor(red, green, blue, a);
        builder.addVertex(matrix, (float) x + rx - ux, (float) y + ry - uy, (float) z + rz - uz).setUv(1F, 1F).setColor(red, green, blue, a);
        builder.addVertex(matrix, (float) x + rx + ux, (float) y + ry + uy, (float) z + rz + uz).setUv(1F, 0F).setColor(red, green, blue, a);
        builder.addVertex(matrix, (float) x - rx + ux, (float) y - ry + uy, (float) z - rz + uz).setUv(0F, 0F).setColor(red, green, blue, a);
    }

    public static int mix(int from, int to, float amount) {
        var red = (int) Mth.lerp(amount, from >> 16 & 255, to >> 16 & 255);
        var green = (int) Mth.lerp(amount, from >> 8 & 255, to >> 8 & 255);
        var blue = (int) Mth.lerp(amount, from & 255, to & 255);

        return red << 16 | green << 8 | blue;
    }
}
