package it.hurts.sskirillss.relics.client.screen.description.experience.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import it.hurts.sskirillss.relics.client.screen.base.IHoverableWidget;
import it.hurts.sskirillss.relics.client.screen.base.ITickingWidget;
import it.hurts.sskirillss.relics.client.screen.description.experience.ExperienceDescriptionScreen;
import it.hurts.sskirillss.relics.client.screen.description.general.widgets.base.AbstractDescriptionWidget;
import it.hurts.sskirillss.relics.client.screen.description.misc.DescriptionTextures;
import it.hurts.sskirillss.relics.client.screen.description.research.particles.SmokeParticleData;
import it.hurts.sskirillss.relics.client.screen.utils.ParticleStorage;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.utils.MathUtils;
import it.hurts.sskirillss.relics.utils.Reference;
import it.hurts.sskirillss.relics.utils.data.GUIRenderer;
import it.hurts.sskirillss.relics.utils.data.SpriteAnchor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Locale;

public class BigExperienceCardWidget extends AbstractDescriptionWidget implements ITickingWidget {
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

        if (isUnlocked) {
            GUIRenderer.begin(sourceData.getIcon().apply(stack), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .color(color, color, color, 1F)
                    .pos(getX() + 7, getY() + 10)
                    .texSize(34, 49)
                    .end();

            RenderSystem.enableBlend();

            GUIRenderer.begin(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "textures/gui/description/experience/filters/" + sourceData.getColor().name().toLowerCase(Locale.ROOT) + ".png"), poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() + 7, getY() + 10)
                    .texSize(34, 49)
                    .end();

            RenderSystem.disableBlend();
        } else
            GUIRenderer.begin(DescriptionTextures.BIG_CARD_BACKGROUND, poseStack)
                    .anchor(SpriteAnchor.TOP_LEFT)
                    .pos(getX() + 7, getY() + 10)
                    .end();

        GUIRenderer.begin(isUnlocked ? DescriptionTextures.BIG_CARD_FRAME_UNLOCKED_ACTIVE : DescriptionTextures.BIG_CARD_FRAME_UNLOCKED_INACTIVE, poseStack)
                .anchor(SpriteAnchor.TOP_LEFT)
                .pos(getX(), getY())
                .end();

        {
            poseStack.pushPose();

            MutableComponent pointsComponent = Component.literal(isUnlocked ? String.valueOf(relic.getLevelingSourceLevel(stack, source)) : "?").withStyle(ChatFormatting.BOLD);

            poseStack.scale(0.75F, 0.75F, 1F);

            guiGraphics.drawString(minecraft.font, pointsComponent, (int) (((getX() + 25.5F) * 1.33F) - (minecraft.font.width(pointsComponent) / 2F)), (int) ((getY() + 4) * 1.33F), isUnlocked ? 0xFFE278 : 0xB7AED9, true);

            poseStack.popPose();
        }

        poseStack.popPose();
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