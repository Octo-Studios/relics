package it.hurts.sskirillss.relics.effects;

import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.client.models.effects.StunStarModel;
import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

public class StunEffect extends MobEffect {
    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0X6836AA);
    }

    @EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onMouseInput(InputEvent.InteractionKeyMappingTriggered event) {
            var player = Minecraft.getInstance().player;

            if (player != null && player.hasEffect(RelicsMobEffects.STUN)) {
                event.setSwingHand(false);

                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onBlockHighlight(RenderHighlightEvent.Block event) {
            var player = Minecraft.getInstance().player;

            if (player != null && player.hasEffect(RelicsMobEffects.STUN))
                event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onEntityRender(RenderLivingEvent.Pre<?, ?> event) {
            var entity = event.getEntity();

            var effect = entity.getEffect(RelicsMobEffects.STUN);

            if (effect == null || entity.isDeadOrDying())
                return;

            var poseStack = event.getPoseStack();

            poseStack.pushPose();
            poseStack.translate(0, entity.getBbHeight() + 0.25F, 0);
            poseStack.scale(0.25F, 0.25F, 0.25F);

            var stars = (int) entity.getBbWidth() * 10;
            var ticks = entity.tickCount + event.getPartialTick();
            var radius = 1F + stars * 0.15F;

            for (var i = 0; i < stars; i++) {
                poseStack.pushPose();

                var angle = ticks / 20F + (2 * Math.PI / stars) * i;

                var x = Mth.cos((float) angle) * radius;
                var z = Mth.sin((float) angle) * radius;
                var y = Mth.sin(ticks * 0.15F + i * 0.75F) * 0.25F;

                poseStack.translate(x, y, z);

                var rotDeg = (float) Math.toDegrees(angle) + 90F;

                poseStack.mulPose(Axis.YP.rotationDegrees(rotDeg));
                poseStack.mulPose(Axis.XP.rotationDegrees(180F));

                new StunStarModel<>().renderToBuffer(poseStack, event.getMultiBufferSource().getBuffer(RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/mob_effect/model/stun_star.png"))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            }

            poseStack.popPose();
        }

        @SubscribeEvent
        public static void onComputeFOV(ComputeFovModifierEvent event) {
            var player = event.getPlayer();
            var effect = player.getEffect(RelicsMobEffects.STUN);

            if (effect == null)
                return;

            var duration = effect.getDuration();
            var threshold = 80F;
            var factor = duration > threshold ? 0.5F : (1F - duration / threshold) * 0.5F + 0.5F;

            event.setNewFovModifier(Math.clamp(factor, 0.5F, 1F));
        }
    }
}