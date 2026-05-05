package it.hurts.sskirillss.relics.client.renderer.items;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.items.MidnightMantleFullMoonModel;
import it.hurts.sskirillss.relics.client.models.items.MidnightMantleModel;
import it.hurts.sskirillss.relics.client.models.items.MidnightMantleNewMoonModel;
import it.hurts.sskirillss.relics.client.renderer.items.base.IRelicRenderer;
import it.hurts.sskirillss.relics.items.relics.back.MidnightMantleItem;
import it.hurts.sskirillss.relics.utils.FlawlessUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class MidnightMantleRenderer implements IRelicRenderer {
    private final MidnightMantleModel model;

    public MidnightMantleRenderer() {
        this.model = new MidnightMantleModel(Minecraft.getInstance().getEntityModels().bakeLayer(MidnightMantleModel.LAYER));
    }

    @Override
    public <E extends LivingEntity, EM extends EntityModel<E>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<E, EM> parent, MultiBufferSource bufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        var entity = slotContext.entity();

        if (!(entity instanceof Player player))
            return;

        var relic = (MidnightMantleItem) stack.getItem();

        var time = entity.tickCount + (Minecraft.getInstance().isPaused() ? 0 : partialTicks);

        poseStack.pushPose();

        this.model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        ICurioRenderer.followBodyRotations(entity, this.model);

        var mode = relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").getMode();

        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/midnight_mantle_" + (relic.getRelicData(entity, stack).isVisuallyFlawless() ? "flawless" : mode) + ".png"))), relic.getRelicData(entity, stack).isVisuallyFlawless() ? LightTexture.FULL_BRIGHT : light, OverlayTexture.NO_OVERLAY);

        float flicker = 0.75F + 0.25F * Mth.sin(time * 0.15F);

        this.model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(FlawlessUtils.getTexture(entity, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/midnight_mantle_stars_" + mode + ".png")))), relic.getRelicData(entity, stack).isVisuallyFlawless() ? LightTexture.FULL_BRIGHT : LightTexture.pack((int) (15 * flicker), (int) (15 * flicker)), OverlayTexture.NO_OVERLAY);

        if (relic.getRelicData(entity, stack).getAbilitiesData().getAbilityData("phase").canPlayerUse(entity)) {
            var deltaX = (float) (Mth.lerp(partialTicks, player.xCloakO, player.xCloak) - Mth.lerp(partialTicks, player.xo, player.getX()));
            var deltaY = (float) (Mth.lerp(partialTicks, player.yCloakO, player.yCloak) - Mth.lerp(partialTicks, player.yo, player.getY()));
            var deltaZ = (float) (Mth.lerp(partialTicks, player.zCloakO, player.zCloak) - Mth.lerp(partialTicks, player.zo, player.getZ()));

            var amplitude = 0.35F;

            var bodyYaw = Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);

            var sin = (float) Math.sin(bodyYaw);
            var cos = (float) Math.cos(bodyYaw);

            var forward = -deltaX * sin + deltaZ * cos;
            var side = deltaX * cos + deltaZ * sin;

            var dx = -side * amplitude;
            var dy = -deltaY * amplitude;
            var dz = -forward * amplitude;

            var levAmp = 0.1F;
            var levSpeed = 0.1F;
            var levOffset = Mth.sin(time * levSpeed) * levAmp;

            var tiltDeg = 5F;
            var tiltAmp = tiltDeg * ((float) Math.PI / 180F);
            var tiltSpeed = 0.1F;
            var tiltX = Mth.sin(time * tiltSpeed) * tiltAmp;
            var tiltZ = Mth.cos(time * tiltSpeed) * tiltAmp;

            poseStack.pushPose();

            poseStack.translate(dx, dy + levOffset, dz);

            poseStack.mulPose(Axis.XP.rotation(tiltX));
            poseStack.mulPose(Axis.ZP.rotation(tiltZ));

            if (mode.equals("new_moon"))
                new MidnightMantleNewMoonModel<>().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(FlawlessUtils.getTexture(player, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/midnight_mantle_moon_new_moon.png")))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            else if (mode.equals("full_moon"))
                new MidnightMantleFullMoonModel<>().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutout(FlawlessUtils.getTexture(player, stack, ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/item/model/midnight_mantle_moon_full_moon.png")))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
