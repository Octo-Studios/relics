package it.hurts.sskirillss.relics.client.renderer.items;

import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.models.items.ReflectiveNecklaceModel;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class ReflectiveNecklaceRenderer implements ICurioRenderer {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/item/model/reflection_necklace.png");

    private final ReflectiveNecklaceModel model;

    public ReflectiveNecklaceRenderer() {
        this.model = new ReflectiveNecklaceModel(Minecraft.getInstance().getEntityModels().bakeLayer(ReflectiveNecklaceModel.LAYER));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> parent, MultiBufferSource buf, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(slotContext.entity() instanceof Player player))
            return;

        poseStack.pushPose();

        model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTicks);
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.translateIfSneaking(poseStack, player);
        ICurioRenderer.rotateIfSneaking(poseStack, player);

        ICurioRenderer.followBodyRotations(player, model);

        poseStack.translate(0, 1, 0.015F);

        var vertexConsumer = ItemRenderer.getArmorFoilBuffer(buf, RenderType.armorCutoutNoCull(TEXTURE), stack.hasFoil());

        var pendant = model.bodyPart.getChild("pendant");

        model.bodyPart.getChild("neck").render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);

        var deltaX = Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX());
        var deltaY = Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY());
        var deltaZ = Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ());

        var bodyYawLerp = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);

        var sinYaw = Math.sin(bodyYawLerp * (Math.PI / 180D));
        var cosYaw = -Math.cos(bodyYawLerp * (Math.PI / 180D));

        var forwardMotion = Mth.clamp((float) (deltaX * sinYaw + deltaZ * cosYaw) * 100F, -80F, 80F);
        var verticalMotion = Mth.clamp((float) deltaY * 10F, -6F, 32F);
        var sideMotion = Mth.clamp((float) (deltaX * cosYaw - deltaZ * sinYaw) * 100F, -20F, 20F);
        var pressMotion = forwardMotion > 0 ? forwardMotion * 0.1F : forwardMotion * 0.3F;
        var liftMotion = verticalMotion > 0 ? -verticalMotion * 2F : -verticalMotion * 0.05F;

        var rawDegX = pressMotion + liftMotion;
        var rawDegY = sideMotion * 0.7F;

        var nlDegX = Math.signum(rawDegX) * (float) Math.pow(Math.abs(rawDegX), 1.2F);
        var nlDegY = Math.signum(rawDegY) * (float) Math.pow(Math.abs(rawDegY), 1.2F);

        var radX = nlDegX * ((float) Math.PI / 180F);
        var radY = nlDegY * ((float) Math.PI / 180F);

        pendant.xRot = radX;
        pendant.yRot = radY;
        pendant.zRot = 0F;

        var idleTime = ageInTicks + partialTicks;

        var noiseX = (Mth.sin(idleTime * 0.1F) + Mth.sin(idleTime * 0.3F)) * 0.02F;
        var noiseY = (Mth.cos(idleTime * 0.1F) + Mth.cos(idleTime * 0.3F)) * 0.02F;

        pendant.xRot += noiseX;
        pendant.yRot += noiseY;

        var swingProgress = player.getAttackAnim(partialTicks);

        if (swingProgress > 0F) {
            var swingOffset = Mth.sin(swingProgress * (float) Math.PI) * 0.5F;

            pendant.yRot += swingOffset;
        }

        pendant.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}