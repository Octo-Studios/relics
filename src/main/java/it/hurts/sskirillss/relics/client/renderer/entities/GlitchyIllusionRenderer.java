package it.hurts.sskirillss.relics.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.entities.GlitchyIllusionEntity;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GlitchyIllusionRenderer extends EntityRenderer<GlitchyIllusionEntity> {
    private final PlayerModel<AbstractClientPlayer> wideModel;
    private final PlayerModel<AbstractClientPlayer> slimModel;
    private final ItemInHandRenderer itemInHandRenderer;

    private final Map<Integer, Snapshot> snapshots = new HashMap<>();
    private final Map<Integer, Float> attackColorBlend = new HashMap<>();
    private final Map<Integer, Float> attackColorBlendTime = new HashMap<>();

    public GlitchyIllusionRenderer(EntityRendererProvider.Context context) {
        super(context);

        this.wideModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.itemInHandRenderer = context.getItemInHandRenderer();
    }

    @Override
    public void render(GlitchyIllusionEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var owner = entity.getOwner();

        if (!(owner instanceof AbstractClientPlayer player))
            return;

        var snapshot = this.snapshots.computeIfAbsent(entity.getId(), id -> this.captureSnapshot(entity, player, partialTicks));
        var model = snapshot.slim() ? this.slimModel : this.wideModel;
        var attackProgress = entity.getEchoAttackProgress(partialTicks);
        var attackArm = player.getMainArm();

        snapshot.apply(model);
        this.applyEchoLook(model, entity);
        this.applyEchoSwing(model, attackArm, attackProgress);

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180F - entity.getYRot()));
        if (entity.getPose() == Pose.SWIMMING || entity.getPose() == Pose.FALL_FLYING) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90F - entity.getSnapshotXRot()));
            poseStack.translate(0D, -1D, 0.3D);
        }
        poseStack.scale(-0.9375F, -0.9375F, 0.9375F);
        poseStack.translate(0D, -1.501D, 0D);

        var time = entity.tickCount + partialTicks;
        var attackColorBlend = this.getAttackColorBlend(entity, attackProgress, time);
        var wave = (Mth.sin(time * 0.45F) + 1F) * 0.5F;
        var jitter = (Mth.sin(time * 2.8F + entity.getId()) + 1F) * 0.5F;
        var red = Mth.lerp(wave, 0.04F, 0.18F);
        var green = Mth.lerp(jitter, 0.78F, 1F);
        var blue = Mth.lerp(wave, 0.22F, 0.58F);
        red = Mth.lerp(attackColorBlend, red, 1F);
        green = Mth.lerp(attackColorBlend, green, 0.08F);
        blue = Mth.lerp(attackColorBlend, blue, 0.05F);
        var alpha = Mth.lerp(jitter, 0.48F, 0.72F);
        var bodyColor = color(entity.isFlawless(), alpha, red, green, blue);
        var auraRed = Mth.lerp(attackColorBlend, 0.1F, 1F);
        var auraGreen = Mth.lerp(attackColorBlend, 1F, 0.12F);
        var auraBlue = Mth.lerp(attackColorBlend, 0.35F, 0.08F);
        var auraColor = color(entity.isFlawless(), 0.18F + wave * 0.12F, auraRed, auraGreen, auraBlue);
        var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(snapshot.texture()));

        model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bodyColor);

        poseStack.pushPose();

        var pulseScale = 1.1F + wave * 0.1F;

        poseStack.scale(pulseScale, pulseScale, pulseScale);

        model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, auraColor);

        poseStack.popPose();

        if (attackProgress > 0F && !entity.getEchoAttackItem().isEmpty())
            this.renderHeldItem(player, model, entity.getEchoAttackItem(), attackArm, poseStack, buffer, packedLight);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private Snapshot captureSnapshot(GlitchyIllusionEntity entity, AbstractClientPlayer player, float partialTicks) {
        var model = player.getSkin().model() == PlayerSkin.Model.SLIM ? this.slimModel : this.wideModel;

        this.setupModelProperties(model, entity, player);

        var shouldSit = player.isPassenger() && player.getVehicle() != null && player.getVehicle().shouldRiderSit();
        var bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        var headYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot);
        var netHeadYaw = headYaw - bodyYaw;

        if (shouldSit && player.getVehicle() instanceof LivingEntity vehicle) {
            bodyYaw = Mth.rotLerp(partialTicks, vehicle.yBodyRotO, vehicle.yBodyRot);
            netHeadYaw = headYaw - bodyYaw;

            var wrappedYaw = Mth.wrapDegrees(netHeadYaw);

            if (wrappedYaw < -85F)
                wrappedYaw = -85F;

            if (wrappedYaw >= 85F)
                wrappedYaw = 85F;

            bodyYaw = headYaw - wrappedYaw;

            if (wrappedYaw * wrappedYaw > 2500F)
                bodyYaw += wrappedYaw * 0.2F;

            netHeadYaw = headYaw - bodyYaw;
        }

        var headPitch = Mth.lerp(partialTicks, player.xRotO, player.getXRot());
        var limbSwing = 0F;
        var limbSwingAmount = 0F;

        if (!shouldSit && player.isAlive()) {
            limbSwingAmount = player.walkAnimation.speed(partialTicks);
            limbSwing = player.walkAnimation.position(partialTicks);

            if (player.isBaby())
                limbSwing *= 3F;

            if (limbSwingAmount > 1F)
                limbSwingAmount = 1F;
        }

        model.attackTime = player.getAttackAnim(partialTicks);
        model.riding = shouldSit;
        model.young = player.isBaby();

        model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(player, limbSwing, limbSwingAmount, player.tickCount + partialTicks, Mth.wrapDegrees(netHeadYaw), headPitch);

        return Snapshot.from(model, player);
    }

    private void setupModelProperties(PlayerModel<AbstractClientPlayer> model, GlitchyIllusionEntity entity, AbstractClientPlayer player) {
        if (player.isSpectator()) {
            model.setAllVisible(false);

            model.head.visible = true;
            model.hat.visible = true;

            return;
        }

        model.setAllVisible(true);

        model.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
        model.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
        model.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        model.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        model.crouching = entity.getPose() == Pose.CROUCHING;

        var mainPose = getArmPose(player, InteractionHand.MAIN_HAND);
        var offPose = getArmPose(player, InteractionHand.OFF_HAND);

        if (mainPose.isTwoHanded())
            offPose = player.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;

        if (player.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = mainPose;
            model.leftArmPose = offPose;
        } else {
            model.rightArmPose = offPose;
            model.leftArmPose = mainPose;
        }
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer player, InteractionHand hand) {
        var itemStack = player.getItemInHand(hand);

        if (itemStack.isEmpty())
            return HumanoidModel.ArmPose.EMPTY;

        var armPose = IClientItemExtensions.of(itemStack).getArmPose(player, hand, itemStack);

        return armPose != null ? armPose : HumanoidModel.ArmPose.ITEM;
    }

    private float getAttackColorBlend(GlitchyIllusionEntity entity, float attackProgress, float time) {
        var id = entity.getId();
        var previous = this.attackColorBlend.getOrDefault(id, 0F);
        var previousTime = this.attackColorBlendTime.getOrDefault(id, time);
        var delta = Math.max(0F, time - previousTime);
        var blend = attackProgress > 0F ? 1F : Math.max(0F, previous - delta / 8F);

        this.attackColorBlend.put(id, blend);
        this.attackColorBlendTime.put(id, time);

        return blend;
    }

    private void applyEchoSwing(PlayerModel<AbstractClientPlayer> model, HumanoidArm arm, float progress) {
        if (progress <= 0F)
            return;

        var modelArm = arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
        var direction = arm == HumanoidArm.LEFT ? -1F : 1F;
        var bodyRot = Mth.sin(Mth.sqrt(progress) * ((float) Math.PI * 2F)) * 0.2F * direction;

        model.body.yRot = bodyRot;
        model.rightArm.z = Mth.sin(bodyRot) * 5F;
        model.rightArm.x = -Mth.cos(bodyRot) * 5F;
        model.leftArm.z = -Mth.sin(bodyRot) * 5F;
        model.leftArm.x = Mth.cos(bodyRot) * 5F;
        model.rightArm.yRot += bodyRot;
        model.leftArm.yRot += bodyRot;
        model.leftArm.xRot += bodyRot;

        var eased = 1F - progress;
        eased *= eased;
        eased *= eased;
        eased = 1F - eased;

        var swing = Mth.sin(eased * (float) Math.PI);
        var headOffset = Mth.sin(progress * (float) Math.PI) * -(model.head.xRot - 0.7F) * 0.75F;

        modelArm.xRot -= swing * 1.2F + headOffset;
        modelArm.yRot += bodyRot * 2F;
        modelArm.zRot += Mth.sin(progress * (float) Math.PI) * -0.4F;

        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
        model.jacket.copyFrom(model.body);
    }

    private void applyEchoLook(PlayerModel<AbstractClientPlayer> model, GlitchyIllusionEntity entity) {
        if (entity.getEchoAttackTicks() <= 0 && entity.getEchoHeadPitch() == 0F)
            return;

        model.head.yRot = 0F;
        model.head.xRot = entity.getEchoHeadPitch() * ((float) Math.PI / 180F);
        model.hat.copyFrom(model.head);
    }

    private void renderHeldItem(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, net.minecraft.world.item.ItemStack stack, HumanoidArm arm,
                                PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        model.translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180F));
        poseStack.translate((arm == HumanoidArm.LEFT ? -1F : 1F) / 16F, 0.125F, -0.625F);
        this.itemInHandRenderer.renderItem(player, stack, arm == HumanoidArm.LEFT ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                arm == HumanoidArm.LEFT, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static int color(boolean flawless, float alpha, float red, float green, float blue) {
        var source = new Color(
                Mth.clamp(red, 0F, 1F),
                Mth.clamp(green, 0F, 1F),
                Mth.clamp(blue, 0F, 1F),
                Mth.clamp(alpha, 0F, 1F)
        );
        var result = FlawlessUtils.getColor(flawless, source);

        return result.getRGB();
    }

    @Override
    public ResourceLocation getTextureLocation(GlitchyIllusionEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    private record Snapshot(ResourceLocation texture, boolean slim, boolean young, HumanoidModel.ArmPose leftArmPose, HumanoidModel.ArmPose rightArmPose, PartSnapshot head, PartSnapshot hat, PartSnapshot body, PartSnapshot rightArm, PartSnapshot leftArm, PartSnapshot rightLeg, PartSnapshot leftLeg, PartSnapshot leftSleeve, PartSnapshot rightSleeve, PartSnapshot leftPants, PartSnapshot rightPants, PartSnapshot jacket) {
        private static Snapshot from(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player) {
            return new Snapshot(player.getSkin().texture(), player.getSkin().model() == PlayerSkin.Model.SLIM, model.young, model.leftArmPose, model.rightArmPose, PartSnapshot.from(model.head), PartSnapshot.from(model.hat), PartSnapshot.from(model.body), PartSnapshot.from(model.rightArm), PartSnapshot.from(model.leftArm), PartSnapshot.from(model.rightLeg), PartSnapshot.from(model.leftLeg), PartSnapshot.from(model.leftSleeve), PartSnapshot.from(model.rightSleeve), PartSnapshot.from(model.leftPants), PartSnapshot.from(model.rightPants), PartSnapshot.from(model.jacket));
        }

        private void apply(PlayerModel<AbstractClientPlayer> model) {
            model.young = this.young;
            model.leftArmPose = this.leftArmPose;
            model.rightArmPose = this.rightArmPose;

            this.head.apply(model.head);
            this.hat.apply(model.hat);
            this.body.apply(model.body);
            this.rightArm.apply(model.rightArm);
            this.leftArm.apply(model.leftArm);
            this.rightLeg.apply(model.rightLeg);
            this.leftLeg.apply(model.leftLeg);
            this.leftSleeve.apply(model.leftSleeve);
            this.rightSleeve.apply(model.rightSleeve);
            this.leftPants.apply(model.leftPants);
            this.rightPants.apply(model.rightPants);
            this.jacket.apply(model.jacket);
        }
    }

    private record PartSnapshot(float x, float y, float z, float xRot, float yRot, float zRot, float xScale, float yScale, float zScale, boolean visible) {
        private static PartSnapshot from(ModelPart part) {
            return new PartSnapshot(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot, part.xScale, part.yScale, part.zScale, part.visible);
        }

        private void apply(ModelPart part) {
            part.x = this.x;
            part.y = this.y;
            part.z = this.z;
            part.xRot = this.xRot;
            part.yRot = this.yRot;
            part.zRot = this.zRot;
            part.xScale = this.xScale;
            part.yScale = this.yScale;
            part.zScale = this.zScale;
            part.visible = this.visible;
        }
    }
}
