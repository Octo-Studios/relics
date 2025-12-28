package it.hurts.sskirillss.relics.client.post_effects;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.post_effects.PostEffect;
import it.hurts.sskirillss.relics.api.post_effects.RenderStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

public class DistortionPostEffect extends PostEffect {
    private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void construct(PostChain postChain) {
        var player = MC.player;
        if (player == null) return;

        var level = player.level();
        var cam = MC.gameRenderer.getMainCamera();

        var radius = 64.0;
        var aabb = player.getBoundingBox().inflate(radius);

        var max = 64;
        List<Entity> entities = level.getEntities(player, aabb, e -> e.isAlive() && e != player && player.hasLineOfSight(e));

        if (entities.size() > max)
            entities = entities.subList(0, max);

        var count = entities.size();
        var points = new float[192];

        for (var i = 0; i < count; i++) {
            var e = entities.get(i);

            var x = (float) e.getX();
            var y = (float) (e.getY() + e.getBbHeight() * 0.5);
            var z = (float) e.getZ();

            var o = i * 3;
            points[o] = x;
            points[o + 1] = y;
            points[o + 2] = z;
        }

        var view = new Matrix4f();
        var q = new Quaternionf(cam.rotation());
        q.conjugate();
        view.rotate(q);

        var camPos = cam.getPosition();
        view.translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);

        var proj = RenderSystem.getProjectionMatrix();
        var vp = new Matrix4f(proj).mul(view);

        var vpArr = new float[16];
        vp.get(vpArr);

        for (var postpass : postChain.passes) {
            var effect = postpass.getEffect();

            effect.safeGetUniform("PointCount").set(count);
            effect.safeGetUniform("PointsWorld").set(points);
            effect.safeGetUniform("ViewProjMat").set(vpArr);
        }
    }

    @Override
    public boolean shouldRender() {
        return false;
    }

    @Override
    public ResourceLocation getPath() {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "shaders/post/distortion.json");
    }

    @Override
    public RenderStage getStage() {
        return RenderStage.LEVEL;
    }
}