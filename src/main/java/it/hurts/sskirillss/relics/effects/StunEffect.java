package it.hurts.sskirillss.relics.effects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.client.models.effects.StunStarModel;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import it.hurts.sskirillss.relics.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

public class StunEffect extends MobEffect {
    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0X6836AA);
    }

    @EventBusSubscriber(modid = Reference.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onMouseInput(InputEvent.InteractionKeyMappingTriggered event) {
            Player player = Minecraft.getInstance().player;

            if (player != null && player.hasEffect(RelicsMobEffects.STUN)) {
                event.setSwingHand(false);

                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(RenderHighlightEvent.Block event) {
            Player player = Minecraft.getInstance().player;

            if (player != null && player.hasEffect(RelicsMobEffects.STUN))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
            LivingEntity entity = event.getEntity();

            if (!entity.hasEffect(RelicsMobEffects.STUN) || entity.isDeadOrDying())
                return;

            PoseStack poseStack = event.getPoseStack();

            poseStack.pushPose();

            poseStack.translate(0, entity.getBbHeight() + 0.15F, 0);
            poseStack.scale(0.175F, 0.175F, 0.175F);
            poseStack.scale(1, -1, 1);

            int stars = 10;

            float ticks = entity.tickCount + event.getPartialTick();

            for (int i = 0; i < stars; i++) {
                poseStack.pushPose();

                poseStack.translate(0, Mth.sin(ticks * 0.1F + i * 20) * 0.2F, 0);

                poseStack.mulPose(Axis.ZP.rotationDegrees((Mth.cos(ticks / 10.0F) / 7.0F) * (180F / (float) Math.PI)));
                poseStack.mulPose(Axis.YP.rotationDegrees((ticks / 20.0F) * (180F / (float) Math.PI) + (i * (360F / stars))));
                poseStack.mulPose(Axis.XP.rotationDegrees((Mth.sin(ticks / 10.0F) / 7.0F) * (180F / (float) Math.PI)));

                poseStack.translate(0, 0, 1F + stars * 0.15F);

                StunStarModel.createBodyLayer().bakeRoot().render(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.entityCutout(StunStarModel.TEXTURE.getModel())),
                        event.getPackedLight(), OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            }

            poseStack.popPose();
        }
    }
}