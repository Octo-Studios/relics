package it.hurts.sskirillss.relics.client.screen.description.experience.widgets;

import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.experience.ExperienceDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.research.particles.SmokeParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.util.RandomSource;

public class BigExperienceCardWidget extends AbstractDescriptionWidget implements IHoverableWidget, ITickingWidget {
    private ExperienceDescriptionScreen screen;

    public BigExperienceCardWidget(int x, int y, ExperienceDescriptionScreen screen) {
        super(x, y, 48, 74);

        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        var stack = screen.getStack();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var player = minecraft.player;
        var poseStack = guiGraphics.pose();
        var source = screen.getSelectedSource();
        var sourceData = relic.getLevelingSourcesData().getSources().get(source);

        var isUnlocked = relic.isLevelingSourceUnlocked(stack, source);

        float color = (float) (1.05F + (Math.sin((player.tickCount + (sourceData.getId().length() * 10)) * 0.2F) * 0.1F));

        poseStack.pushPose();

        if (isUnlocked)
            GUIRenderer.begin(sourceData.getIcon().apply(stack), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .color(color, color, color, 1F)
                    .pos(getX() + 7, getY() + 10)
                    .texSize(34, 49)
                    .end();
        else
            GUIRenderer.begin(DescriptionTextures.BIG_CARD_BACKGROUND, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() + 7, getY() + 10)
                    .end();

        GUIRenderer.begin(isUnlocked ? DescriptionTextures.BIG_CARD_FRAME_UNLOCKED_ACTIVE : DescriptionTextures.BIG_CARD_FRAME_UNLOCKED_INACTIVE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY())
                .end();

        if (isHovered())
            GUIRenderer.begin(DescriptionTextures.BIG_CARD_FRAME_OUTLINE, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() - 1, getY() - 1)
                    .end();

        poseStack.popPose();
    }

    @Override
    public void onHovered(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//        var stack = screen.getStack();
//        var ability = screen.getSelectedAbility();
//
//        if (!(stack.getItem() instanceof IRelicItem relic) || !relic.isAbilityUnlocked(stack, ability))
//            return;
//
//        PoseStack poseStack = guiGraphics.pose();
//
//        List<FormattedCharSequence> tooltip = Lists.newArrayList();
//
//        int maxWidth = 150;
//        int renderWidth = 0;
//
//        List<MutableComponent> entries = Lists.newArrayList(
//                Component.literal("").append(Component.translatable("tooltip.relics.researching.ability.info.level").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + relic.getAbilityLevel(stack, ability) + "/" + relic.getAbilityData(ability).getMaxLevel()),
//                Component.literal("").append(Component.translatable("tooltip.relics.researching.ability.info.quality").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE)).append(" " + MathUtils.round(relic.getAbilityQuality(stack, ability) / 2F, 1) + "/" + relic.getMaxQuality() / 2),
//                Component.literal(" ")
//        );
//
//        if (Screen.hasShiftDown())
//            entries.add(Component.translatable("tooltip.relics.researching.ability.info.extra_info").withStyle(ChatFormatting.ITALIC));
//        else
//            entries.add(Component.translatable("tooltip.relics.researching.general.extra_info"));
//
//        for (MutableComponent entry : entries) {
//            int entryWidth = (minecraft.font.width(entry) / 2);
//
//            if (entryWidth > renderWidth)
//                renderWidth = Math.min(entryWidth + 2, maxWidth);
//
//            tooltip.addAll(minecraft.font.split(entry, maxWidth * 2));
//        }
//
//        poseStack.pushPose();
//
//        poseStack.translate(0F, 0F, 400);
//
//        DescriptionUtils.drawTooltipBackground(guiGraphics, renderWidth, tooltip.size() * 5, mouseX - 9 - (renderWidth / 2), mouseY);
//
//        poseStack.scale(0.5F, 0.5F, 0.5F);
//
//        int yOff = 0;
//
//        for (FormattedCharSequence entry : tooltip) {
//            guiGraphics.drawString(minecraft.font, entry, ((mouseX - renderWidth / 2) + 1) * 2, ((mouseY + yOff + 9) * 2), DescriptionUtils.TEXT_COLOR, false);
//
//            yOff += 5;
//        }
//
//        poseStack.popPose();
    }

    @Override
    public void onTick() {
        var stack = screen.getStack();
        var source = screen.getSelectedSource();

        if (!(stack.getItem() instanceof IRelicItem relic))
            return;

        var isUnlocked = relic.isLevelingSourceUnlocked(stack, source);

        if (!isUnlocked) {
            RandomSource random = minecraft.player.getRandom();

            ParticleStorage.addParticle(screen, new SmokeParticleData(getX() + 11 + random.nextInt(27), getY() + 15 + random.nextInt(43), 0.75F + (random.nextFloat() * 0.25F), 20 + random.nextInt(40), 0.5F)
                    .setDeltaX(MathUtils.randomFloat(random) * 0.1F).setDeltaY(MathUtils.randomFloat(random) * 0.1F));
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {

    }
}