package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.entities.ReflectiveOrbModel;
import it.hurts.sskirillss.relics.entities.ReflectiveOrbEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReflectiveOrbRenderer extends EntityRenderer<ReflectiveOrbEntity> {
    public ReflectiveOrbRenderer(Context renderManager) {
        super(renderManager);
    }

    @Override
    public void render(ReflectiveOrbEntity orb, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        var time = orb.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        var velocity = orb.getDeltaMovement();
        var speed = (float) velocity.length();

        var spinSpeed = 7F / (speed + 1F);
        var spinAngle = time * spinSpeed;

        var zStretch = 1F + speed * 1.5F;

        var invNorm = 1F / (speed + 1F);

        var jellyAmp = 0.15F * invNorm;

        var jellyX = 1F + (float) Math.sin(time) * jellyAmp;
        var jellyY = 1F + (float) Math.cos(time) * jellyAmp;

        poseStack.pushPose();

        poseStack.translate(0, 0.2, 0);

        var dx = velocity.x;
        var dy = velocity.y;
        var dz = velocity.z;

        var yawAngle = (float) Math.toDegrees(Math.atan2(dx, dz));
        var pitchAngle = (float) Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        poseStack.mulPose(Axis.YP.rotationDegrees(yawAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitchAngle));

        poseStack.scale(1F, 1F, zStretch);
        poseStack.scale(jellyX, jellyY, 1F);

        poseStack.mulPose(Axis.ZP.rotationDegrees(spinAngle));
        poseStack.mulPose(Axis.YN.rotationDegrees(spinAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitchAngle));
        poseStack.mulPose(Axis.YP.rotationDegrees(-yawAngle));

        poseStack.translate(0, -0.125, 0);

        var modelScale = 0.8F;

        poseStack.scale(modelScale, modelScale, modelScale);
        poseStack.translate(0, -1.125, 0);

        new ReflectiveOrbModel<>().renderToBuffer(poseStack, buffers.getBuffer(RenderType.entityTranslucentCull(getTextureLocation(orb))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ReflectiveOrbEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/entities/reflective_orb" + (entity.isFlawless() ? "_flawless" : "") + ".png");
    }
}