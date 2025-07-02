package it.hurts.sskirillss.relics.client.tooltip;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.api.events.common.TooltipDisplayEvent;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.style.TooltipData;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.AnimationData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(modid = Reference.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TooltipBorderHandler {
    @SubscribeEvent
    public static void onTooltipDisplay(TooltipDisplayEvent event) {
        var player = Minecraft.getInstance().player;

        if (player == null)
            return;

        var stack = event.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var tooltip = relic.getStyleTemplate(player, stack).getTooltip().apply(player, stack);

        if (!tooltip.isTextured())
            return;

        var graphics = event.getGraphics();
        var poseStack = graphics.pose();

        var width = event.getWidth();
        var height = event.getHeight();

        var x = event.getX();
        var y = event.getY();

        var id = tooltip.getIcon().isEmpty() ? BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() : tooltip.getIcon();

        var texture = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/tooltip/frame/" + id + "/frame.png");

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, texture);

        Minecraft.getInstance().getTextureManager().getTexture(texture).bind();

        int texWidth = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int texHeight = GlStateManager._getTexLevelParameter(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

        if (texHeight == 0 || texWidth == 0)
            return;

        var patternWidth = 160;
        var patternHeight = 64;

        var cornerWidth = 32;
        var cornerHeight = 32;

        var middleWidth = 96;
        var middleHeight = cornerHeight;

        poseStack.pushPose();

        RenderSystem.enableBlend();

        poseStack.translate(0, 0, 410.0);

        var animation = AnimationData.fromMcmeta(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/tooltip/frame/" + id + "/frame.png.mcmeta"));

        int frame = animation.getFrameByTime(player.tickCount).getKey();

        int offset = patternHeight * frame;

        graphics.blit(texture, x - cornerWidth / 2 - 3, y - cornerHeight / 2 - 3, 0, offset, cornerWidth, cornerHeight, texWidth, texHeight);
        graphics.blit(texture, x + width - cornerWidth / 2 + 3, y - cornerHeight / 2 - 3, patternWidth - cornerWidth, offset, cornerWidth, cornerHeight, texWidth, texHeight);

        graphics.blit(texture, x - cornerWidth / 2 - 3, y + height - cornerHeight / 2 + 3, 0, (patternHeight - cornerHeight) + offset, cornerWidth, cornerHeight, texWidth, texHeight);
        graphics.blit(texture, x + width - cornerWidth / 2 + 3, y + height - cornerHeight / 2 + 3, patternWidth - cornerWidth, (patternHeight - cornerHeight) + offset, cornerWidth, cornerHeight, texWidth, texHeight);

        graphics.blit(texture, x + (width - middleWidth) / 2, y - middleHeight + 1, cornerWidth, offset, middleWidth, middleHeight, texWidth, texHeight);
        graphics.blit(texture, x + (width - middleWidth) / 2, y + height - 1, cornerWidth, middleHeight + offset, middleWidth, middleHeight, texWidth, texHeight);

        texture = ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/tooltip/frame/" + id + "/star.png");

        RenderSystem.setShaderTexture(0, texture);

        var xOff = 0;

        for (int i = 1; i < relic.calculateRelicQuality(player, stack) + 1; i++) {
            var isAliquot = i % 2 == 1;

            var color = (float) (1F + Math.sin(player.tickCount * i * 0.05F) * 0.1F);

            RenderSystem.setShaderColor(color, color, color, 1F);

            graphics.blit(texture, x + width / 2 - 14 + xOff, y - 10, (isAliquot ? 0 : 3), 0, isAliquot ? 3 : 2, 5, 5, 5);

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            xOff += 3;
        }

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onTooltipColorEvent(RenderTooltipEvent.Color event) {
        ItemStack stack = event.getItemStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        TooltipData tooltip = relic.getStyleTemplate(Minecraft.getInstance().player, stack).getTooltip().apply(Minecraft.getInstance().player, stack);

        if (tooltip.getBorderTop() != -1)
            event.setBorderStart(tooltip.getBorderTop());
        if (tooltip.getBorderBottom() != -1)
            event.setBorderEnd(tooltip.getBorderBottom());

        if (tooltip.getBackgroundTop() != -1)
            event.setBackgroundStart(tooltip.getBackgroundTop());
        if (tooltip.getBackgroundBottom() != -1)
            event.setBackgroundEnd(tooltip.getBackgroundBottom());
    }
}