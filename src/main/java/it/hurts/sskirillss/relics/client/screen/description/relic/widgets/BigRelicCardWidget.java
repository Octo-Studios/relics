package it.hurts.sskirillss.relics.client.screen.description.relic.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import it.hurts.sskirillss.relics.Relics;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.RelicDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.utils.misc.AnimatedValue;
import it.hurts.sskirillss.relics.client.screen.utils.misc.DeltaCounter;
import it.hurts.sskirillss.relics.client.screen.utils.misc.FloatingTexts;
import it.hurts.sskirillss.relics.client.screen.utils.misc.LevelUpEffect;
import it.hurts.sskirillss.relics.client.screen.utils.misc.RerollAnimation;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@EventBusSubscriber(modid = Relics.MODID, value = Dist.CLIENT)
public class BigRelicCardWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    public static final List<ResourceLocation> BACKGROUNDS = Stream.of("blue", "cyan", "green", "light_blue", "orange", "magenta", "purple", "red", "yellow")
            .map(color -> ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/backgrounds/" + color + ".png"))
            .toList();

    private static final int PARTICLES = 60;
    private static final int PARTICLES_PER_RANK = 20;
    private static final int MAX_PARTICLES = 200;
    private static final int MIN_ROLLBACK_DURATION = 8;
    private static final int MAX_ROLLBACK_DURATION = 30;
    private static final float ROLLBACK_PER_LEVEL = 0.75F;
    private static final float COUNTER_OFFSET_Y = 8F;
    private static final float FLASH_STRENGTH = 0.9F;

    private static final Map<ResourceLocation, Integer> TEXTURE_COLORS = new HashMap<>();
    private static final Map<TextureAtlasSprite, Integer> SPRITE_COLORS = new HashMap<>();

    private final RelicDescriptionScreen screen;

    private final LevelUpEffect levelUpEffect = new LevelUpEffect();
    private final FloatingTexts floatingTexts = new FloatingTexts();

    private final AnimatedValue shownLevel = new AnimatedValue();
    private final DeltaCounter levelCounter = new DeltaCounter(floatingTexts);

    private int lastRank = -1;
    private int lastLevel = -1;

    public BigRelicCardWidget(int x, int y, RelicDescriptionScreen screen) {
        super(x, y, 50, 87);

        this.screen = screen;
    }

    @Override
    public void onTick() {
        if (minecraft.player == null || !(screen.getStack().getItem() instanceof IRelicItem relic))
            return;

        var random = minecraft.player.getRandom();
        var leveling = relic.getRelicData(minecraft.player, screen.getStack()).getLevelingData();
        var rank = leveling.getRank();

        if (lastRank >= 0 && rank > lastRank) {
            var tint = getTextureColor(pickClosestBackground(screen.getStack(), BACKGROUNDS));

            levelUpEffect.start(screen, random, getX(), getY(), width, height, tint, Math.min(PARTICLES + rank * PARTICLES_PER_RANK, MAX_PARTICLES));

            screen.getRankupEffect().start();
        }

        lastRank = rank;

        var level = leveling.getLevel();

        if (lastLevel < 0 || level > lastLevel) {
            shownLevel.snap(level);
            levelCounter.reset();
        } else if (level < lastLevel)
            shownLevel.set(level, Mth.clamp(Math.round((shownLevel.getValue() - level) * ROLLBACK_PER_LEVEL), MIN_ROLLBACK_DURATION, MAX_ROLLBACK_DURATION));

        lastLevel = level;

        levelCounter.update(shownLevel.getInt(level), shownLevel.isMoving(), RerollAnimation.TINT_DOWN, RerollAnimation.TINT_DOWN, getX() + width / 2F, getY() + COUNTER_OFFSET_Y);

        levelUpEffect.tick();
        floatingTexts.tick();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var player = minecraft.player;
        var poseStack = guiGraphics.pose();

        var relicData = relic.getRelicData(player, stack);

        var canBeLeveledUp = relicData.calculateMaxLevel() > 0;

        poseStack.pushPose();

        poseStack.translate(0, 0, -100);

        var flash = Math.max(levelUpEffect.flash(pPartialTick), screen.getDismantleEffect().backgroundFlash(pPartialTick));
        var color = (float) (1.05F + (Math.sin(player.tickCount * 0.25F) * 0.1F)) + flash * FLASH_STRENGTH;

        var background = pickClosestBackground(stack, BACKGROUNDS);
        GUIRenderer.begin(background, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .color(color, color, color, 1F)
                .pos(getX() + 8, getY() + 20)
                .end();

        poseStack.translate(0, 0, 100);

        GUIRenderer.begin(DescriptionTextures.BIG_CARD_FRAME_ACTIVE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY())
                .end();

        if (!canBeLeveledUp)
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/big_card_frame_level_slug_active.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(this.getX(), this.getY())
                    .end();

        var hasStars = relicData.getAbilitiesData().hasUnlockedAbilityWithStats();

        if (hasStars) {
            var xOff = 0;
            for (var i = 0; i < 5; i++) {
                GUIRenderer.begin(DescriptionTextures.BIG_STAR_HOLE, poseStack)
                        .anchor(SpriteAnchor.TOP_LEFT)
                        .pos(getX() + xOff + 5, getY() + 72)
                        .end();

                xOff += 8;
            }

            xOff = 0;

            var quality = relicData.calculateQuality();
            var isAliquot = quality % 2 == 1;

            for (var i = 0; i < quality / 2; i++) {
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

        if (!screen.getDismantleEffect().isFinishing())
            renderItem(guiGraphics, pPartialTick);

        poseStack.pushPose();

        poseStack.scale(0.75F, 0.75F, 1F);

        var levelComponent = Component.literal(String.valueOf(shownLevel.getInt(relicData.getLevelingData().getLevel()))).withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, levelComponent, (int) (((getX() + 26.5F) * 1.33F) - (minecraft.font.width(levelComponent) / 2F)), (int) ((getY() + 4.5F) * 1.33F), 0xFFE278, false);

        poseStack.popPose();

        floatingTexts.render(guiGraphics, minecraft.font, pPartialTick);

        if (isHovered() && hasStars)
            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Relics.MODID, "textures/gui/description/general/big_card_frame_outline.png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() - 1, getY() - 1)
                    .end();

        poseStack.popPose();
    }

    public void renderItem(GuiGraphics graphics, float partialTick) {
        var stack = screen.getStack();
        var pose = graphics.pose();
        var time = minecraft.player.tickCount + partialTick;
        var scale = 1.75F;

        pose.pushPose();
        pose.translate(getX() + 11 + 8 * scale, getY() + 31 + Math.sin(time * 0.1F) * 2F + 8 * scale, 0);
        pose.mulPose(Axis.ZP.rotationDegrees((float) Math.cos(time * 0.05F) * 5F));
        pose.mulPose(Axis.YP.rotationDegrees((float) Math.cos(time * 0.075F) * 25F));
        pose.scale(scale, scale, scale);

        if (!screen.getDismantleEffect().renderItem(graphics, stack, partialTick)) {
            pose.translate(-8F, -8F, -150F);
            graphics.renderItem(stack, 0, 0);
        }

        pose.popPose();
    }

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resources -> {
            TEXTURE_COLORS.clear();
            SPRITE_COLORS.clear();
        });
    }

    public static int getTextureColor(ResourceLocation textureLocation) {
        return TEXTURE_COLORS.computeIfAbsent(textureLocation, BigRelicCardWidget::loadTextureColor);
    }

    private static int loadTextureColor(ResourceLocation textureLocation) {
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

        return SPRITE_COLORS.computeIfAbsent(sprite, icon -> averageColor(icon.contents().width(), icon.contents().height(), (x, y) -> icon.getPixelRGBA(0, x, y)));
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

        if (!(stack.getItem() instanceof IRelicItem relic) || !relic.getRelicData(minecraft.player, stack).getAbilitiesData().hasUnlockedAbilityWithStats())
            return;

        var poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        int maxWidth = 150;
        int renderWidth = 0;

        List<MutableComponent> entries = Lists.newArrayList(
                Component.literal("").append(Component.translatable("relics.description.researching.relic.info.level").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + relic.getRelicData(minecraft.player, stack).getLevelingData().getLevel() + "/" + relic.getRelicData(minecraft.player, stack).calculateMaxLevel()),
                Component.literal("").append(Component.translatable("relics.description.researching.relic.info.quality").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + MathUtils.round(relic.getRelicData(minecraft.player, stack).calculateQuality() / 2F, 1) + "/" + relic.getRelicData(minecraft.player, stack).getMaxQuality() / 2),
                Component.literal(" ")
        );

        if (Screen.hasShiftDown())
            entries.add(Component.translatable("relics.description.researching.relic.info.extra_info").withStyle(ChatFormatting.ITALIC));
        else
            entries.add(Component.translatable("relics.description.researching.general.extra_info"));

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
