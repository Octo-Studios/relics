package it.hurts.sskirillss.relics.client.screen.description.experience.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.experience.ExperienceDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.relic.particles.ExperienceParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.awt.*;
import java.util.Locale;

public class ExperienceGemWidget extends AbstractDescriptionWidget implements ITickingWidget {
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

        GUIRenderer.begin(isUnlocked ? sourceData.getIcon().apply(stack) : DescriptionTextures.SMALL_CARD_LOCK_BACKGROUND, poseStack)
                .pos(0, -1)
                .end();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/experience/gems/" + shape + "/" + sourceData.getColor().name().toLowerCase(Locale.ROOT) + ".png"), poseStack)
                .end();

        GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/experience/gems/" + shape + "/frame.png"), poseStack)
                .end();

        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    @Override
    public void onTick() {
        if (!(screen.stack.getItem() instanceof IRelicItem relic))
            return;

        float maxScale = 1.15F;
        float minScale = 1F;

        RandomSource random = minecraft.player.getRandom();

//        if (canUpgrade || canResearch) {
//            if (minecraft.player.tickCount % 7 == 0)
//                ParticleStorage.addParticle(screen, new ExperienceParticleData(new Color(200 + random.nextInt(50), 150 + random.nextInt(100), 0),
//                        getX() + 5 + random.nextInt(18), getY() + 18, 1F + (random.nextFloat() * 0.5F), 100 + random.nextInt(50)));
//        }

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

//    @Override
//    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//        if (!(screen.stack.getItem() instanceof IRelicItem relic))
//            return;
//
//        AbilityData data = relic.getAbilityData(ability);
//
//        if (data == null)
//            return;
//
//        PoseStack poseStack = guiGraphics.pose();
//
//        List<FormattedCharSequence> tooltip = Lists.newArrayList();
//
//        int maxWidth = 110;
//        int renderWidth = 0;
//
//        List<MutableComponent> entries = new ArrayList<>();
//
//        MutableComponent title = Component.translatableWithFallback("tooltip.relics." + BuiltInRegistries.ITEM.getKey(screen.stack.getItem()).getPath() + ".ability." + ability, ability).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE);
//
//        if (!relic.isAbilityUnlocked(screen.stack, ability))
//            title = ScreenUtils.obfuscate(title, 0.99F, minecraft.level.getGameTime() / 5);
//
//        entries.add(title);
//
//        int level = relic.getRelicLevel(screen.stack);
//        int requiredLevel = data.getRequiredLevel();
//
//        if (level < requiredLevel) {
//            entries.add(Component.literal(" "));
//
//            entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.low_level", Component.literal(String.valueOf(requiredLevel)).withStyle(ChatFormatting.BOLD))));
//        } else {
//            if (!relic.isLockUnlocked(screen.stack, ability)) {
//                entries.add(Component.literal(" "));
//
//                entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.ready_to_unlock", Component.literal(String.valueOf(relic.getMaxLockUnlocks() - relic.getLockUnlocks(screen.stack, ability))).withStyle(ChatFormatting.BOLD))));
//            } else {
//                if (!relic.isAbilityResearched(screen.stack, ability)) {
//                    entries.add(Component.literal(" "));
//
//                    entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.unresearched")));
//                } else {
//                    if (data.getMaxLevel() == 0) {
//                        entries.add(Component.literal(" "));
//
//                        entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.no_stats")));
//                    } else if (relic.mayPlayerUpgrade(minecraft.player, screen.stack, ability)) {
//                        entries.add(Component.literal(" "));
//
//                        entries.add(Component.literal("").append(Component.translatable("tooltip.relics.researching.relic.card.ready_to_upgrade")));
//                    }
//                }
//            }
//        }
//
//        for (MutableComponent entry : entries) {
//            int entryWidth = (minecraft.font.width(entry)) / 2;
//
//            if (entryWidth > renderWidth)
//                renderWidth = Math.min(entryWidth + 2, maxWidth);
//
//            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
//        }
//
//        int height = tooltip.size() * 5;
//
//        int y = getHeight() / 2;
//
//        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
//
//        float lerpedScale = Mth.lerp(partialTicks, scaleOld, scale);
//
//        poseStack.scale(lerpedScale, lerpedScale, lerpedScale);
//
//        poseStack.translate((getX() + (getWidth() / 2F)) / lerpedScale, (getY() + (getHeight() / 2F)) / lerpedScale, 0);
//
//        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, height, -((renderWidth + 19) / 2), y);
//
//        int yOff = 0;
//
//        for (FormattedCharSequence entry : tooltip) {
//            poseStack.pushPose();
//
//            poseStack.scale(0.5F, 0.5F, 0.5F);
//
//            guiGraphics.drawString(minecraft.font, entry, -(minecraft.font.width(entry) / 2), ((y + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);
//
//            yOff += 5;
//
//            poseStack.popPose();
//        }
//    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!isLocked())
            super.playDownSound(handler);
    }
}