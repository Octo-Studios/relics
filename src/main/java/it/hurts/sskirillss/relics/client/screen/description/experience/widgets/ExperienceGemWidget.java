package it.hurts.sskirillss.relics.client.screen.description.experience.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.experience.ExperienceDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionUtils;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.client.screen.utils.ScreenUtils;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExperienceGemWidget extends AbstractDescriptionWidget implements ITickingWidget, IHoverableWidget {
    private final ExperienceDescriptionScreen screen;
    private final String source;

    private float scale = 1F;
    private float scaleOld = 1F;

    public ExperienceGemWidget(int x, int y, ExperienceDescriptionScreen screen, String source) {
        super(x, y, 32, 47);

        this.screen = screen;
        this.source = source;
    }

    @Override
    public void onPress() {
        screen.setSelectedSource(source);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var player = Minecraft.getInstance().player;

        if (player == null || !(screen.stack.getItem() instanceof IRelicItem relic))
            return;

        var stack = screen.getStack();
        var poseStack = guiGraphics.pose();
        var sourceData = relic.getLevelingSourcesData().getSources().get(source);

        var isUnlocked = relic.isLevelingSourceUnlocked(stack, source);

        poseStack.pushPose();

        RenderSystem.enableBlend();

        var partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(false);

        var lerpedScale = Mth.lerp(partialTicks, scaleOld, scale);

        poseStack.scale(lerpedScale, lerpedScale, lerpedScale);

        poseStack.translate((getX() + (width / 2F)) / lerpedScale, (getY() + (height / 2F)) / lerpedScale, 0);

        var shape = sourceData.getShape().name().toLowerCase(Locale.ROOT);

        var color = (float) (1.05F + (Math.sin((player.tickCount + (source.length() * 10)) * 0.2F) * 0.1F));

        if (isUnlocked)
            GUIRenderer.begin(sourceData.getIcon().apply(stack), poseStack)
                    .pos(0, -1)
                    .color(color, color, color, 1F)
                    .end();
        else
            GUIRenderer.begin(DescriptionTextures.SMALL_CARD_LOCK_BACKGROUND, poseStack)
                    .pos(0, -1)
                    .end();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/experience/gems/" + shape + "/" + sourceData.getColor().name().toLowerCase(Locale.ROOT) + ".png"), poseStack)
                .end();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/experience/gems/" + shape + "/frame_" + (isUnlocked ? "unlocked" : "locked") + ".png"), poseStack)
                .end();

        RenderSystem.disableBlend();

        {
            MutableComponent title = Component.literal(isUnlocked ? String.valueOf(relic.getLevelingSourceLevel(stack, source)) : "?").withStyle(ChatFormatting.BOLD);

            float textScale = 0.5F;

            poseStack.scale(textScale, textScale, textScale);

            guiGraphics.drawString(minecraft.font, title, -((width + 1) / 2) - (minecraft.font.width(title) / 2) + 16, (-(height / 2) + 40), isUnlocked ? 0xFFE278 : 0xB7AED9, true);
        }

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        if (!(screen.stack.getItem() instanceof IRelicItem relic))
            return;

        float maxScale = 1.15F;
        float minScale = 1F;

        RandomSource random = minecraft.player.getRandom();

        scaleOld = scale;

        if (scale > maxScale)
            scale = Math.max(minScale, scale - 0.01F);

        if (isHovered()) {
            if (minecraft.player.tickCount % 3 == 0)
                ParticleStorage.addParticle(screen, new ExperienceParticleData(
                        new Color(200 + random.nextInt(50), 150 + random.nextInt(100), 0),
                        getX() + random.nextInt(width), getY() - 1, 1F + (random.nextFloat() * 0.5F), 100 + random.nextInt(50)));

            if (scale < maxScale)
                scale = Math.min(maxScale, scale + 0.04F);
        } else {
            if (scale > minScale)
                scale = Math.max(minScale, scale - 0.03F);
        }
    }

    @Override
    public boolean isLocked() {
        return screen.getSelectedSource().equals(source);
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var stack = screen.stack;

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var data = relic.getLevelingSourceData(source);

        if (data == null)
            return;

        PoseStack poseStack = guiGraphics.pose();

        List<FormattedCharSequence> tooltip = Lists.newArrayList();

        var title = Component.translatableWithFallback("tooltip.relics." + BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + ".leveling_source." + source + ".title", source);

        int maxWidth = 110;
        int renderWidth = Math.min((minecraft.font.width(title.withStyle(ChatFormatting.BOLD)) / 2) + 4, maxWidth);

        List<MutableComponent> entries = new ArrayList<>();

        entries.add(Component.literal(" "));

        var requiredLevel = data.getRequiredLevel();
        var requiredAbility = data.getRequiredAbility();

        if (relic.getRelicLevel(screen.stack) < requiredLevel) {
            entries.add(Component.literal(" "));

            entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.gem.low_level", Component.literal(String.valueOf(requiredLevel)).withStyle(ChatFormatting.BOLD))));
        } else if (!relic.isAbilityUnlocked(stack, requiredAbility)) {
            entries.add(Component.literal(" "));

            entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.gem.locked_ability")));
        }

        for (MutableComponent entry : entries) {
            int entryWidth = (minecraft.font.width(entry)) / 2;

            if (entryWidth > renderWidth)
                renderWidth = Math.min(entryWidth + 4, maxWidth);

            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
        }

        int height = tooltip.size() * 5;

        int y = getHeight() / 2;

        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(false);

        float lerpedScale = Mth.lerp(partialTicks, scaleOld, scale);

        poseStack.scale(lerpedScale, lerpedScale, lerpedScale);

        poseStack.translate((getX() + (getWidth() / 2F)) / lerpedScale, (getY() + (getHeight() / 2F)) / lerpedScale, 0);

        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, height, -((renderWidth + 19) / 2), y);

        int yOff = 0;

        poseStack.pushPose();

        poseStack.scale(0.5F, 0.5F, 0.5F);

        if (!relic.isLevelingSourceUnlocked(stack, source)) {
            title = ScreenUtils.stylizeWidthReplacement(title, 1F, Style.EMPTY.withFont(ScreenUtils.ILLAGER_ALT_FONT).withColor(0x9E00B0), source.length());

            var random = minecraft.player.getRandom();

            var shakeX = MathUtils.randomFloat(random) * 0.5F;
            var shakeY = MathUtils.randomFloat(random) * 0.5F;

            poseStack.translate(shakeX, shakeY, 0F);

        } else
            title.withStyle(ChatFormatting.BOLD);

        guiGraphics.drawString(minecraft.font, title, -(minecraft.font.width(title) / 2), ((y + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

        poseStack.popPose();

        for (FormattedCharSequence entry : tooltip) {
            poseStack.pushPose();

            poseStack.scale(0.5F, 0.5F, 0.5F);

            guiGraphics.drawString(minecraft.font, entry, -(minecraft.font.width(entry) / 2), ((y + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);

            yOff += 5;

            poseStack.popPose();
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked())
            super.playDownSound(handler);
    }
}