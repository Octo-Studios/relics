package it.hurts.sskirillss.relics.client.astral;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.items.AstralSatchelItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstralSpaceRenderer {
    private static final Minecraft MC = Minecraft.getInstance();

    private static final float ITEM_SIZE = 1.1F;
    private static final int RINGS = 9;
    private static final int MERIDIANS = 16;
    private static final int SEGMENTS = 72;

    private static final Map<Vec3, ItemVisual> VISUALS = new HashMap<>();

    private static final float WAVE_SPEED = 14F;

    private static float lastFrameTime;
    private static float frameFlash;

    private static class ItemVisual {
        float hover;
        float spin;

        ItemVisual(float spin) {
            this.spin = spin;
        }
    }

    public static void render(Matrix4f baseView, Matrix4f projectionMatrix, float partialTick, boolean buffered) {
        if (MC.player == null || MC.level == null)
            return;

        if (buffered)
            AstralSceneBuffer.bind();

        var time = AstralSpace.getTime();
        var delta = Mth.clamp(time - lastFrameTime, 0F, 0.1F);

        lastFrameTime = time;

        var now = System.nanoTime();
        var camera = AstralSpace.getCamera(partialTick);
        var entries = AstralSpace.getEntries();
        var projection = new Matrix4f(projectionMatrix);
        var zoom = 1F - Mth.clamp(AstralSpace.getSpeed(partialTick) / 0.4F, 0F, 1F) * 0.13F;

        projection.m00(projection.m00() * zoom);
        projection.m11(projection.m11() * zoom);

        var view = baseView;

        var oldProjection = RenderSystem.getProjectionMatrix();
        var oldSorting = RenderSystem.getVertexSorting();
        var modelView = RenderSystem.getModelViewStack();

        RenderSystem.setProjectionMatrix(projection, VertexSorting.DISTANCE_TO_ORIGIN);

        modelView.pushMatrix();
        modelView.identity();

        RenderSystem.applyModelViewMatrix();

        FogRenderer.setupNoFog();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.clearColor(0.015F, 0.008F, 0.05F, 1F);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();

        AstralStarField.renderDome(view, time, camera);

        var right = new Vector3f(view.m00(), view.m10(), view.m20());
        var up = new Vector3f(view.m01(), view.m11(), view.m21());
        var pose = new PoseStack();

        pose.mulPose(view);
        pose.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        AstralStarField.renderStars(pose, right, up, time);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();

        var bufferSource = MC.renderBuffers().bufferSource();
        var breath = 1F + 0.015F * Mth.sin(time * 0.9F);

        frameFlash = 0F;

        for (var wave : AstralSpace.getWaves())
            frameFlash += Math.max(0F, 1F - (now - wave.born()) / 1.0E9F * 2.5F);

        frameFlash = Math.min(frameFlash, 1F);

        renderShell(pose, time, now, camera, breath);

        var aimed = AstralSpace.pick(entries, partialTick);

        renderItems(pose, bufferSource, entries, partialTick, time, delta, aimed, camera, projection, view, buffered);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        renderNodes(pose, right, up, time, now, camera, breath);

        AstralFX.render(pose, right, up, partialTick, time);

        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();

        RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.ORTHOGRAPHIC_Z);

        renderOverlay();

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();

        if (buffered) {
            RenderSystem.disableBlend();

            MC.getMainRenderTarget().bindWrite(true);
        }

        modelView.popMatrix();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
        RenderSystem.defaultBlendFunc();
    }

    private static Vec3 spherePoint(int ring, int meridian, float rotation, float radius) {
        var latitude = (float) Math.PI * ring / (RINGS + 1) - (float) Math.PI / 2F;
        var longitude = rotation + (float) Math.PI * 2F * meridian / MERIDIANS;

        return new Vec3(Mth.cos(latitude) * Mth.cos(longitude) * radius, Mth.sin(latitude) * radius, Mth.cos(latitude) * Mth.sin(longitude) * radius);
    }

    private static float near(double x, double y, double z, Vec3 camera) {
        var dx = x - camera.x;
        var dy = y - camera.y;
        var dz = z - camera.z;
        var distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        return Mth.clamp(1.2F - distance / (float) (AstralSatchelItem.RADIUS * 1.3D), 0.18F, 1F);
    }

    private static float waveWeight(Vec3 origin, double x, double y, double z, float age) {
        var dx = x - origin.x;
        var dy = y - origin.y;
        var dz = z - origin.z;
        var offset = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) - age * WAVE_SPEED;

        return (float) Math.exp(-offset * offset / 5F) * (float) Math.pow(1F - age / AstralSpace.WAVE_DURATION, 0.7D);
    }

    private static float wave(double x, double y, double z, long now) {
        var total = 0F;

        for (var wave : AstralSpace.getWaves()) {
            var age = (now - wave.born()) / 1.0E9F;

            if (age < 0F || age > AstralSpace.WAVE_DURATION)
                continue;

            total += waveWeight(wave.origin(), x, y, z, age);
        }

        return Math.min(total, 1F);
    }

    private static int waveColor(double x, double y, double z, long now) {
        var red = 0F;
        var green = 0F;
        var blue = 0F;
        var weight = 0F;

        for (var wave : AstralSpace.getWaves()) {
            var age = (now - wave.born()) / 1.0E9F;

            if (age < 0F || age > AstralSpace.WAVE_DURATION)
                continue;

            var value = waveWeight(wave.origin(), x, y, z, age);

            red += (wave.color() >> 16 & 255) * value;
            green += (wave.color() >> 8 & 255) * value;
            blue += (wave.color() & 255) * value;
            weight += value;
        }

        if (weight < 1.0E-4F)
            return 0xFFFFFF;

        return (int) (red / weight) << 16 | (int) (green / weight) << 8 | (int) (blue / weight);
    }

    private static void renderShell(PoseStack pose, float time, long now, Vec3 camera, float breath) {
        var radius = (float) AstralSatchelItem.RADIUS * breath;
        var rotation = time * 0.03F;
        var matrix = pose.last().pose();
        var builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (var ring = 1; ring <= RINGS; ring++) {
            var latitude = (float) Math.PI * ring / (RINGS + 1) - (float) Math.PI / 2F;
            var ringRadius = Mth.cos(latitude) * radius;
            var height = Mth.sin(latitude) * radius;
            var base = ring == (RINGS + 1) / 2 ? 120 : 62;

            for (var i = 0; i < SEGMENTS; i++) {
                var a1 = rotation + (float) Math.PI * 2F * i / SEGMENTS;
                var a2 = rotation + (float) Math.PI * 2F * (i + 1) / SEGMENTS;

                segment(builder, matrix, Mth.cos(a1) * ringRadius, height, Mth.sin(a1) * ringRadius, Mth.cos(a2) * ringRadius, height, Mth.sin(a2) * ringRadius, base, time, now, camera, radius);
            }
        }

        for (var meridian = 0; meridian < MERIDIANS; meridian++) {
            var longitude = rotation + (float) Math.PI * 2F * meridian / MERIDIANS;

            for (var i = 0; i < SEGMENTS / 2; i++) {
                var l1 = (float) Math.PI * i / (SEGMENTS / 2F) - (float) Math.PI / 2F;
                var l2 = (float) Math.PI * (i + 1) / (SEGMENTS / 2F) - (float) Math.PI / 2F;

                segment(builder, matrix,
                        Mth.cos(l1) * Mth.cos(longitude) * radius, Mth.sin(l1) * radius, Mth.cos(l1) * Mth.sin(longitude) * radius,
                        Mth.cos(l2) * Mth.cos(longitude) * radius, Mth.sin(l2) * radius, Mth.cos(l2) * Mth.sin(longitude) * radius, 62, time, now, camera, radius);
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        var mesh = builder.build();

        if (mesh != null)
            BufferUploader.drawWithShader(mesh);
    }

    private static void segment(VertexConsumer builder, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, int base, float time, long now, Vec3 camera, float radius) {
        var direction = new Vector3f(x2 - x1, y2 - y1, z2 - z1);
        var middle = new Vector3f((x1 + x2) * 0.5F - (float) camera.x, (y1 + y2) * 0.5F - (float) camera.y, (z1 + z2) * 0.5F - (float) camera.z);
        var side = direction.cross(middle, new Vector3f());

        if (side.lengthSquared() < 1.0E-8F)
            return;

        var waveMid = wave((x1 + x2) * 0.5F, (y1 + y2) * 0.5F, (z1 + z2) * 0.5F, now);

        side.normalize(0.0026F * middle.length() * (1F + 2.2F * waveMid));

        var first = shade(x1, y1, z1, base, time, now, camera, radius);
        var second = shade(x2, y2, z2, base, time, now, camera, radius);

        builder.addVertex(matrix, x1 - side.x, y1 - side.y, z1 - side.z).setColor(first[0] >> 16 & 255, first[0] >> 8 & 255, first[0] & 255, first[1]);
        builder.addVertex(matrix, x1 + side.x, y1 + side.y, z1 + side.z).setColor(first[0] >> 16 & 255, first[0] >> 8 & 255, first[0] & 255, first[1]);
        builder.addVertex(matrix, x2 + side.x, y2 + side.y, z2 + side.z).setColor(second[0] >> 16 & 255, second[0] >> 8 & 255, second[0] & 255, second[1]);
        builder.addVertex(matrix, x2 - side.x, y2 - side.y, z2 - side.z).setColor(second[0] >> 16 & 255, second[0] >> 8 & 255, second[0] & 255, second[1]);
    }

    private static int[] shade(float x, float y, float z, int base, float time, long now, Vec3 camera, float radius) {
        var near = near(x, y, z, camera);
        var wave = wave(x, y, z, now);
        var pulse = 0.6F + 0.4F * Mth.sin(time * 1.4F - y * 0.45F + x * 0.15F);
        var color = AstralSprites.mix(0x966EFF, 0x5AC8FF, Mth.clamp(y / radius * 0.5F + 0.5F, 0F, 1F));

        color = AstralSprites.mix(color, 0x3A1E80, (1F - near) * 0.75F);

        if (wave > 0.01F)
            color = AstralSprites.mix(color, AstralSprites.mix(waveColor(x, y, z, now), 0xFFFFFF, 0.3F), Mth.clamp(wave * 1.4F, 0F, 1F));

        return new int[]{color, Mth.clamp((int) (base * near * pulse * 1.6F + wave * 255F + frameFlash * 70F), 0, 255)};
    }

    private static void renderNodes(PoseStack pose, Vector3f right, Vector3f up, float time, long now, Vec3 camera, float breath) {
        var matrix = pose.last().pose();
        var builder = AstralSprites.begin(AstralSprites.SPARK);
        var rotation = time * 0.03F;
        var radius = (float) AstralSatchelItem.RADIUS * breath;

        for (var ring = 1; ring <= RINGS; ring++) {
            for (var meridian = 0; meridian < MERIDIANS; meridian++) {
                var point = spherePoint(ring, meridian, rotation, radius);
                var pulse = 0.5F + 0.5F * Mth.sin(time * 1.8F + ring * 0.9F + meridian * 0.7F);
                var near = near(point.x, point.y, point.z, camera);
                var wave = wave(point.x, point.y, point.z, now);
                var alpha = Mth.clamp((int) (150F * near * 1.6F * (0.5F + 0.5F * pulse) + wave * 200F), 0, 255);

                AstralSprites.quad(builder, matrix, point.x, point.y, point.z, right, up, 0.22F + 0.2F * pulse + wave * 0.2F, 0F, AstralSprites.mix(0x7A68FF, 0xFFFFFF, wave * 0.7F), alpha);
            }
        }

        AstralSprites.end(builder);
    }

    private static void renderItems(PoseStack pose, MultiBufferSource.BufferSource bufferSource, List<AstralSatchelItem.Entry> entries, float partialTick, float time, float delta, int aimed, Vec3 camera, Matrix4f projection, Matrix4f view, boolean buffered) {
        var itemRenderer = MC.getItemRenderer();
        var radius = (float) AstralSatchelItem.RADIUS;
        var labelPosition = Vec3.ZERO;

        VISUALS.keySet().removeIf(key -> entries.stream().noneMatch(entry -> entry.position().equals(key)));

        Lighting.setupFor3DItems();

        for (var i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            var birth = AstralSpace.getBirth(entry);
            var phase = (float) (entry.position().x * 12.9898D + entry.position().z * 37.719D);
            var visual = VISUALS.computeIfAbsent(entry.position(), key -> new ItemVisual(phase * 40F));

            visual.hover += ((i == aimed ? 1F : 0F) - visual.hover) * Mth.clamp(delta * 12F, 0F, 1F);
            visual.spin += Mth.lerp(visual.hover, 25F, 70F) * delta * (birth < 1F ? 3F - 2F * birth : 1F);

            var position = AstralSpace.getPosition(entry, partialTick);
            var toCamera = camera.subtract(position);
            var distance = (float) toCamera.length();

            if (distance > 0.001F)
                position = position.add(toCamera.scale(0.35D * visual.hover / distance));

            if (i == aimed)
                labelPosition = position;

            var fog = Mth.lerp(visual.hover, Mth.clamp(1.15F - distance / (radius * 1.6F), 0F, 1F), 1F);
            var model = itemRenderer.getModel(entry.stack(), MC.level, null, 0);
            var groundScale = Math.max(model.getTransforms().ground.scale.x(), 0.1F);
            var scale = ITEM_SIZE / groundScale * (1F + 0.2F * visual.hover) * birth;

            RenderSystem.setShaderColor(Mth.lerp(fog, 0.55F, 1F), Mth.lerp(fog, 0.5F, 1F), Mth.lerp(fog, 0.85F, 1F), 1F);

            pose.pushPose();
            pose.translate(position.x, position.y, position.z);
            pose.mulPose(Axis.YP.rotationDegrees(visual.spin));
            pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(time * 0.8F + phase) * 8F));
            pose.scale(scale, scale, scale);

            itemRenderer.renderStatic(entry.stack(), ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, bufferSource, MC.level, 0);

            pose.popPose();

            bufferSource.endBatch();

            if (buffered)
                AstralSceneBuffer.bind();
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        for (var flyer : AstralSpace.getFlyers()) {
            var progress = AstralSpace.getFlyerProgress(flyer);
            var position = AstralSpace.getFlyerPosition(flyer, partialTick);
            var model = itemRenderer.getModel(flyer.stack(), MC.level, null, 0);
            var groundScale = Math.max(model.getTransforms().ground.scale.x(), 0.1F);
            var scale = ITEM_SIZE / groundScale * Mth.lerp(progress, 1.2F, 0.2F);

            pose.pushPose();
            pose.translate(position.x, position.y, position.z);
            pose.mulPose(Axis.YP.rotationDegrees(time * 420F));
            pose.scale(scale, scale, scale);

            itemRenderer.renderStatic(flyer.stack(), ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, pose, bufferSource, MC.level, 0);

            pose.popPose();

            bufferSource.endBatch();

            if (buffered)
                AstralSceneBuffer.bind();
        }

        if (aimed >= 0 && aimed < entries.size()) {
            var point = project(labelPosition, camera, projection, view);

            if (point != null)
                AstralSpace.setLabel(entries.get(aimed).stack(), point[0], point[1], VISUALS.get(entries.get(aimed).position()).hover);
        } else {
            AstralSpace.fadeLabel(delta * 7F);
        }
    }

    private static float[] project(Vec3 position, Vec3 camera, Matrix4f projection, Matrix4f view) {
        var matrix = new Matrix4f(projection).mul(view);
        var point = new Vector4f((float) (position.x - camera.x), (float) (position.y - camera.y), (float) (position.z - camera.z), 1F);

        matrix.transform(point);

        if (point.w <= 0.01F)
            return null;

        var window = MC.getWindow();

        return new float[]{(point.x / point.w * 0.5F + 0.5F) * window.getGuiScaledWidth(), (1F - (point.y / point.w * 0.5F + 0.5F)) * window.getGuiScaledHeight()};
    }

    private static void renderOverlay() {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        var vignette = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var steps = 40;
        var inner = 0.62F;
        var outer = 1.6F;

        for (var i = 0; i < steps; i++) {
            var a1 = (float) Math.PI * 2F * i / steps;
            var a2 = (float) Math.PI * 2F * (i + 1) / steps;

            vignette.addVertex(Mth.cos(a1) * inner, Mth.sin(a1) * inner, -0.5F).setColor(6, 2, 22, 0);
            vignette.addVertex(Mth.cos(a1) * outer, Mth.sin(a1) * outer, -0.5F).setColor(6, 2, 22, 185);
            vignette.addVertex(Mth.cos(a2) * outer, Mth.sin(a2) * outer, -0.5F).setColor(6, 2, 22, 185);
            vignette.addVertex(Mth.cos(a2) * inner, Mth.sin(a2) * inner, -0.5F).setColor(6, 2, 22, 0);
        }

        var vignetteMesh = vignette.build();

        if (vignetteMesh != null)
            BufferUploader.drawWithShader(vignetteMesh);
    }
}
