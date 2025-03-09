package it.hurts.sskirillss.relics.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.events.common.TooltipDisplayEvent;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.RenderUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
    @Unique
    private static final ResourceLocation UPGRADE_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/item_state/upgrade.png");
    @Unique
    private static final ResourceLocation RESEARCH_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/item_state/research.png");

    @Inject(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onTooltipRender(Font font, List<ClientTooltipComponent> tooltip, int x, int y, ClientTooltipPositioner positioner, CallbackInfo info, RenderTooltipEvent.Pre event, int width, int height, int postWidth, int postHeight, Vector2ic postPos) {
        if (!tooltip.isEmpty())
            NeoForge.EVENT_BUS.post(new TooltipDisplayEvent(event.getItemStack(), (GuiGraphics) (Object) this, postWidth, postHeight, postPos.x(), postPos.y()));
    }

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", shift = At.Shift.AFTER))
    public void renderItemDecorations(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;

        if (player == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var guiGraphics = (GuiGraphics) (Object) this;
        var poseStack = guiGraphics.pose();

        var partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        var time = (stack.getItem().hashCode() / 1000F) + player.tickCount + partialTicks;
        var color = (float) (1F + Math.sin(time * 0.45F) * 0.1F);

        var renderedUpgradeIcon = false;
        var renderedResearchIcon = false;

        for (var ability : relic.getAbilitiesData().getAbilities().values()) {
            if (!renderedUpgradeIcon && relic.mayUpgrade(stack, ability.getId())) {
                poseStack.pushPose();

                poseStack.translate(x + 9, y, 200);

                poseStack.translate(3.5F, 3.5F, 0);

                poseStack.mulPose(Axis.ZP.rotation((float) (Math.sin(time * 0.35F) * 0.15F)));

                poseStack.translate(-3.5F, -3.5F, 0);

                GUIRenderer.begin(UPGRADE_TEXTURE, poseStack)
                        .color(color, color, color, 1F)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .end();

                poseStack.popPose();

                renderedUpgradeIcon = true;
            }

            if (!renderedResearchIcon && (relic.mayResearch(stack, ability.getId()) || relic.mayUnlock(stack, ability.getId()))) {
                poseStack.pushPose();

                poseStack.translate(x + 9, y + 9, 200);

                poseStack.translate(Math.sin(time * 0.25F) * 0.75F, Math.cos(time * 0.25F) * 0.75F, 0F);

                RenderSystem.enableBlend();

                GUIRenderer.begin(RESEARCH_TEXTURE, poseStack)
                        .color(color, color, color, 1F)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .end();

                RenderSystem.disableBlend();

                poseStack.popPose();

                renderedResearchIcon = true;
            }
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", shift = At.Shift.AFTER))
    public void renderItem(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int guiOffset, CallbackInfo ci) {
        var player = Minecraft.getInstance().player;

        if (player == null || !(stack.getItem() instanceof IRelicItem relic))
            return;

        var guiGraphics = (GuiGraphics) (Object) this;
        var poseStack = guiGraphics.pose();

        var partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        var time = seed + player.tickCount + partialTicks;

        if (relic.isRelicFlawless(stack)) {
            var data = relic.getStyleData().getBeams().apply(player, stack);

            var beams = 8;

            for (int i = 0; i < beams; i++) {
                var angle = (float) (i * 2F * Math.PI / beams);

                poseStack.pushPose();

                poseStack.mulPose(Axis.ZP.rotation(angle));
                poseStack.mulPose(Axis.ZP.rotation(time * 0.025F));

                var length = 0.85F + ((i % 2 == 0 ? Math.sin(time * 0.25F) : Math.cos(time * 0.25F)) * 0.1F);

                RenderUtils.renderFlatBeam(guiGraphics, partialTicks, (float) length, 0.45F, data.getStartColor(), data.getEndColor());

                poseStack.popPose();
            }
        }
    }
}