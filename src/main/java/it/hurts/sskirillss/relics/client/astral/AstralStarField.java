package it.hurts.sskirillss.relics.client.astral;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.hurts.sskirillss.relics.init.RelicsCoreShaders;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class AstralStarField {
    private static final float DOME_RADIUS = 40F;
    private static final float[] LAYER_DISTANCE = {118F, 108F, 98F, 88F, 78F, 68F};

    private static final float[] LAYER_MIN = {0.5F, 0.5F, 0.5F, 0.7F, 0.9F, 0.9F};
    private static final float[] LAYER_MAX = {1.3F, 1.5F, 1.6F, 2.0F, 2.3F, 2.3F};
    private static final int[] LAYER_COUNT = {90, 900, 760, 540, 1240, 90};
    private static final long[] LAYER_SEED = {77221L, 41315L, 35151L, 61354L, 61355L, 61356L};
    private static final float[] LAYER_SPIN = {0.024F, 0.020F, -0.017F, 0.013F, -0.011F, 0.009F};
    private static final float[] LAYER_ALPHA = {0.95F, 1F, 0.9F, 0.78F, 0.42F, 0.42F};

    private static final float[] PRIMARY = {0.78F, 0.70F, 1F};
    private static final float[] SECONDARY = {0.62F, 0.82F, 1F};

    private static final List<List<Star>> LAYERS = new ArrayList<>();

    private record Star(Vector3f position, float size, float angle, float pulseOffset, float pulseSpeed) {
    }

    static {
        for (var layer = 0; layer < LAYER_COUNT.length; layer++) {
            var random = new LegacyRandomSource(LAYER_SEED[layer]);
            var stars = new ArrayList<Star>();

            while (stars.size() < LAYER_COUNT[layer]) {
                var direction = new Vector3f(random.nextFloat() * 2F - 1F, random.nextFloat() * 2F - 1F, random.nextFloat() * 2F - 1F);
                var length = direction.lengthSquared();

                if (length >= 1F || length < 0.001F)
                    continue;

                var size = (LAYER_MIN[layer] + random.nextFloat() * (LAYER_MAX[layer] - LAYER_MIN[layer])) * (LAYER_DISTANCE[layer] / 100F);

                stars.add(new Star(direction.normalize().mul(LAYER_DISTANCE[layer]), size, random.nextFloat() * Mth.TWO_PI, random.nextFloat() * Mth.TWO_PI, 0.045F + random.nextFloat() * 0.045F));
            }

            LAYERS.add(stars);
        }
    }

    public static void renderDome(Matrix4f view, float time, Vec3 camera) {
        var shader = RelicsCoreShaders.ASTRAL_NEBULA_SHADER;

        if (shader == null)
            return;

        var modelView = RenderSystem.getModelViewStack();

        modelView.pushMatrix();
        modelView.mul(view);

        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(() -> shader);

        shader.safeGetUniform("Time").set(time);
        shader.safeGetUniform("Offset").set((float) camera.x, (float) camera.y, (float) camera.z);

        var builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        var stacks = 14;
        var slices = 28;

        for (var i = 0; i < stacks; i++) {
            var lat1 = (float) Math.PI * i / stacks - (float) Math.PI / 2F;
            var lat2 = (float) Math.PI * (i + 1) / stacks - (float) Math.PI / 2F;

            for (var j = 0; j < slices; j++) {
                var lon1 = (float) Math.PI * 2F * j / slices;
                var lon2 = (float) Math.PI * 2F * (j + 1) / slices;

                vertex(builder, lat1, lon1);
                vertex(builder, lat1, lon2);
                vertex(builder, lat2, lon2);
                vertex(builder, lat2, lon1);
            }
        }

        var mesh = builder.build();

        if (mesh != null)
            BufferUploader.drawWithShader(mesh);

        modelView.popMatrix();

        RenderSystem.applyModelViewMatrix();
    }

    private static void vertex(VertexConsumer builder, float latitude, float longitude) {
        builder.addVertex(Mth.cos(latitude) * Mth.cos(longitude) * DOME_RADIUS, Mth.sin(latitude) * DOME_RADIUS, Mth.cos(latitude) * Mth.sin(longitude) * DOME_RADIUS);
    }

    public static void renderStars(PoseStack pose, Vector3f right, Vector3f up, float time) {
        var matrix = pose.last().pose();

        for (var layer = 0; layer < LAYERS.size(); layer++) {
            var builder = AstralSprites.begin(AstralSprites.STARS[layer]);
            var tint = layer % 2 == 0 ? SECONDARY : PRIMARY;

            for (var star : LAYERS.get(layer)) {
                var pulse = 0.5F + 0.5F * Mth.sin(time * 20F * star.pulseSpeed() + star.pulseOffset());
                var alphaPulse = 0.78F + 0.22F * Mth.sin(time * 20F * star.pulseSpeed() * 1.17F + star.pulseOffset() + 1.4F);
                var red = Mth.clamp(Mth.lerp(pulse, 1F, tint[0]) * 1.25F, 0F, 1F);
                var green = Mth.clamp(Mth.lerp(pulse, 1F, tint[1]) * 1.25F, 0F, 1F);
                var blue = Mth.clamp(Mth.lerp(pulse, 1F, tint[2]) * 1.25F, 0F, 1F);
                var fog = Mth.clamp((LAYER_DISTANCE[layer] - 68F) / 50F, 0F, 1F) * 0.55F;
                var color = AstralSprites.mix((int) (red * 255F) << 16 | (int) (green * 255F) << 8 | (int) (blue * 255F), 0x4A2C9A, fog);
                var alpha = (int) (255F * Mth.clamp(LAYER_ALPHA[layer] * alphaPulse * (1F - fog * 0.45F), 0F, 1F));

                AstralSprites.quad(builder, matrix, star.position().x, star.position().y, star.position().z, right, up, star.size(), star.angle() + time * 20F * LAYER_SPIN[layer], color, alpha);
            }

            AstralSprites.end(builder);
        }
    }
}
