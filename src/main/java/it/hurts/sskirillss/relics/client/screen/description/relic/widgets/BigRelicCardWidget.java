package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class BigRelicCardWidget extends AbstractDescriptionWidget implements IHoverableWidget {
    public static final List<ResourceLocation> BACKGROUNDS = Stream.of("blue", "cyan", "green", "light_blue", "orange", "magenta", "purple", "red", "yellow")
            .map(color -> ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/backgrounds/" + color + ".png"))
            .toList();

    private RelicDescriptionScreen screen;

    public BigRelicCardWidget(int x, int y, RelicDescriptionScreen screen) {
        super(x, y, 50, 87);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var player = minecraft.player;
        var poseStack = guiGraphics.pose();

        poseStack.pushPose();

        poseStack.translate(0, 0, -100);


        var color = (float) (1.05F + (Math.sin(player.tickCount * 0.25F) * 0.1F));

        RenderSystem.setShaderColor(color, color, color, 1F);

        var background = pickClosestBackground(stack, BACKGROUNDS);

        if (background != null)
            GUIRenderer.begin(background, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() + 8, getY() + 20)
                    .end();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        poseStack.translate(0, 0, 100);

        GUIRenderer.begin(DescriptionTextures.BIG_CARD_FRAME_UNLOCKED_ACTIVE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY())
                .end();

        int xOff = 0;

        if (relic.hasUnlockedUpgradeableAbility(player, stack)) {
            for (int i = 0; i < 5; i++) {
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_HOLE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 5, getY() + 72)
                        .end();

                xOff += 8;
            }

            xOff = 0;

            var quality = relic.calculateRelicQuality(player, stack);
            var isAliquot = quality % 2 == 1;

            for (int i = 0; i < Math.floor(quality / 2D); i++) {
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 5, getY() + 72)
                        .end();

                xOff += 8;
            }

            if (isAliquot)
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_ACTIVE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 5, getY() + 72)
                        .patternSize(4, 7)
                        .texSize(8, 7)
                        .end();
        }

        poseStack.pushPose();

        float scale = 1.75F;

        poseStack.translate(getX() + 11 + 8 * scale, getY() + 31 + Math.sin((player.tickCount + pPartialTick) * 0.1F) * 2F + 8 * scale, 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.cos((player.tickCount + pPartialTick) * 0.05F) * 5F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.cos((player.tickCount + pPartialTick) * 0.075F) * 25F));

        poseStack.translate(-8 * scale, -8 * scale, -150 * scale);

        poseStack.scale(scale, scale, scale);

        guiGraphics.renderItem(stack, 0, 0);

        poseStack.popPose();

        poseStack.pushPose();

        poseStack.scale(0.75F, 0.75F, 1F);

        MutableComponent levelComponent = Component.literal(String.valueOf(relic.getRelicLevel(player, stack))).withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, levelComponent, (int) (((getX() + 26.5F) * 1.33F) - (minecraft.font.width(levelComponent) / 2F)), (int) ((getY() + 4.5F) * 1.33F), 0xFFE278, false);

        poseStack.popPose();

        if (isHovered() && relic.hasUnlockedUpgradeableAbility(player, stack))
            GUIRenderer.begin(DescriptionTextures.BIG_CARD_FRAME_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() - 1, getY() - 1)
                    .end();

        poseStack.popPose();
    }

    public static int getTextureColor(ResourceLocation textureLocation) {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        var resource = resourceManager.getResource(textureLocation).orElse(null);

        if (resource == null)
            return 0x00000000;

        try (var in = resource.open(); var img = NativeImage.read(in)) {
            return averageColor(img.getWidth(), img.getHeight(), img::getPixelRGBA);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getItemColor(ItemStack itemStack) {
        var mc = Minecraft.getInstance();
        var renderer = mc.getItemRenderer();
        var model = renderer.getModel(itemStack, mc.level, mc.player, 0);
        var sprite = model.getParticleIcon(ModelData.EMPTY);

        var w = sprite.contents().width();
        var h = sprite.contents().height();

        return averageColor(w, h, (x, y) -> sprite.getPixelRGBA(0, x, y));
    }

    public static double colorDistance(int colorA, int colorB) {
        var rA = (colorA >>> 16) & 0xFF;
        var gA = (colorA >>> 8) & 0xFF;
        var bA = colorA & 0xFF;
        var rB = (colorB >>> 16) & 0xFF;
        var gB = (colorB >>> 8) & 0xFF;
        var bB = colorB & 0xFF;

        var dr = rA - rB;
        var dg = gA - gB;
        var db = bA - bB;

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    public static ResourceLocation pickClosestBackground(ItemStack itemStack, List<ResourceLocation> backgroundTextures) {
        var itemColor = getItemColor(itemStack);

        return backgroundTextures.stream()
                .min(Comparator.comparingDouble(tex -> colorDistance(itemColor, getTextureColor(tex))))
                .orElse(null);
    }

    private static int averageColor(int width, int height, PixelSampler sampler) {
        long sumR = 0, sumG = 0, sumB = 0, count = 0;

        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                var p = sampler.sample(x, y);
                if (FastColor.ABGR32.alpha(p) < 16)
                    continue;

                sumR += FastColor.ABGR32.red(p);
                sumG += FastColor.ABGR32.green(p);
                sumB += FastColor.ABGR32.blue(p);

                count++;
            }
        }

        if (count == 0)
            return 0;

        var avgR = (int) (sumR / count);
        var avgG = (int) (sumG / count);
        var avgB = (int) (sumB / count);

        return (avgR << 16) | (avgG << 8) | avgB;
    }

    @FunctionalInterface
    private interface PixelSampler {
        int sample(int x, int y);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic) || !relic.hasUnlockedUpgradeableAbility(minecraft.player, stack))
            return;

        var poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 150;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.info.level").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + relic.getRelicLevel(minecraft.player, stack) + "/" + relic.calculateRelicMaxLevel(minecraft.player, stack)),
                Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.info.quality").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + MathUtils.round(relic.calculateRelicQuality(minecraft.player, stack) / 2F, 1) + "/" + relic.getRelicMaxQuality(minecraft.player, stack) / 2),
                Component.literal(" ")
        );

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("tooltip.relics.researching.relic.info.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("tooltip.relics.researching.general.extra_info"));

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry) / 2);

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 2, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        poseStack.pushPose();

        poseStack.translate(0F, 0F, 400);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, (this.getX() - renderWidth / 2) + 16, this.getY() + this.getHeight() - 2);

        poseStack.scale(0.5F, 0.5F, 0.5F);

        int yOff = 0;

        for (FormattedCharSequence entry : tooltip) {
            guiGraphics.drawString(minecraft.font, entry, ((this.getX() - renderWidth / 2) + 14 + 12) * 2, ((this.getY() + yOff + this.getHeight() - 2 + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;
        }

        poseStack.popPose();
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}