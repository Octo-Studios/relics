package it.hurts.sskirillss.relics.client.handlers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.client.models.items.base.CurioModel;
import it.hurts.sskirillss.relics.client.models.items.base.SidedFPRCurioModel;
import it.hurts.sskirillss.relics.client.models.items.utils.ModelSide;
import it.hurts.sskirillss.relics.items.relics.base.IRenderableCurio;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@EventBusSubscriber(modid = Reference.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class HandRenderHandler {
    @SubscribeEvent
    public static void onRenderHand(RenderArmEvent event) {
        HumanoidArm arm = event.getArm();

        PoseStack poseStack = event.getPoseStack();

        CuriosApi.getCuriosInventory(event.getPlayer()).ifPresent(itemHandler -> {
            for (ICurioStacksHandler curioHandler : itemHandler.getCurios().values()) {
                IDynamicStackHandler stackHandler = curioHandler.getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    if (!curioHandler.getRenders().get(i))
                        continue;

                    ItemStack stack = stackHandler.getStackInSlot(i);

                    if (!(stack.getItem() instanceof IRenderableCurio renderable) || CurioModel.getLayerLocation(stack.getItem()).toString().equals("minecraft:air#air")
                            || !(renderable.getModel(stack) instanceof SidedFPRCurioModel model))
                        continue;

                    model.setSlot(i);

                    if ((arm == HumanoidArm.RIGHT && model.getSide() == ModelSide.RIGHT && model.getById("right_arm") != null)
                            || (arm == HumanoidArm.LEFT && model.getSide() == ModelSide.LEFT && model.getById("left_arm") != null)) {
                        poseStack.pushPose();

                        float scale = 1.6F;

                        if (arm == HumanoidArm.RIGHT) {
                            poseStack.mulPose(Axis.ZN.rotationDegrees(-5));
                            poseStack.scale(scale, scale, scale);
                            poseStack.translate(-0.075, -0.7, 0);
                        } else {
                            poseStack.mulPose(Axis.ZN.rotationDegrees(5));
                            poseStack.scale(scale, scale, scale);
                            poseStack.translate(0.01, -0.7, 0);
                        }

                        model.renderToBuffer(poseStack, ItemRenderer.getArmorFoilBuffer(event.getMultiBufferSource(), RenderType.armorCutoutNoCull(renderable.getTexture(stack)), stack.hasFoil()), event.getPackedLight(), OverlayTexture.NO_OVERLAY);

                        poseStack.popPose();
                    }
                }
            }
        });
    }
}